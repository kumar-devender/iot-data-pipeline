package com.relay42.iotdatacollection.protocol.h02;

import com.relay42.dto.ProtocolConfigDTO;
import com.relay42.iotdatacollection.properties.ProtocolProperties;
import com.relay42.iotdatacollection.protocol.Protocol;
import com.relay42.iotdatacollection.server.Server;
import lombok.RequiredArgsConstructor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class H02Protocol implements Protocol {
    public static final String H_02_MESSAGE_HANDLER = "h02MessageHandler";
    public static final String HOST = "localhost";
    private final ProtocolProperties protocolProperties;
    private final H02ProtocolMessageHandler h02ProtocolMessageHandler;

    @Override
    public String getName() {
        return "H02";
    }

    @Override
    public void initServers(List<Server> serverList) {
        ProtocolConfigDTO configDTO = protocolProperties.getConfig(getName());
        serverList.add(new Server(new ServerBootstrap(), HOST, configDTO.getPort()) {
            @Override
            protected void addProtocolHandlers(ChannelPipeline pipeline) {
                pipeline.addLast("object_decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                pipeline.addLast(H_02_MESSAGE_HANDLER, h02ProtocolMessageHandler);
            }

            @Override
            protected String getProtocolName() {
                return getName();
            }
        });
    }
}
