package com.relay42;

import com.relay42.protocol.h02.H02DTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.io.Serializable;
import java.util.*;

public class MainTest implements Serializable {
    public static void main(String[] args) throws InterruptedException {
        SparkConf sparkConf = new SparkConf().setAppName("JavaDirectKafkaWordCount")
                .setMaster("local");
        //.setMaster("spark://localhost:7077");
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(2));

        Set<String> topicsSet = new HashSet<>(Arrays.asList("H02"));
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "iot-devices-group");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        kafkaParams.put(JsonDeserializer.TRUSTED_PACKAGES, "*");


        // Create direct kafka stream with brokers and topics
        JavaInputDStream<ConsumerRecord<String, H02DTO>> messages = KafkaUtils.createDirectStream(
                jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topicsSet, kafkaParams));

        // Get the lines, split them into words, count the words and print
        JavaDStream<H02DTO> lines = messages.map(ConsumerRecord::value);
        //lines.print();
        //lines.foreachRDD(MainTest::printRDD);

        lines.foreachRDD(x -> {
            x.collect().stream().forEach(n -> System.out.println("item of list: " + n));
        });

        // Start the computation
        jssc.start();
        jssc.awaitTermination();
    }

    private static void printMe(H02DTO h02DTO) {
        System.out.println("hello world !" + h02DTO);
    }

    public static void printRDD(final JavaRDD<H02DTO> s) {
        s.foreach(MainTest::printMe);
    }

}
