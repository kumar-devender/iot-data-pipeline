package com.relay42.iotdataprocessing.config;

import lombok.RequiredArgsConstructor;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class SparkConfig {
    private final Environment env;

    @Bean
    public SparkConf conf() {
        return new SparkConf()
                .setAppName(env.getProperty("relay42.iotdataprocessing.config.spark.app.name"))
                //.setMaster("local");
                .setMaster(env.getProperty("relay42.iotdataprocessing.config.spark.master"))
                .set("spark.cassandra.connection.host", env.getProperty("relay42.iotdataprocessing.config.cassandra.host"))
                .set("spark.cassandra.connection.port", env.getProperty("relay42.iotdataprocessing.config.cassandra.port"))
                .set("spark.cassandra.auth.username", env.getProperty("relay42.iotdataprocessing.config.cassandra.username"))
                .set("spark.cassandra.auth.password", env.getProperty("relay42.iotdataprocessing.config.cassandra.password"))
                .set("spark.cassandra.connection.keep_alive_ms", env.getProperty("relay42.iotdataprocessing.config.cassandra.keep_alive"));
    }

    @Bean
    public JavaStreamingContext javaSparkContext() {
        JavaStreamingContext jsc = new JavaStreamingContext(conf(), Durations.seconds(5));
        //jsc.checkpoint(env.getProperty("relay42.iotdataprocessing.config.spark.checkpoint.dir"));
        return jsc;
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder().config(conf()).getOrCreate();
    }
}
