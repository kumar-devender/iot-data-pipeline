package com.relay42.iotdataprocessing;

import com.relay42.iotdataprocessing.consumer.VehicleConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    private VehicleConsumer vehicleConsumer;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        vehicleConsumer.startStreamProcessing();
    }
}
