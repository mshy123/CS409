package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.spark.broadcast.Broadcast;
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

import scala.Tuple2;

/**
 * CEP engine that consumes messages from one or more topics in Kafka.
 *
 * The engine's algorithm to detect user defined rule is following.
 * There are two kinds of rules, one is baseRules and the other is remainRules.
 * baseRules doesn't change except beginning of the program to read Rule.json.
 * remainRUles comes from baseRules.
 * baseRules that is compared to packet can produce new rule instance that belogns to remainRules.
 * All packet messages are paired with each baseRules, remainRules.
 * 
 * For example, let there is rule that is counted as complete when getting 5 "apachepost" types.
 * We can indicate like this.
 * 
 * Rule1 ( 0 / 5 )
 * 
 * 5 means number of "apachepost" types that should be filtered.
 * 0 means current state of Rule instance.
 * 
 * Then let say program receive 2 series of "apachepost" type packets.
 * 
 * before messages received.
 * baseRules = [ Rule1 ( 0 / 5 ) ]
 * remainRules = [ ]
 * 
 * messages pairing.
 * ( [ "apachepost", "apachepost" ], Rule1 ( 0 / 5 ) ) 
 * 
 * after messages received.
 * baseRules = [ Rule1 ( 0 / 5 ) ]
 * remainRUles = [ Rule1 ( 1 / 5 ), Rule1 ( 2 / 5 ) ]
 * 
 * 
 * Then let say program receive 4 series of "apachepost" type packets.
 * 
 * before messages received.
 * baseRules = [ Rule1 ( 0 / 5 ) ]
 * remainRUles = [ Rule1 ( 1 / 5 ), Rule1 ( 2 / 5 ) ]
 * 
 * messages pairing.
 * ( [ "apachepost", "apachepost", "apachepost", "apachepost" ], Rule1 ( 0 / 5 ) )
 * ( [ "apachepost", "apachepost", "apachepost", "apachepost" ], Rule1 ( 1 / 5 ) )
 * ( [ "apachepost", "apachepost", "apachepost", "apachepost" ], Rule1 ( 2 / 5 ) ) 
 * 
 * after messages received.
 * baseRules = [ Rule1 ( 0 / 5 ) ]
 * remainRUles = [ Rule1 ( 1 / 5 ), Rule1 ( 2 / 5 ), Rule1 ( 3 / 5 ), Rule1 ( 4 / 5 ) ]
 * completeRules = [ Rule1 ( 5 / 5 ), Rule1 ( 5 / 5 ) ]
 *
 * According to our policy, total 6 packets return 2 complete Rule1.
 * ("apachepost", "apachepost", "apachepost", "apachepost", "apachepost"), "apachepost"
 * "apachepost", ("apachepost", "apachepost", "apachepost", "apachepost", "apachepost")
 *
 * Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>
 *   <zkQuorum> is a list of one or more zookeeper servers that make quorum
 *   <group> is the name of kafka consumer group
 *   <topics> is a list of one or more kafka topics to consume from
 *   <numThreads> is the number of threads the kafka consumer should use
 */

public class App {
	/** 
	 * The "Rule"s array read from Rule.json file.
	 * It only added beginning of program,
	 * and doesn't change anymore in program's life cycle.
	 */
	private static ArrayList<Rule> baseRules;

	/** The "Rule"s array indicating that current state of the rules (how many types are proceeded). */
	private static ArrayList<Rule> remainRules;
	
	final static Logger logger = Logger.getLogger(App.class);

