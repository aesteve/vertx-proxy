package io.vertx.examples.proxy;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class TestRequestBody extends TestBase {

    private final static Random rand = new Random(new Date().getTime());
    private final static String CHARACTERS = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    protected void createRequestHandler() {
        testServer.requestHandler(request -> {
            HttpServerResponse response = request.response();
            response.setChunked(true);
            response.setStatusCode(200);
            Pump.pump(request, response).start();
            request.endHandler(voidz -> response.end());
        });
    }

    @Test
    public void testBody(TestContext context) {
        Async async = context.async();
        StringBuilder sentContent = new StringBuilder();
        HttpClientRequest request = client().post("/test", response -> {
            assertEquals(200, response.statusCode());
            Buffer buff = Buffer.buffer();
            response.handler(buff::appendBuffer);
            response.endHandler(voidz -> {
                assertEquals(buff.toString("UTF-8"), sentContent.toString());
                async.complete();
            });
        });
        request.setChunked(true);
        for (int i = 0; i < 1000; i++) {
            String randomString = randomString(10000);
            request.write(randomString);
            sentContent.append(randomString);
        }
        request.end();
    }

    private String randomString(int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = CHARACTERS.charAt(rand.nextInt(CHARACTERS.length()));
        }
        return new String(text);
    }
}
