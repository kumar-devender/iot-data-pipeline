package com.relay42;

import com.relay42.config.DeviceConfig;
import com.relay42.datasetbuilder.DataSetBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Device {
    private final DataSetBuilder dataSetBuilder;
    private DeviceConfig deviceConfig;
    private final Channel channel;
    private boolean running = false;
    private ScheduledExecutorService scheduledThreadPool;

    public Device(String protocol, DeviceConfig deviceConfig, DataSetBuilder dataSetBuilder) {
        this.dataSetBuilder = dataSetBuilder;
        this.deviceConfig = deviceConfig;
        log.info("Initializing device [{}] in group [{}] ", deviceConfig.getDeviceId(), protocol);
        ChannelFactory factory = new NioClientSocketChannelFactory();
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        ChannelGroup group = new DefaultChannelGroup(protocol);
        bootstrap.getPipeline().addLast("handler", new SimpleChannelDownstreamHandler());
        bootstrap.getPipeline().addLast("upstream_handler", new SimpleChannelUpstreamHandler());
        bootstrap.getPipeline().addLast("encoder", new ObjectEncoder());

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("connectTimeoutMillis", 100);
        channel = bootstrap.connect(new InetSocketAddress(this.deviceConfig.getRemoteHost(), this.deviceConfig.getRemotePort())).awaitUninterruptibly().getChannel();
        group.add(channel);
        scheduledThreadPool = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        if (running) {
            log.info("Device is already running");
            return;
        }
        scheduledThreadPool.scheduleAtFixedRate(() -> sendData(), 0, this.deviceConfig.getDataTransmissionFreqInSecond(), TimeUnit.SECONDS);
        running = true;
    }

    public void stop() {
        scheduledThreadPool.shutdown();
        running = false;
    }

    public void sendData() {
        if (running && channel.isOpen()) {
            dataSetBuilder.buildData(deviceConfig.getDeviceId())
                    .forEach(packet -> {
                        channel.write(packet);
                        log.debug("Sent packet .....");
                    });
        }
    }
}
