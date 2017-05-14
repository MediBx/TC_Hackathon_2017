package com.medi.service.api.util;

public class RedisException extends Exception {
    public RedisException(String err) {
        super(err);
    }

    public RedisException(String err, Exception e) {
        super(err, e);
    }
}
