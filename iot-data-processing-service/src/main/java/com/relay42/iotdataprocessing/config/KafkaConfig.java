package com.relay42.iotdataprocessing.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class KafkaConfig {
    private Map<String, Object> props;
}
