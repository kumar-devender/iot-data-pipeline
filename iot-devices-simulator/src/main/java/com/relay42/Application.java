package com.relay42;

import com.relay42.config.DeviceConfig;
import com.relay42.config.DeviceConfigProperties;
import com.relay42.datasetbuilder.DataSetBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private List<DataSetBuilder> dataSetBuilders;
    @Autowired
    private DeviceConfigProperties deviceConfigProperties;

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        deviceConfigProperties.getDevices()
                .entrySet().stream()
                .forEach(entry -> {
                    startDevice(entry.getKey(), entry.getValue());
                });
    }

    private void startDevice(String protocol, List<DeviceConfig> devices) {
        DataSetBuilder dataSetBuilder = getDataSetBuilder(protocol);
        devices.forEach(deviceConfig -> new Device(protocol, deviceConfig, dataSetBuilder).start());
    }

    private DataSetBuilder getDataSetBuilder(String deviceProtocol) {
        return dataSetBuilders.stream()
                .filter(dataSetBuilder -> dataSetBuilder.isSupported(deviceProtocol))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(String.format("DataSetBuilder for %s is not implemented", deviceProtocol)));
    }
}
