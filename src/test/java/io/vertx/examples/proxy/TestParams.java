package io.vertx.examples.proxy;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class TestParams extends TestBase {

    @Override
    protected void createRequestHandler() {
        testServer.requestHandler(request -> {
	        StringJoiner joiner = new StringJoiner("&");
	        request.params().forEach(header -> {
		        joiner.add(header.getKey() + "=" + header.getValue());
	        });
            request.response().end(joiner.toString());
        });
    }

    @Test
    public void requestParamsForwarded(TestContext context) {
        Async async = context.async();
	    String params = "param1=value1&param2=value2";
        client().getNow("/test?" + params, response -> {
            assertEquals(200, response.statusCode());
            response.bodyHandler(buff -> {
	            context.assertEquals(params, buff.toString("UTF-8"));
                async.complete();
            });
        });
    }

}
