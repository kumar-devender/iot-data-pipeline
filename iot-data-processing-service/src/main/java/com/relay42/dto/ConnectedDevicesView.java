package com.relay42.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConnectedDevicesView implements Serializable {
    private String protocol;
    private long total;
    //private LocalDateTime timeStamp;
}
