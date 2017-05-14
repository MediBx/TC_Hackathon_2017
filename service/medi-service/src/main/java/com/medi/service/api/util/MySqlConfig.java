package com.medi.service.api.util;

import com.typesafe.config.Config;

/**
 * Created by nan on 10/8/2016.
 */
public class MySqlConfig {
    private final Config conf;

    public MySqlConfig(final Config root) {
        this.conf = root.getConfig("mysql.config");
    }

    public String host() {
        return conf.getString("host");
    }

    public int port() {
        return conf.getInt("port");
    }

    public String schema() {
        return conf.getString("schema");
    }

    public String user() {
        return conf.getString("user");
    }

    public String password() {
        return conf.getString("pw");
    }
}
