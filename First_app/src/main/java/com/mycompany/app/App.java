package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

  private App() {
  }

  public static void main(String[] args) {
    if (args.length < 4) {
      System.err.println("Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
      System.exit(1);
    }
    
    currentRules = new ArrayList<Rule>();

    JSONParser parser = new JSONParser();
	try {
		Object obj = parser.parse(new FileReader("Rule.json"));
		
		JSONObject jsonObject = (JSONObject) obj;
 
		JSONArray Rules = (JSONArray) jsonObject.get("Rule");
		
		for (Object rule : Rules) {
			JSONObject jsonRule = (JSONObject) rule;
			
			JSONArray jsonTypes = (JSONArray) jsonRule.get("types");
			ArrayList<String> types = new ArrayList<String>();
			
			for (Object type : jsonTypes) {
				types.add((String) ((JSONObject) type).get("name"));
			}
			
			Rule Rule = new Rule ((String) jsonRule.get("name"),
					(Long) jsonRule.get("duration"),
					(Boolean) jsonRule.get("ordered"), 
					types);
			currentRules.add(Rule);
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		e.printStackTrace();
	}
	
	System.out.println(currentRules.size());
    
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
    
    lines.print();
//    wordCounts.print();
    jssc.start();
    jssc.awaitTermination();
  }
}