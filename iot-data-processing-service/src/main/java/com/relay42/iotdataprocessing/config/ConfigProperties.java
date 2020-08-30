package com.relay42.iotdataprocessing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class ConfigProperties implements Serializable {
    @Autowired
    private Environment env;

    public String get(String configKey) {
        return env.getProperty(configKey);
    }
}
