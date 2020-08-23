package com.relay42.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class H02DTO implements Serializable {
    private double lat;
    private double lon;
    private int speed;
    private int fuel;
}
