package com.relay42.dto;

import com.relay42.protocol.h02.H02DTO;
import lombok.Data;

import java.util.Map;

@Data
public class H02ExtendedDTO extends H02DTO {
    private Map<String, String> meta;
}
