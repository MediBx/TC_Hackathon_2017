package com.medi.service.api.http;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by nanxiao on 9/3/16.
 */
public final class DefaultHttpResponder extends HttpResponder {
    @Override
    protected boolean haveMatchingResource(HttpRequest request) {
        return true;
    }

    @Override
    protected boolean isSupportedMethod(HttpRequest request) {
        return true;
    }

    @Override
    protected FullHttpResponse generateResponse(HttpRequest request) {
        return createSuccessResponse("Success!");
    }
}
