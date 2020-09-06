package com.relay42.iotdataprocessing.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class CassandraMigrationConfig {
    @Value("${relay42.iotdataprocessing.config.cassandra.password}")
    private String password;

    @Value("${relay42.iotdataprocessing.config.cassandra.username}")
    private String username;

    @Value("${relay42.iotdataprocessing.config.cassandra.keyspace-name}")
    private String keyspaceName;

    @Bean(name = "cassandraMigrationCqlSession")
    public CqlSession cassandraMigrationCqlSession() {
        return CqlSession.builder()
                .withAuthCredentials(password, password)
                .withKeyspace(keyspaceName).build();
    }

    @Bean
    @Primary
    public CqlSession applicationCqlSession() {
        return CqlSession.builder()
                .withAuthCredentials(password, password)
                .withKeyspace(keyspaceName).build();
    }
}
