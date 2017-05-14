package com.medi.service.api;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.medi.service.api.http.HttpResponder;
import com.medi.service.api.http.HttpServer;
import com.medi.service.api.util.MySqlConfig;
import com.medi.service.api.util.MySqlConnectorFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MediServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        Config rootConfig = ConfigFactory.load();
        // Http Responder
        bind(HttpResponder.class).to(MediServiceResponder.class);
        // Server Port
        int serverPort = rootConfig.getInt("server.port");
        bind(Integer.class).annotatedWith(Names.named(HttpServer.LISTEN_PORT_NAME))
                .toInstance(serverPort);
        // MySql Configuration
        MySqlConfig mysqlConf = new MySqlConfig(rootConfig);
        bind(MySqlConnectorFactory.class)
                .toInstance(new MySqlConnectorFactory(mysqlConf.host(), mysqlConf.port(), mysqlConf.schema(), mysqlConf.user(), mysqlConf.password()));
    }
}