	/**
	 * Read Rule.json and save parse into Rule instances.
	 * All result are saved in baseRules.
	 */
	private static void readRule () {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("/Users/hyunhoha/LocalCEP/Rule.json"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray Rules = (JSONArray) jsonObject.get("Rule");

			for (Object rule : Rules) {
				JSONObject jsonRule = (JSONObject) rule;

				JSONArray jsonTypes = (JSONArray) jsonRule.get("types");
				ArrayList<Type> types = new ArrayList<Type>();

				for (Object type : jsonTypes) {
					for (int i=0;
							i<Math.toIntExact((Long) ((JSONObject) type).get("number"));
							i++) {
						types.add(new Type ((String) ((JSONObject) type).get("name")));
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
	}

	/** Write complete rule to mongoDB */
	private static void writeDB (Rule r) {
		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("test");
		DBCollection collection = db.getCollection("Rules");
		BasicDBObject document = new BasicDBObject();
		document.put("name", r.getName());

		ArrayList<BasicDBObject> types = new ArrayList<BasicDBObject>();
		for (Type t : r.getCheckedTypes()) {
			BasicDBObject tmp = new BasicDBObject();
			tmp.put("name", t.typeName);
			tmp.put("content", t.content);
			types.add(tmp);
		}
		document.put("types", types);

		Date now = new Date();
		BasicDBObject timeNow = new BasicDBObject("date", now);
		document.put("savedTime", timeNow);
		
		collection.insert(document);
		mongo.close();
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			/* Check invalid options */
			System.err.println("Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
			System.exit(1);
		}

		/* Rules initialization */
		baseRules = new ArrayList<Rule>();
		remainRules = new ArrayList<Rule>();
		readRule();

		SparkConf sparkConf = new SparkConf().setAppName("Logax");
		/* 
		 * Create the context with 2 seconds batch size
		 * Batch size can be tuned for performance. 
		 */
		final JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

		int numThreads = Integer.parseInt(args[3]);
		Map<String, Integer> topicMap = new HashMap<String, Integer>();
		String[] topics = args[2].split(",");
		for (String topic: topics) {
			topicMap.put(topic, numThreads);
		}

		JavaPairReceiverInputDStream<String, String> messages =
				KafkaUtils.createStream(jssc, args[0], args[1], topicMap);

		/* Lines is series of input packets RDD. */
		JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
			public String call(Tuple2<String, String> tuple2) {
				return tuple2._2();
			}
		});

		/* 
		 * Each RDD in typeRulePairDStream is composed of pairs.
		 * ( [packet, packet, ...], Rule )
		 * Total number of pairs is ( baseRules + remainRules )
		 * So the degree of distribution is up to number of rules.
		 */
		JavaPairDStream<ArrayList<String>, Rule> typeRulePairDStream = lines.transformToPair(
				new Function<JavaRDD<String>, JavaPairRDD <ArrayList<String>, Rule>>() {
					public JavaPairRDD<ArrayList<String>, Rule> call (JavaRDD<String> t) {
						JavaRDD<Rule> baseRulesRDD = jssc.sparkContext().parallelize(baseRules);
						JavaRDD<Rule> remainRulesRDD = jssc.sparkContext().parallelize(remainRules);
						JavaRDD<Rule> rulesRDD = baseRulesRDD.union(remainRulesRDD);

						ArrayList<ArrayList<String>> a = new ArrayList<ArrayList<String>>();
						ArrayList<String> arrayListT = new ArrayList<String>();
						arrayListT.addAll(t.collect());
						a.add(arrayListT);
						JavaRDD<ArrayList<String>> tmp = jssc.sparkContext().parallelize(a);

						return tmp.cartesian(rulesRDD);
					}
				});

		/* 
		 * Pair RDD is checked here.
		 * When Rule is baseRule, it can produce some other rule instances.
		 * When RUle is remainRule, it is only checked within itself.
		 */
		JavaDStream<Rule> resultRuleDstream = typeRulePairDStream.flatMap (
				new FlatMapFunction<Tuple2<ArrayList<String>, Rule>, Rule> () {

					public Iterable<Rule> call(Tuple2<ArrayList<String>, Rule> t) throws Exception {
						// TODO Auto-generated method stub

						ArrayList<Rule> result = new ArrayList<Rule>(); /* return value */
						ArrayList<String> packetStream = t._1; /* input packet stream */
						Rule rule = t._2.ruleClone(); /* Rule for checking */
						Type type = null;
						Boolean fromBase = rule.isBase();

						if (fromBase) {
							/* When rule is baseRule */
							for (String packet : packetStream) {
								try {
									final JSONParser parser = new JSONParser();
									JSONObject obj = (JSONObject) parser.parse(packet);
									type = new Type (obj.get("type").toString(), 
											obj.get("content").toString()); /* a parsed packet */
								} catch (ParseException e) {
									e.printStackTrace();
								}


								ArrayList<Rule> tempArray = new ArrayList<Rule>();
								for (Rule r : result) {
									/* Checking for newly created rules from baseRule (not remainRule now!) */
									switch (r.check(type)) {
									case UPDATE:
										r.update(type);
										tempArray.add(r);
										break;
									case FAIL:
										tempArray.add(r);
										break;
									case TIMEOVER:
										break;
									case COMPLETE:
										r.update(type);
										writeDB(r);
										break;
									}
								}
								result = tempArray;

								Rule baseTemp = rule.ruleClone();
								switch (baseTemp.check(type)) {
								/* Checking for baseRule */
								case UPDATE:
									baseTemp.update(type);
									result.add(baseTemp); /* Creating new Rule instance */
									break;
								case FAIL:
									break;
								case TIMEOVER:
									break;
								case COMPLETE:
									baseTemp.update(type);
									writeDB(baseTemp);
									break;
								}
							}

							return result; /* It will be added to remainRules */
						}
						else {
							/* When rule is remainRule */
							for (String packet : packetStream) {
								try {
									final JSONParser parser = new JSONParser();
									JSONObject obj = (JSONObject) parser.parse(packet);
									type = new Type (obj.get("type").toString(), 
											obj.get("content").toString());
								} catch (ParseException e) {
									e.printStackTrace();
								}

								switch (rule.check(type)) {
								case UPDATE:
									rule.update(type);
									continue;
								case FAIL:
									continue;
								case TIMEOVER:
									return result; /* result has no element */
								case COMPLETE:
									rule.update(type);
									writeDB(rule);
									return result;
								}
							}

							result.add(rule);
							return result; /* result has only one element */
						}
					}
				});

		/* Clear the remainRules and add all rules from checking process. */
		resultRuleDstream.foreachRDD(
				new VoidFunction<JavaRDD<Rule>> (){
					public void call(JavaRDD<Rule> t) throws Exception {
						// TODO Auto-generated method stub

						List<Rule> a = t.collect();
						int cnt = 0;
						for (Rule r : a) {
							if (cnt == 0) {
								remainRules = new ArrayList<Rule>(); /* clear rule. */
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
