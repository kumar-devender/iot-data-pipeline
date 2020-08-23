package com.relay42.iotdatacollection.properties;

import com.relay42.dto.ProtocolConfigDTO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@ConfigurationProperties(prefix = "relay42.iotdatacollection.protocol")
@Configuration
@Data
public class ProtocolProperties {
    private Map<String, ProtocolConfigDTO> config;

    public ProtocolConfigDTO getConfig(String name) {
        return config.get(name);
    }
}
