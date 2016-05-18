package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

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
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
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

import com.mycompany.app.Rule.resultCode;

import junit.textui.ResultPrinter;

import org.apache.log4j.*;

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
  private static final Pattern SPACE = Pattern.compile(" ");
  private static ArrayList<Rule> currentRules;
  
  final static Logger logger = Logger.getLogger(App.class);

  private App() {
  }

  public static void main(String[] args) {
    if (args.length < 4) {
      System.err.println("Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
      System.exit(1);
    }
    
    currentRules = new ArrayList<Rule>();
    
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
				types.add(new Tuple ((String) ((JSONObject) type).get("name"),
						Math.toIntExact((Long) ((JSONObject) type).get("number"))));
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
			currentRules.add(Rule);
		}
		
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		e.printStackTrace();
	}
	
	final long baseRuleSize = currentRules.size();
	logger.error("initial currentRules size: " + baseRuleSize);
    
    SparkConf sparkConf = new SparkConf().setAppName("JavaKafkaWordCount");
    // Create the context with 2 seconds batch size
    JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

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

    final JavaRDD<Rule> rulesRDD = jssc.sparkContext().parallelize(currentRules);
    
    JavaPairDStream<String, Rule> typeRulePairDStream = lines.transformToPair(
    		new Function<JavaRDD<String>, JavaPairRDD <String, Rule>>() {
    			public JavaPairRDD<String, Rule> call (JavaRDD<String> t) {
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
					
					if (type == null) return null;
					
					
					
					switch (result) {
						case UPDATE:
							if (rule.isBase()) {
								rule.update(type);
								currentRules.add(rule);
								logger.error("UPDATE when rule is Base");
							}
							else {
								// find rule from currentRules
								// remove it and add new rule updated
								currentRules = rule.removeFrom(currentRules);
								rule.update(type);
								currentRules.add(rule);
								logger.error("UPDATE when rule is not Base");
							}
							return rule;
						case FAIL:
							logger.error("FAIL");
							return rule;
						case TIMEOVER:
							//remove rule;
							currentRules = rule.removeFrom(currentRules);
							logger.error("TIMEOVER");
							return null;
						case COMPLETE:
							// remove rule;
							logger.error("currentRules.size() = " + currentRules.size());
							currentRules = rule.removeFrom(currentRules);
							logger.error("currentRules.size() = " + currentRules.size());
							logger.error("COMPLETE");
							// operation on complete rule including saving to DB
							return null;
					}
					
					logger.error("I don't know");
					return null;
				}
    		});
    
    
//    JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
//      public Iterable<String> call(String x) {
//        return Lists.newArrayList(SPACE.split(x));
//      }
//    });
//
//    JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
//      new PairFunction<String, String, Integer>() {
//        public Tuple2<String, Integer> call(String s) {
//          return new Tuple2<String, Integer>(s, 1);
//        }
//      }).reduceByKey(new Function2<Integer, Integer, Integer>() {
//        public Integer call(Integer i1, Integer i2) {
//          return i1 + i2;
//        }
//      });
    
//    lines.print();
//    typeRulePairDStream.print();
    resultRuleDstream.print();
//    wordCounts.print();
    jssc.start();
    jssc.awaitTermination();
  }
}