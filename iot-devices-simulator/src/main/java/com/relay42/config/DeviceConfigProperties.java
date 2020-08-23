package com.relay42.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "relay42.config")
@Configuration
@Data
public class DeviceConfigProperties {
    private Map<String, List<DeviceConfig>> devices;
}
