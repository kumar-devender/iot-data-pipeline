package com.relay42.protocol.h02;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class H02DTO implements Serializable {
    private int id;
    private double lat;
    private double lon;
    private int speed;
    private String protocol;
    private int fuel;
    private Map<String, String> metaData;
}
