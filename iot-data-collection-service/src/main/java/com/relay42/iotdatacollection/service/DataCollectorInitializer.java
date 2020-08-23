package com.relay42.iotdatacollection.service;

import com.relay42.iotdatacollection.protocol.Protocol;
import com.relay42.iotdatacollection.server.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataCollectorInitializer {
    private final List<Protocol> protocols;

    @PostConstruct
    public void initialize() {
        List<Server> servers = new ArrayList<>();
        protocols.forEach(protocol -> protocol.initServers(servers));
        servers.forEach(server -> server.start());
    }
}
