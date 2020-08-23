package com.relay42.config;

import lombok.Data;

@Data
public class DeviceConfig {
    private Integer deviceId;
    private String remoteHost;
    private Integer remotePort;
    private Integer dataTransmissionFreqInSecond;
}
