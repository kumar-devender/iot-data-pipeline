package com.relay42.iotdatacollection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic adviceTopic() {
        return new NewTopic("H02", 3, (short) 1);
    }
}
