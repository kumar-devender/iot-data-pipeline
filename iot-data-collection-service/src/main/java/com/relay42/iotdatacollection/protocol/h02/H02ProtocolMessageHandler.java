package com.relay42.iotdatacollection.protocol.h02;

import com.relay42.dto.H02DTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class H02ProtocolMessageHandler extends SimpleChannelUpstreamHandler {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${relay42.iotdatacollection.protocol.config.H02.topic:H02}")
    private String topic;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object messageObject = e.getMessage();
        if (messageObject instanceof H02DTO) {
            H02DTO h02DTO = (H02DTO) messageObject;
            log.debug("Converted object to [{}]", h02DTO);
            kafkaTemplate.send(topic, h02DTO);
        }
        super.messageReceived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

        log.debug(e.getCause().toString());

        super.exceptionCaught(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Device :: " + e.getChannel().getRemoteAddress() + " has connected!");

        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        log.info("Device :: " + e.getChannel().getRemoteAddress() + " has disconnected from the Server.");

        super.channelDisconnected(ctx, e);
    }

}