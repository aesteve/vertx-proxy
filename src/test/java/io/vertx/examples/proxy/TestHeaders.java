package io.vertx.examples.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpClientRequest;
import static io.vertx.core.http.HttpHeaders.*;
import static org.junit.Assert.*;
import io.vertx.core.json.JsonObject;
import static io.vertx.examples.proxy.util.ProxyHeaders.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class TestHeaders extends TestBase {

    private final static String ASKED_HOST = "http://vert-x3.github.io";
    private final static String CUSTOM_HEADER_NAME = "X-Custom-Header";
    private final static String CUSTOM_HEADER_VALUE = "custom value";

    /**
     * A simple server that puts in response body the list of non standard headers it received
     * Note : non-standard headers usually start with "X-"
     */
    @Override
    protected void createRequestHandler() {
        testServer.requestHandler(request -> {
            JsonObject headers = new JsonObject();
            request.headers().forEach(header -> {
                if (header.getKey().startsWith("X-")) {
                    headers.put(header.getKey(), header.getValue());
                }
            });
            request.response().end(headers.toString());
        });
    }

    /**
     * When a request is made by the proxy, the following headers should be set :
     * - X-Forwarded-For
     * - X-Forwarded-Host
     */
    @Test
    public void proxyHeadersAreSet(TestContext context) {
        Async async = context.async();
        HttpClientRequest request = client().get("/test", response -> {
            assertEquals(200, response.statusCode());
            response.bodyHandler(buff -> {
                JsonObject json = new JsonObject(buff.toString());
                assertEquals(ASKED_HOST, json.getString(X_FORWARDED_HOST.toString()));
                assertEquals("127.0.0.1", json.getString(X_FORWARDED_FOR.toString()));
                async.complete();
            });
        });
        request.putHeader(HOST.toString(), ASKED_HOST);
        request.end();
    }

    /**
     * The request headers the client has originally sent should be forwarded as is
     */
    @Test
    public void clientHeadersAreForwarded(TestContext context) {
        Async async = context.async();
        String path = "/test";
        client().get(path, response -> {
            assertEquals(200, response.statusCode());
            response.bodyHandler(buff -> {
                JsonObject json = new JsonObject(buff.toString());
                assertEquals(CUSTOM_HEADER_VALUE, json.getString(CUSTOM_HEADER_NAME));
                async.complete();
            });
        }).putHeader(CUSTOM_HEADER_NAME, CUSTOM_HEADER_VALUE).end();
    }

}
