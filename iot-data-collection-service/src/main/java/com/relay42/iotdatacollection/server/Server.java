package com.relay42.iotdatacollection.server;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;

@Slf4j
public abstract class Server {
    private final int port;
    private final Bootstrap bootstrap;
    private final String address;
    private final ChannelGroup allChannels = new DefaultChannelGroup();

    public ChannelGroup getChannelGroup() {
        return allChannels;
    }

    public Server(Bootstrap bootstrap, String address, int port) {
        this.bootstrap = bootstrap;
        if (bootstrap instanceof ServerBootstrap) {
            bootstrap.setFactory(new NioServerSocketChannelFactory());
        } else if (bootstrap instanceof ConnectionlessBootstrap) {
            bootstrap.setFactory(new NioDatagramChannelFactory());
        }

        this.address = address;
        this.port = port;

        bootstrap.setPipelineFactory(() -> {
            ChannelPipeline pipeline = Channels.pipeline();
            Server.this.addProtocolHandlers(pipeline);
            return pipeline;
        });

    }

    protected abstract void addProtocolHandlers(ChannelPipeline pipeline);

    protected abstract String getProtocolName();

    public void start() {
        log.info("Initializing server for protocol [{}]", getProtocolName());
        InetSocketAddress endpoint = new InetSocketAddress(address, port);
        Channel channel = null;
        if (bootstrap instanceof ServerBootstrap) {
            channel = ((ServerBootstrap) bootstrap).bind(endpoint);
        } else if (bootstrap instanceof ConnectionlessBootstrap) {
            channel = ((ConnectionlessBootstrap) bootstrap).bind(endpoint);
        }

        if (channel != null) {
            getChannelGroup().add(channel);
        }
    }

    public void stop() {
        ChannelGroupFuture future = getChannelGroup().close();
        future.awaitUninterruptibly();
    }
}
