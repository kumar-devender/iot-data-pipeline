package com.relay42.iotdataprocessing.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic H02Topic() {
        return new NewTopic("H02", 3, (short) 1);
    }
}
