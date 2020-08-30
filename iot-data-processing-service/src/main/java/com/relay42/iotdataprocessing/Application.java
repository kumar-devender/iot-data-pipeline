package com.relay42.iotdataprocessing;

import com.relay42.iotdataprocessing.consumer.VehicleConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {
    @Autowired
    private VehicleConsumer vehicleConsumer;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            vehicleConsumer.startStreamProcessing();
        } catch (InterruptedException e) {
            log.error("Exception while processing stream", e);
        }
    }
}
