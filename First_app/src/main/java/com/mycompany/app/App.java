package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.spark.api.java.function.FlatMapFunction;
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
import com.mycompany.app.Rule.RESULTCODE;

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

	private App() {
	}

	private static void writeDB (Rule r) {
		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("test");
		DBCollection collection = db.getCollection("Rules");
		BasicDBObject document = new BasicDBObject();
		document.put("name", r.getName());

		ArrayList<BasicDBObject> types = new ArrayList<BasicDBObject>();
		for (Tuple t : r.getCheckedTypes()) {
			BasicDBObject tmp = new BasicDBObject();
			tmp.put("name", t.typeName);
			tmp.put("content", t.content);
			types.add(tmp);
		}
		document.put("types", types);

		collection.insert(document);
		mongo.close();
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
						types.add(new Tuple ((String) ((JSONObject) type).get("name")));
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
				return tuple2._2();
			}
		});

		JavaPairDStream<ArrayList<String>, Rule> typeRulePairDStream = lines.transformToPair(
				new Function<JavaRDD<String>, JavaPairRDD <ArrayList<String>, Rule>>() {
					public JavaPairRDD<ArrayList<String>, Rule> call (JavaRDD<String> t) {
						JavaRDD<Rule> baseRulesRDD = jssc.sparkContext().parallelize(baseRules);
						JavaRDD<Rule> remainRulesRDD = jssc.sparkContext().parallelize(remainRules);
						JavaRDD<Rule> rulesRDD = baseRulesRDD.union(remainRulesRDD);
						logger.error("Size of baseRDD: " + baseRulesRDD.count());
						logger.error("Size of remainRulesRDD: " + remainRulesRDD.count());
						logger.error("Size of lines: " + t.count());

						ArrayList<ArrayList<String>> a = new ArrayList<ArrayList<String>>();
						ArrayList<String> arrayListT = new ArrayList<String>();
						arrayListT.addAll(t.collect());
						a.add(arrayListT);
						JavaRDD<ArrayList<String>> tmp = jssc.sparkContext().parallelize(a);

						return tmp.cartesian(rulesRDD);
					}
				});


		JavaDStream<Rule> resultRuleDstream = typeRulePairDStream.flatMap (
				new FlatMapFunction<Tuple2<ArrayList<String>, Rule>, Rule> () {

					public Iterable<Rule> call(Tuple2<ArrayList<String>, Rule> t) throws Exception {
						// TODO Auto-generated method stub

						ArrayList<Rule> result = new ArrayList<Rule>();
						ArrayList<String> packetStream = t._1;
						Rule rule = t._2.ruleClone();
						Tuple type = null;
						Boolean fromBase = rule.isBase();

						if (fromBase) {
							for (String packet : packetStream) {
								try {
									final JSONParser parser = new JSONParser();
									JSONObject obj = (JSONObject) parser.parse(packet);
									type = new Tuple (obj.get("type").toString(), 
											obj.get("content").toString());
								} catch (ParseException e) {
									e.printStackTrace();
								}

								if (type == null) {
									logger.error("CANNOT ARRIVE HERE! (type == null)");
									continue;
								}

								ArrayList<Rule> tempArray = new ArrayList<Rule>();
								for (Rule r : result) {
									switch (r.check(type)) {
									case UPDATE:
										r.update(type);
										tempArray.add(r);
										break;
									case FAIL:
										tempArray.add(r);
										break;
									case TIMEOVER:
										//remove
										logger.error("TIMEOVER RULE");
										break;
									case COMPLETE:
										r.update(type);
										writeDB(r);
										logger.error("COMPLETE: " + r.getCheckedTypes().size() + " / " + r.getTypes().size());
										break;
									}
								}
								result = tempArray;
								
								Rule baseTemp = rule.ruleClone();
								switch (baseTemp.check(type)) {
								case UPDATE:
									baseTemp.update(type);
									result.add(baseTemp);
									break;
								case FAIL:
									break;
								case TIMEOVER:
									logger.error("TIMEOVER RULE");
									break;
								case COMPLETE:
									baseTemp.update(type);
									writeDB(baseTemp);
									logger.error("COMPLETE: " + baseTemp.getCheckedTypes().size() + " / " + baseTemp.getTypes().size());
									break;
								}
							}
							
							return result;
						}
						else {
							for (String packet : packetStream) {
								try {
									final JSONParser parser = new JSONParser();
									JSONObject obj = (JSONObject) parser.parse(packet);
									type = new Tuple (obj.get("type").toString(), 
											obj.get("content").toString());
								} catch (ParseException e) {
									e.printStackTrace();
								}

								if (type == null) {
									logger.error("CANNOT ARRIVE HERE! (type == null)");
									continue;
								}

								switch (rule.check(type)) {
								case UPDATE:
									rule.update(type);
									continue;
								case FAIL:
									continue;
								case TIMEOVER:
									logger.error("TIMEOVER RULE");
									result.add(rule);
									return result;
								case COMPLETE:
									rule.update(type);
									writeDB(rule);
									logger.error("COMPLETE: " + rule.getCheckedTypes().size() + " / " + rule.getTypes().size());
									return result;
								}
							}
							
							result.add(rule);
							return result;
						}
					}
				});

		resultRuleDstream.foreachRDD(
				new VoidFunction<JavaRDD<Rule>> (){
					public void call(JavaRDD<Rule> t) throws Exception {
						// TODO Auto-generated method stub

						List<Rule> a = t.collect();
						int cnt = 0;
						for (Rule r : a) {
							if (cnt == 0) {
								remainRules = new ArrayList<Rule>();
								cnt++;
							}
							if (r != null) {
								remainRules.add(r);
							}
						}
					}
				});

		jssc.start();
		jssc.awaitTermination();
	}
}