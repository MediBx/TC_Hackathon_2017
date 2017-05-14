package com.medi.service.api;

import com.google.inject.Inject;
import com.medi.service.api.http.HttpResponder;
import com.medi.service.api.impl.Router;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;
import java.util.function.Function;

public final class MediServiceResponder extends HttpResponder {
    private final Map<String, Function<HttpRequest, String>> funcMap;

    @Inject
    public MediServiceResponder(final Router router) {
        funcMap = router.getFuncMap();
    }

    @Override
    protected boolean haveMatchingResource(HttpRequest request) {
        return true;
    }

    @Override
    protected boolean isSupportedMethod(HttpRequest request) {
        return funcMap.containsKey(request.uri());
    }

    @Override
    protected FullHttpResponse generateResponse(HttpRequest request) {
        try {
            String response = funcMap.get(request.uri()).apply(request);
            return createSuccessResponse(response);
        } catch (Exception e) {
            return createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

