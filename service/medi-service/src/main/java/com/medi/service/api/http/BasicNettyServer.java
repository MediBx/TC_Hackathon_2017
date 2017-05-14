package com.medi.service.api.http;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;

public class BasicNettyServer implements Server {
    private final List<ServerConnector> connectors;
    private List<ChannelFuture> channels = Lists.newArrayList();
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;

    public BasicNettyServer(final ServerConnector connector) {
        this(Lists.newArrayList(connector));
    }

    public BasicNettyServer(final List<ServerConnector> connectors) {
        this.connectors = connectors;
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
    }

    public void start() throws ServerException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        try {
            // for each connector, build a bootstrap,
            // start and save the ChannelFuture
            for (final ServerConnector connector : connectors) {
                final ServerBootstrap bootstrap =
                        new ServerBootstrap()
                                .group(masterGroup, slaveGroup)
                                .channel(NioServerSocketChannel.class)
                                .childHandler(connector.getChannelInitializer())
                                .option(ChannelOption.SO_BACKLOG, 128)
                                .childOption(ChannelOption.SO_KEEPALIVE, true);
                channels.add(bootstrap.bind(connector.getPort()).sync());
            }
        } catch (Exception e) {
            throw new ServerException("Failed to start server. Reason: " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();

        for (final ChannelFuture channel : channels) {
            try {
                channel.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}