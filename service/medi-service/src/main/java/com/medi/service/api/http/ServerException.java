package com.medi.service.api.http;

/**
 * Created by nan on 1/2/2017.
 */
public class ServerException extends Exception {
    public ServerException(String err, Throwable e) {
        super(err, e);
    }
}
