package com.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.google.common.collect.Lists;

import kafka.serializer.StringDecoder;
import scala.Tuple2;

public class UserClickSpark {
	
	private static final Pattern SPACE = Pattern.compile(" ");
	
	public static void main(String[] args) {
		
		    String brokers = ConfigUtil.get("brokers");
		    String topics =  ConfigUtil.get("topics");

		    // Create context with 1 second batch interval
		    SparkConf sparkConf = new SparkConf().setAppName("SparkKafkaUserClick");
		    JavaStreamingContext context = new JavaStreamingContext(sparkConf, Durations.seconds(1));

		    HashSet<String> topicsSet = new HashSet<String>(Arrays.asList(topics.split(",")));
		    HashMap<String, String> kafkaParams = new HashMap<String, String>();
		    kafkaParams.put("metadata.broker.list", brokers);

		    // Create direct kafka stream with brokers and topics
		    JavaPairInputDStream<String, String> messages = KafkaUtils.createDirectStream(
		    	context,
		        String.class,
		        String.class,
		        StringDecoder.class,
		        StringDecoder.class,
		        kafkaParams,
		        topicsSet
		    );

		    // Get the lines
		    JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
		      public String call(Tuple2<String, String> tuple2) {
		        return tuple2._2();
		      }
		    });
		    
		    //split them into words 
		    JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
		      public Iterable<String> call(String x) {
		    	JSONObject jsonobj = JSONObject.fromObject(x);
		    	String idfa = jsonobj.has("idfa") ? jsonobj.getString("idfa") : "";
		        return Lists.newArrayList(idfa);
		      }
		    });
		    
		    //count the deviceId
		    JavaPairDStream<String, Integer> clickCounts = words.mapToPair(
		      new PairFunction<String, String, Integer>() {
		        public Tuple2<String, Integer> call(String s) {
		          return new Tuple2<String, Integer>(s, 1);
		        }
		      }).reduceByKey(
		        new Function2<Integer, Integer, Integer>() {
		        public Integer call(Integer i1, Integer i2) {
		          return i1 + i2;
		        }
		      });
		    
		    //print
		    clickCounts.print();

		    // Start the computation
		    context.start();
		    context.awaitTermination();
		   
//		    context.stop();
		  }
	
}


