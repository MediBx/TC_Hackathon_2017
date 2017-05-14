package com.medi.service.api.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class HttpServer extends BasicNettyServer {
    public static final String LISTEN_PORT_NAME = "listenPort";

    @Inject
    public HttpServer(final Injector injector, @Named(LISTEN_PORT_NAME) int port) {
        super(HttpServerConnector.builder()
                .port(port)
                .injector(injector)
                .build());
    }

    public HttpServer(final ServerConnector connector) {
        super(connector);
    }

    public HttpServer() {
        super(HttpServerConnector.builder()
                .port(8080)
                .build());
    }
}