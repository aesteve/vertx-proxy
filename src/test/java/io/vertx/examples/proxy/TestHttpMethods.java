package io.vertx.examples.proxy;

import static org.junit.Assert.assertEquals;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TestHttpMethods extends TestBase {

    protected final static String METHOD_HEADER = "X-Original-Method";

    @Override
    protected void createRequestHander() {
        testServer.requestHandler(request -> {
            request.response().putHeader(METHOD_HEADER, request.method().toString());
            request.response().end();
        });
    }

    @Test
    public void everyMethodIsForwardedAsIs() throws Exception {
        CountDownLatch latch = new CountDownLatch(HttpMethod.values().length);
        for (HttpMethod method : HttpMethod.values()) {
            client().request(method, "/test", response -> {
                assertEquals(200, response.statusCode());
                assertEquals(method.toString(), response.getHeader(METHOD_HEADER));
                latch.countDown();
            }).end();
        }
        latch.await();
    }

}
