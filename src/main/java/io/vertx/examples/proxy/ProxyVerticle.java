package io.vertx.examples.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.examples.proxy.handler.ProxyHandler;

public class ProxyVerticle extends AbstractVerticle {

    private final static Logger log = LoggerFactory.getLogger(ProxyVerticle.class);
    private final static String DEFAULT_HOST = "localhost";
    private final static Integer DEFAULT_PORT = 8080;

    private HttpServer server;
    private HttpServerOptions options;

    @Override
    public void start(Future<Void> future) {
        log.info("Starting proxy");
        createServerOptions();
        server = vertx.createHttpServer(options);
        ProxyHandler proxy;
        try {
            proxy = new ProxyHandler(vertx, context.config().getJsonObject("client"));
        } catch (Throwable t) {
            future.fail(t);
            return;
        }
        server.requestHandler(proxy::forward);
        server.listen(res -> {
            if (res.failed()) {
                future.fail(res.cause());
            } else {
                log.info("...Proxy server started");
                future.complete();
            }
        });
    }

    @Override
    public void stop(Future<Void> future) {
        log.info("Closing proxy server");
        if (server == null) {
            future.complete();
            return;
        }
        server.close(res -> {
            if (res.failed()) {
                log.error("Could not close web server", res.cause());
            }
            log.info("Proxy server closed");
            future.complete();
        });
    }

    private void createServerOptions() {
        options = new HttpServerOptions();
        JsonObject config = context.config();
        JsonObject serverConfig = config.getJsonObject("server");
        if (serverConfig == null) {
            options.setHost(DEFAULT_HOST);
            options.setPort(DEFAULT_PORT);
            return;
        }
        options.setHost(serverConfig.getString("host", DEFAULT_HOST));
        options.setPort(serverConfig.getInteger("port", DEFAULT_PORT));
        /*
         * README : add your options there. For example :
         * clientAuthRequired, ssl configuration, ...
         */
    }

}
