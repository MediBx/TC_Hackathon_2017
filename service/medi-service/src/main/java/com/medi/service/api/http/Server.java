package com.medi.service.api.http;

public interface Server {
    void start() throws ServerException;

    void shutdown();
}