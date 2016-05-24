package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mycompany.app.Rule.resultCode;

import scala.Tuple2;

/**
 * Consumes messages from one or more topics in Kafka and does wordcount.
 *
 * Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>
 *   <zkQuorum> is a list of one or more zookeeper servers that make quorum
 *   <group> is the name of kafka consumer group
 *   <topics> is a list of one or more kafka topics to consume from
 *   <numThreads> is the number of threads the kafka consumer should use
 *
 * To run this example:
 *   `$ bin/run-example org.apache.spark.examples.streaming.JavaKafkaWordCount zoo01,zoo02, \
 *    zoo03 my-consumer-group topic1,topic2 1`
 */

public class App {
	private static ArrayList<Rule> baseRules;
	private static ArrayList<Rule> remainRules;

	final static Logger logger = Logger.getLogger(App.class);
	public static String myUrl = "";

	private App() {
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			System.err.println("Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
			System.exit(1);
		}

		baseRules = new ArrayList<Rule>();
		remainRules = new ArrayList<Rule>();

		try {
			final JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("Rule.json"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray Rules = (JSONArray) jsonObject.get("Rule");

			for (Object rule : Rules) {
				JSONObject jsonRule = (JSONObject) rule;

				JSONArray jsonTypes = (JSONArray) jsonRule.get("types");
				ArrayList<Tuple> types = new ArrayList<Tuple>();

				for (Object type : jsonTypes) {
					for (int i=0;
							i<Math.toIntExact((Long) ((JSONObject) type).get("number"));
							i++) {
						types.add(new Tuple ((String) ((JSONObject) type).get("name"), 1));
					}
				}
				JSONArray jsonAttributes = (JSONArray) jsonRule.get("attributes");
				ArrayList<String> attributes = new ArrayList<String>();

				for (Object attribute : jsonAttributes) {
					attributes.add(((JSONObject) attribute).get("name").toString());
				}

				Rule Rule = new Rule ((String) jsonRule.get("name"),
						(Long) jsonRule.get("duration"),
						(Boolean) jsonRule.get("ordered"), 
						types, attributes);
				baseRules.add(Rule);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		logger.error("Initial baseRules size: " + baseRules.size());

		SparkConf sparkConf = new SparkConf().setAppName("JavaKafkaWordCount");
		// Create the context with 2 seconds batch size
		final JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

		int numThreads = Integer.parseInt(args[3]);
		Map<String, Integer> topicMap = new HashMap<String, Integer>();
		String[] topics = args[2].split(",");
		for (String topic: topics) {
			topicMap.put(topic, numThreads);
		}

		JavaPairReceiverInputDStream<String, String> messages =
				KafkaUtils.createStream(jssc, args[0], args[1], topicMap);

		JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
			public String call(Tuple2<String, String> tuple2) {
				logger.error("1");
				return tuple2._2();
			}
		});

		JavaPairDStream<String, Rule> typeRulePairDStream = lines.transformToPair(
				new Function<JavaRDD<String>, JavaPairRDD <String, Rule>>() {
					public JavaPairRDD<String, Rule> call (JavaRDD<String> t) {
						logger.error("2");
						JavaRDD<Rule> baseRulesRDD = jssc.sparkContext().parallelize(baseRules);
						JavaRDD<Rule> remainRulesRDD = jssc.sparkContext().parallelize(remainRules);
						JavaRDD<Rule> rulesRDD = baseRulesRDD.union(remainRulesRDD);
						logger.error("2 size of rulesRDD: " + rulesRDD.count());
						logger.error("2 size of lines: " + t.count());
						return t.cartesian(rulesRDD);
					}
				});		

		JavaPairDStream<resultCode, Rule> resultRuleDstream = typeRulePairDStream.transformToPair(
				new Function<JavaPairRDD <String, Rule>, JavaPairRDD<resultCode, Rule>> () {
					public JavaPairRDD<resultCode, Rule> call(JavaPairRDD<String, Rule> v1) {
						// TODO Auto-generated method stub
						logger.error("3");
						return v1.mapToPair(new PairFunction<Tuple2<String,Rule>, resultCode, Rule> () {
							public Tuple2<resultCode, Rule> call(Tuple2<String, Rule> v1) throws Exception {
								// TODO Auto-generated method stub
								resultCode result = resultCode.FAIL;

								Rule rule = v1._2;
								Tuple type = null;
								try {
									//{"content":"{\"host\":\"127.0.0.1\",\"user\":\"-\",\"path\":\"/login.php\",\"code\":\"201\",\"size\":\"2691\"}","type":"high.apachepost","time":1463365885}
									final JSONParser parser = new JSONParser();
									Object obj = parser.parse(v1._1);
									JSONObject packet = (JSONObject) obj;
									type = new Tuple (packet.get("type").toString(), 
											packet.get("content").toString());
									result = rule.check(type);
								} catch (ParseException e) {
									e.printStackTrace();
								}

								if (type == null) {
									return new Tuple2<resultCode, Rule> (resultCode.FAIL, null);
								}

								switch (result) {
								case UPDATE:
									rule.update(type);
									logger.error("3 UPDATE at " + type.typeName + " " + rule);
									return new Tuple2<resultCode, Rule> (resultCode.UPDATE, rule);
								case FAIL:
									logger.error("3 FAIL at " + type.typeName + " " + rule);
									if (rule.isBase()) {
										return new Tuple2<resultCode, Rule> (resultCode.FAIL, rule);
									}
									else {
										return new Tuple2<resultCode, Rule> (resultCode.FAIL, rule);
									}
								case TIMEOVER:
									//remove rule;
									logger.error("3 TIMEOVER at " + type.typeName + " " + rule);
									return new Tuple2<resultCode, Rule> (resultCode.TIMEOVER, rule);
								case COMPLETE:
									// remove rule;
									logger.error("3 COMPLETE at " + type.typeName + " " + rule);
									rule.update(type);
									// operation on complete rule including saving to DB
									return new Tuple2<resultCode, Rule> (resultCode.COMPLETE, rule);
								}

								return new Tuple2<resultCode, Rule> (resultCode.FAIL, null);
							}
						});
					}
				});

		resultRuleDstream.foreachRDD(
				new VoidFunction<JavaPairRDD<resultCode, Rule>> (){
					public void call(JavaPairRDD<resultCode, Rule> t) throws Exception {
						// TODO Auto-generated method stub
						logger.error("5");
						t.foreach( new VoidFunction<Tuple2<resultCode, Rule>> () {
							public void call(Tuple2<resultCode, Rule> t) throws Exception {
								// TODO Auto-generated method stub
								logger.error("5-1");
								if (t._1 == resultCode.COMPLETE) {
									Mongo mongo = new Mongo("localhost", 27017);
									DB db = mongo.getDB("test");
									DBCollection collection = db.getCollection("Rules");
									BasicDBObject document = new BasicDBObject();
									document.put("name", t._2.getName());

									ArrayList<BasicDBObject> types = new ArrayList<BasicDBObject>();
									for (Tuple r : t._2.getCheckedTypes()) {
										BasicDBObject tmp = new BasicDBObject();
										tmp.put("name", r.typeName);
										tmp.put("content", r.content);
										types.add(tmp);
									}
									document.put("types", types);

									collection.insert(document);
									mongo.close();

									t._2.removeFrom(remainRules);
								}
								else if (t._1 == resultCode.UPDATE) {
									t._2.removeFrom(remainRules);
									remainRules.add(t._2);
								}
								else if (t._1 == resultCode.TIMEOVER) {
									t._2.removeFrom(remainRules);
								}
							}
						});
					}
				});

		//		typeRulePairDStream.print();
		resultRuleDstream.print();
		jssc.start();
		jssc.awaitTermination();
	}
}