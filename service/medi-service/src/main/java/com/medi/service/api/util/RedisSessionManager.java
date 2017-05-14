package com.medi.service.api.util;

import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RedisSessionManager {
    final static int MAX_RETRY_TIME = 10;

    protected String host = "localhost";
    protected int port = 6379;
    protected Integer database;
    protected String password = null;
    protected int timeout = Protocol.DEFAULT_TIMEOUT;
    protected String sentinelMaster = null;
    protected Set<String> sentinelSet = null;

    protected Pool<Jedis> connectionPool;

    protected JedisPoolConfig connectionPoolConfig = new JedisPoolConfig();

    public void initializeConnection() throws RedisException {
        try {
            if (getSentinelMaster() != null) {
                Set<String> sentinelSet = getSentinelSet();
                if (sentinelSet != null && sentinelSet.size() > 0) {
                    connectionPool = new JedisSentinelPool(getSentinelMaster(), sentinelSet, this.connectionPoolConfig, getTimeout(), getPassword());
                } else {
                    throw new RedisException(
                            "Error configuring Redis Sentinel connection pool: expected both `sentinelMaster` and `sentiels` to be configured");
                }
            } else {
                connectionPool = new JedisPool(this.connectionPoolConfig, getHost(), getPort(), getTimeout(), getPassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RedisException("Error connecting to Redis", e);
        }
    }

    public void destroyConnection() {
        connectionPool.destroy();
    }

    public Jedis acquireConnection() throws RedisException {
        if (getDatabase() != null) {
            Jedis jedis = getConnection(getDatabase());
            return jedis;
        }
        throw new RedisException("Database is not set.");
    }

    public Jedis acquireConnection(int db) throws RedisException {
        Jedis jedis = getConnection(db);
        return jedis;
    }

    private Jedis getConnection(int db) throws RedisException {
        boolean isSuccessful = false;
        int retryTimes = 0;
        while (!isSuccessful && retryTimes < MAX_RETRY_TIME) {
            try {
                Jedis jedis = connectionPool.getResource();
                jedis.select(db);
                isSuccessful = true;
                return jedis;
            } catch (Exception e) {
                // retry
                try {
                    retryTimes++;
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {//
                }
            }
        }
        throw new RedisException("Unable to aquire redis connection.");
    }

    public void returnConnection(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public void setConfig(JedisPoolConfig connectionPoolConfig) {
        this.connectionPoolConfig = connectionPoolConfig;
    }

    public JedisPoolConfig getConfig() {
        return connectionPoolConfig;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Integer getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSentinels() {
        if (sentinelSet == null) return "";
        StringBuilder sentinels = new StringBuilder();
        for (Iterator<String> iter = this.sentinelSet.iterator(); iter.hasNext(); ) {
            sentinels.append(iter.next());
            if (iter.hasNext()) {
                sentinels.append(",");
            }
        }
        return sentinels.toString();
    }

    public void setSentinels(String sentinels) {
        if (null == sentinels) {
            sentinels = "";
        }

        String[] sentinelArray = sentinels.split(",");
        this.sentinelSet = new HashSet<String>(Arrays.asList(sentinelArray));
    }

    public Set<String> getSentinelSet() {
        return this.sentinelSet;
    }

    public String getSentinelMaster() {
        return this.sentinelMaster;
    }

    public void setSentinelMaster(String master) {
        this.sentinelMaster = master;
    }

    public int getConnectionPoolMaxTotal() {
        return this.connectionPoolConfig.getMaxTotal();
    }

    public void setConnectionPoolMaxTotal(int connectionPoolMaxTotal) {
        this.connectionPoolConfig.setMaxTotal(connectionPoolMaxTotal);
    }
}
