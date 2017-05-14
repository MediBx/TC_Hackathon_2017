package com.medi.service.api;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.medi.service.api.http.HttpServer;
import com.medi.service.api.http.Server;
import com.medi.service.api.http.ServerException;

public class MediServiceRunner {
    final Injector injector;

    @Inject
    public MediServiceRunner(final Injector injector) {
        this.injector = injector;
    }

    public void start() throws ServerException {
        Server server = injector.getInstance(HttpServer.class);
        server.start();
    }

    public static void main(final String[] args) throws ServerException {
        Guice.createInjector(new MediServiceModule())
                .getInstance(MediServiceRunner.class)
                .start();
    }
}
