package com.medi.service.api.http;

import io.netty.channel.ChannelInitializer;

public interface ServerConnector {
    int getPort();

    ChannelInitializer<?> getChannelInitializer();
}