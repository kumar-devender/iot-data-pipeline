package com.relay42.iotdatacollection.protocol;


import com.relay42.iotdatacollection.server.Server;

import java.util.List;

public interface Protocol {
    String getName();

    void initServers(List<Server> serverList);
}
