package com.medi.service.api.util;

import com.typesafe.config.Config;

public class RedisConfig {
    private final Config conf;

    public RedisConfig(final Config root) {
        this.conf = root.getConfig("redis.config");
    }

    public String host() {
        return conf.getString("host");
    }

    public int port() {
        return conf.getInt("port");
    }

    public int db(String name) {
        return conf.getInt("db." + name);
    }
}
