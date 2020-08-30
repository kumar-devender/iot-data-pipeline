package com.relay42.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class H02DTO implements Serializable {
    private int id;
    private double lat;
    private double lon;
    private int speed;
    private int fuel;
    private String protocol;
    private Map<String, String> metaData;
}
