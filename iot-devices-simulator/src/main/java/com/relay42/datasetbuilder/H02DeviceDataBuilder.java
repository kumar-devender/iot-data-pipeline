package com.relay42.datasetbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relay42.dto.H02DTO;
import com.relay42.util.ResourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class H02DeviceDataBuilder implements DataSetBuilder {
    private final ObjectMapper objectMapper;

    @Override
    public boolean isSupported(String device) {
        return "H02".equals(device);
    }

    @Override
    public List<Object> buildData(int deviceId) {
        try {
            String dataJson = ResourceUtil.readResource("json/h02_data.json");
            List<H02DTO> h02DTOS = objectMapper.readValue(dataJson, new TypeReference<List<H02DTO>>() {
            }).stream().peek(h02DTO -> h02DTO.setId(deviceId))
                    .collect(Collectors.toList());
            return new ArrayList<>(h02DTOS.subList(0, 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
