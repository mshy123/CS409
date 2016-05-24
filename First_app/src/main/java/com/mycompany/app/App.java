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

	public static void dbSend (JavaRDD<Rule> t) {
		t.foreachPartition(
				new VoidFunction<Iterator<Rule>>() {
					Mongo mongo = new Mongo("localhost", 27017);
					DB db = mongo.getDB("test");
					DBCollection collection = db.getCollection("Rules");
					public void call(Iterator<Rule> t) throws Exception {
						// TODO Auto-generated method stub

						BasicDBObject document = new BasicDBObject();
						document.put("name", "mkyongDB");
						document.put("table", "hosting");

						BasicDBObject documentDetail = new BasicDBObject();
						documentDetail.put("records", 99);
						documentDetail.put("index", "vps_index1");
						documentDetail.put("active", "true");
						document.put("detail", documentDetail);

						collection.insert(document);
					}
				});
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
					attributes.add(attribute.toString());
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

		final long baseRuleSize = baseRules.size();
		logger.error("Initial baseRules size: " + baseRuleSize);

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
				return tuple2._2();
			}
		});

		JavaPairDStream<String, Rule> typeRulePairDStream = lines.transformToPair(
				new Function<JavaRDD<String>, JavaPairRDD <String, Rule>>() {
					public JavaPairRDD<String, Rule> call (JavaRDD<String> t) {
						JavaRDD<Rule> baseRulesRDD = jssc.sparkContext().parallelize(baseRules);
						JavaRDD<Rule> remainRulesRDD = jssc.sparkContext().parallelize(remainRules);
						logger.error("cartesian base size: " + baseRules.size());
						logger.error("cartesian remain size: " + remainRules.size());
						JavaRDD<Rule> rulesRDD = baseRulesRDD.union(remainRulesRDD);
						return t.cartesian(rulesRDD);
					}
				});

		JavaDStream<Rule> resultRuleDstream = typeRulePairDStream.map(
				new Function<Tuple2<String, Rule>, Rule> () {
					public Rule call(Tuple2<String, Rule> v1) {
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
							return null;
						}


						switch (result) {
						case UPDATE:
							rule.update(type);
							logger.error("UPDATE");
							return rule;
						case FAIL:
							logger.error("FAIL");
							if (rule.isBase()) {
								return null;
							}
							else {
								return rule;
							}
						case TIMEOVER:
							//remove rule;
							logger.error("TIMEOVER");
							return null;
						case COMPLETE:
							// remove rule;
							logger.error("COMPLETE");
							rule.update(type);
							// operation on complete rule including saving to DB
							return rule;
						}

						return null;
					}
				});
		resultRuleDstream = resultRuleDstream.filter(
				new Function<Rule, Boolean> () {

					public Boolean call(Rule v1) throws Exception {
						// TODO Auto-generated method stub
						return v1!=null;
					}

				});

		resultRuleDstream.foreachRDD(
				new VoidFunction<JavaRDD<Rule>> (){

					public void call(JavaRDD<Rule> t) throws Exception {
						// TODO Auto-generated method stub
						if (remainRules.size() != 0) {
							logger.error("remainRules init");
							remainRules = new ArrayList<Rule>();
						}

						t.foreach( new VoidFunction<Rule> () {
							public void call(Rule t) throws Exception {
								// TODO Auto-generated method stub
								if (t.checkComplete()) {
									Mongo mongo = new Mongo("localhost", 27017);
									DB db = mongo.getDB("test");
									DBCollection collection = db.getCollection("Rules");
									BasicDBObject document = new BasicDBObject();
									document.put("name", t.getName());

									ArrayList<BasicDBObject> types = new ArrayList<BasicDBObject>();
									for (Tuple r : t.getCheckedTypes()) {
										BasicDBObject tmp = new BasicDBObject();
										tmp.put("name", r.typeName);
										tmp.put("content", r.content);
										types.add(tmp);
									}
									document.put("types", types);

									collection.insert(document);
									mongo.close();
								}
							}
						});
					}
				});

		resultRuleDstream.foreachRDD(
				new VoidFunction<JavaRDD<Rule>> (){

					public void call(JavaRDD<Rule> t) throws Exception {
						// TODO Auto-generated method stub
						if (!t.isEmpty()) {
							t.foreach( new VoidFunction<Rule> () {
								public void call(Rule t) throws Exception {
									// TODO Auto-generated method stub
									if (!t.checkComplete()) {
										remainRules.add(t);
									}
								}
							});
						}
					}
				});


		resultRuleDstream.print();
		jssc.start();
		jssc.awaitTermination();
	}
}