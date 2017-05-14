package com.medi.service.api.util;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class RedisConnector {
    public static final String REDIS_HOST = "redisHost";
    public static final String REDIS_PORT = "redisPort";
    public static final String REDIS_DB = "redisDb";
    private final RedisSessionManager sessionManager;
    private boolean isConnected = false;

    @Inject
    public RedisConnector(@Named(REDIS_HOST) String host, @Named(REDIS_PORT) int port, @Named(REDIS_DB) int db) {
        this(new RedisSessionManager());
        this.setHost(host);
        this.setPort(port);
        this.setDbIndex(db);
    }

    public RedisConnector(final RedisSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Set Redis server host
     *
     * @param host
     */
    public RedisConnector setHost(String host) {
        sessionManager.setHost(host);
        return this;
    }

    /**
     * Set Redis server port
     *
     * @param port
     */
    public RedisConnector setPort(int port) {
        sessionManager.setPort(port);
        return this;
    }

    /**
     * Set max connection pool size
     *
     * @param connPoolSize
     */
    public RedisConnector setConnPoolSize(int connPoolSize) {
        sessionManager.setConnectionPoolMaxTotal(connPoolSize);
        return this;
    }

    /**
     * Set database index
     *
     * @param dbIndex
     */
    public RedisConnector setDbIndex(int dbIndex) {
        sessionManager.setDatabase(dbIndex);
        return this;
    }

    /**
     * Set Sentinel master (if use sentinel mode)
     *
     * @param sentinelMaster
     */
    public RedisConnector setSentinelMaster(String sentinelMaster) {
        sessionManager.setSentinelMaster(sentinelMaster);
        return this;
    }

    /**
     * Set Sentinel cluster (if use sentinel mode)
     *
     * @param sentinelSet
     */
    public RedisConnector setSentinelSet(String sentinelSet) {
        sessionManager.setSentinels(sentinelSet);
        return this;
    }

    /**
     * Set connection timeout
     *
     * @param timeout
     */
    public RedisConnector setTimeout(int timeout) {
        sessionManager.setTimeout(timeout);
        return this;
    }

    /**
     * Set Jedis config
     *
     * @param config
     */
    public RedisConnector setConfig(JedisPoolConfig config) {
        sessionManager.setConfig(config);
        return this;
    }

    public String getHost() {
        return sessionManager.getHost();
    }

    public int getPort() {
        return sessionManager.getPort();
    }

    public int getConnPoolSize() {
        return sessionManager.getConnectionPoolMaxTotal();
    }

    public Integer getDbIndex() {
        return sessionManager.getDatabase();
    }

    public String getSentinelMaster() {
        return sessionManager.getSentinelMaster();
    }

    public String getSentinelSet() {
        return sessionManager.getSentinels();
    }

    public JedisPoolConfig getConfig() {
        return sessionManager.getConfig();
    }

    /**
     * Get Redis connection information
     *
     * @return
     */
    public String getInfo() {
        return "RedisConnector{" +
                "host='" + getHost() + '\'' +
                ", port=" + getPort() +
                ", dbIndex=" + getDbIndex() +
                ", sentinelSet='" + getSentinelSet() + '\'' +
                ", sentinelMaster='" + getSentinelMaster() + '\'' +
                ", connPoolSize=" + getConnPoolSize() +
                '}';
    }

    /**
     * Connect to Redis
     *
     * @throws RedisException
     */
    public RedisConnector connect() throws RedisException {
        if (!isConnected) {
            if (sessionManager.getHost() == null) {
                throw new RedisException("Please specify Redis host.");
            }
            if (sessionManager.getDatabase() == null) {
                throw new RedisException("Please specify Redis DB to use.");
            }

            log.info("Connecting to redis: " + getInfo());
            sessionManager.initializeConnection();
            isConnected = true;
            log.info("Redis is connected.");
        }
        return this;
    }

    /**
     * Close Redis connection
     */
    public void close() {
        sessionManager.destroyConnection();
    }

    private void checkConn() throws RedisException {
        if (!isConnected) {
            throw new RedisException("Redis is not connected yet.");
        }
    }

    /**
     * Lambda scan
     *
     * @param fnScan
     * @param fnProc
     * @param <T>
     * @param <R>
     * @return
     * @throws RedisException
     */
    public <T, R> List<R> scanWrap(final RedisBiFunction<RedisConnector, String, ScanResult<T>> fnScan, final Function<T, R> fnProc) throws RedisException {
        List<R> ret = new ArrayList<>();
        String cur = ScanParams.SCAN_POINTER_START;
        boolean cycleIsFinished = false;
        while (!cycleIsFinished) {
            ScanResult<T> scanResult = fnScan.apply(this, cur);
            ret.addAll(scanResult.getResult().stream().map(i -> fnProc.apply(i)).collect(Collectors.toList()));
            cur = scanResult.getStringCursor();
            cycleIsFinished = cur.equals(ScanParams.SCAN_POINTER_START);
        }
        return ret;
    }

    /**
     * Sets data in string
     *
     * @param key
     * @param data
     * @throws RedisException
     */
    public void set(String key, String data) throws RedisException {
        wrapper(r -> r.set(key, data));
    }

    /**
     * Removes the key
     *
     * @param key
     * @throws RedisException
     */
    public void del(String key) throws RedisException {
        wrapper(r -> r.del(key));
    }

    /**
     * Check if key exists
     *
     * @param key
     * @return true, if key exists
     * @throws RedisException
     */
    public boolean exists(String key) throws RedisException {
        return wrapperWithRet(r -> r.exists(key));
    }

    /**
     * Gets the data
     *
     * @param key
     * @return string data
     * @throws RedisException
     */
    public String get(String key) throws RedisException {
        return wrapperWithRet(r -> r.get(key));
    }

    /**
     * Scan
     *
     * @param cursor
     * @return the scan result
     * @throws RedisException
     */
    public ScanResult<String> scan(String cursor) throws RedisException {
        return wrapperWithRet(r -> r.scan(cursor));
    }

    /**
     * Sets the section data
     *
     * @param key   of the section
     * @param field field in the section
     * @param data  the data
     * @throws RedisException
     */
    public void hset(String key, String field, String data) throws RedisException {
        wrapper(r -> r.hset(key, field, data));
    }

    /**
     * Checks if field exists in the section
     *
     * @param key   key of the section
     * @param field field in the section
     * @return true, field exists in the section
     * @throws RedisException
     */
    public boolean hexists(String key, String field) throws RedisException {
        return wrapperWithRet(r -> r.hexists(key, field));
    }

    /**
     * Gets section length of the session
     *
     * @param key key of the section
     * @return the section length
     * @throws RedisException
     */
    public Long hlen(String key) throws RedisException {
        return wrapperWithRet(r -> r.hlen(key));
    }

    /**
     * Gets data in string from the section
     *
     * @param key   key of the section
     * @param field field in the section
     * @return the data
     * @throws RedisException
     */
    public String hget(String key, String field) throws RedisException {
        return wrapperWithRet(r -> r.hget(key, field));
    }

    /**
     * Scan data in the section
     *
     * @param key key of the section
     * @return scan result
     * @throws RedisException
     */
    public ScanResult<Entry<String, String>> hscan(String key, String cursor) throws RedisException {
        return wrapperWithRet(r -> r.hscan(key, cursor));
    }

    /**
     * Delete data from the section
     *
     * @param key
     * @param field
     * @throws RedisException
     */
    public void hdel(String key, String field) throws RedisException {
        wrapper(r -> r.hdel(key, field));
    }

    /**
     * Checks if is member of set
     *
     * @param key
     * @param member
     * @return true, if is member of set
     * @throws RedisException
     */
    public boolean sismember(String key, String member) throws RedisException {
        return wrapperWithRet(r -> r.sismember(key, member));
    }

    /**
     * Adds members to set
     *
     * @param key
     * @param members
     * @throws RedisException
     */
    public void sadd(String key, String... members) throws RedisException {
        wrapper(r -> r.sadd(key, members));
    }

    /**
     * Removes members from set
     *
     * @param key
     * @param members
     * @throws RedisException
     */
    public void srem(String key, String... members) throws RedisException {
        wrapper(r -> r.srem(key, members));
    }

    /**
     * Scan set
     *
     * @param key
     * @param cursor
     * @return the scan result
     * @throws RedisException
     */
    public ScanResult<String> sscan(String key, String cursor) throws RedisException {
        return wrapperWithRet(r -> r.sscan(key, cursor));
    }

    /**
     * Adds member to sorted set
     *
     * @param key
     * @param member
     * @param score
     * @throws RedisException
     */
    public void zadd(String key, String member, double score) throws RedisException {
        wrapper(r -> r.zadd(key, score, member));
    }

    /**
     * Gets members from sorted set by score
     *
     * @param key
     * @param min
     * @param max
     * @return members
     * @throws RedisException
     */
    public Set<String> zrangeByScore(String key, double min, double max) throws RedisException {
        return wrapperWithRet(r -> r.zrangeByScore(key, min, max));
    }

    /**
     * Size of the sorted set
     *
     * @param key
     * @return
     * @throws RedisException
     */
    public Long zcard(String key) throws RedisException {
        return wrapperWithRet(r -> r.zcard(key));
    }

    /**
     * Peek the first element of sorted set
     *
     * @param key
     * @return
     * @throws RedisException
     */
    public String zpeek(String key) throws RedisException {
        return wrapperWithRet(r -> r.zrange(key, 0, 0)).iterator().next();
    }

    /**
     * Peek the first element of sorted set with score
     *
     * @param key
     * @return
     * @throws RedisException
     */
    public Tuple zpeekWithScore(String key) throws RedisException {
        return wrapperWithRet(r -> r.zrangeWithScores(key, 0, 0)).iterator().next();
    }

    /**
     * Left pop from sorted set
     *
     * @param key
     * @return
     * @throws RedisException
     */
    public String zlpop(String key) throws RedisException {
        String front = wrapperWithRet(r -> r.zrange(key, 0, 0)).iterator().next();
        wrapper(r -> r.zremrangeByRank(key, 0, 0));
        return front;
    }

    /**
     * Right pop from sorted set
     *
     * @param key
     * @return
     * @throws RedisException
     */
    public String zrpop(String key) throws RedisException {
        String tail = wrapperWithRet(r -> r.zrange(key, -1, -1)).iterator().next();
        wrapper(r -> r.zremrangeByRank(key, -1, -1));
        return tail;
    }

    // Wrapper

    /**
     * Wrap for jedis void methods
     *
     * @param f
     * @throws RedisException
     */
    public void wrapper(Consumer<Jedis> f) throws RedisException {
        checkConn();
        Jedis r = null;
        try {
            r = sessionManager.acquireConnection();
            f.accept(r);
        } finally {
            sessionManager.returnConnection(r);
        }
    }

    /**
     * Wrap for jedis methods with return
     *
     * @param f
     * @param <T>
     * @return
     * @throws RedisException
     */
    public <T> T wrapperWithRet(Function<Jedis, T> f) throws RedisException {
        checkConn();
        Jedis r = null;
        try {
            r = sessionManager.acquireConnection();
            T ret = f.apply(r);
            return ret;
        } finally {
            sessionManager.returnConnection(r);
        }
    }

    @FunctionalInterface
    public interface RedisBiFunction<T1, T2, R> {
        R apply(T1 t1, T2 t2) throws RedisException;
    }
}
