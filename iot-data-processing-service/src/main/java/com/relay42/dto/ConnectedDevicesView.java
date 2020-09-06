package com.relay42.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class ConnectedDevicesView implements Serializable {
    private UUID id;
    private String protocol;
    private long total;
}
