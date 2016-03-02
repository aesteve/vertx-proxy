package io.vertx.examples.proxy.handler;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.HOST;
import static io.vertx.examples.proxy.util.ProxyHeaders.X_FORWARDED_FOR;
import static io.vertx.examples.proxy.util.ProxyHeaders.X_FORWARDED_HOST;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;

public class ProxyHandler {

    private final static Integer DEFAULT_PORT = 80;
    // README : be careful on how you dimension your connection pool
    private final static Integer DEFAULT_MAX_POOL_SIZE = 100;

    private JsonObject clientConfig;
    private HttpClientOptions options;
    private Vertx vertx;
    private HttpClient client;

    public ProxyHandler(Vertx vertx, JsonObject clientConfig) {
        this.vertx = vertx;
        this.clientConfig = clientConfig;
        checkConfig();
    }

    private void checkConfig() {
        if (clientConfig == null) {
            throw new IllegalArgumentException("The verticle configuration should contain a \"client\" configuration");
        }
        if (clientConfig.getString("host") == null) {
            throw new IllegalArgumentException("You should specify a host to forward incoming requests too, eg: {\"client\":{\"host\":\"com.foo.bar\"}}. Config is : " + clientConfig.toString());
        }
        options = new HttpClientOptions();
        options.setDefaultHost(clientConfig.getString("host"));
        options.setDefaultPort(clientConfig.getInteger("port", DEFAULT_PORT));
        options.setMaxPoolSize(clientConfig.getInteger("max-pool-size", DEFAULT_MAX_POOL_SIZE));
        options.setKeepAlive(true);
        /*
         * README : add your client options here, like : ssl configuration,
         * tcpkeepalive, ...
         */
    }

    private void createClient() {
        client = vertx.createHttpClient(options);
    }

    public void forward(HttpServerRequest incomingRequest) {
        if (client == null) {
            createClient();
        }
        HttpServerResponse proxyResponse = incomingRequest.response();
        proxyResponse.setChunked(true);
        HttpClientRequest outgoingRequest = client.request(incomingRequest.method(), incomingRequest.absoluteURI(), response -> {
            proxyResponse.setStatusCode(response.statusCode());
            proxyResponse.setStatusMessage(response.statusMessage());
            response.headers().forEach(header -> {
                if (!header.getKey().equals(CONTENT_LENGTH.toString())) {
                    proxyResponse.putHeader(header.getKey(), header.getValue());
                }
            });
            Pump pump = Pump.pump(response, proxyResponse);
            response.endHandler(voidz -> {
                // pump.stop();
                proxyResponse.end();
            });
            pump.start();
        });
        outgoingRequest.setChunked(true);
        incomingRequest.headers().forEach(header -> {
            if (!header.getKey().equals(CONTENT_LENGTH.toString())) {
                outgoingRequest.putHeader(header.getKey(), header.getValue());
            }
        });
        outgoingRequest.putHeader(X_FORWARDED_FOR.toString(), incomingRequest.remoteAddress().host());
        outgoingRequest.putHeader(X_FORWARDED_HOST.toString(), incomingRequest.getHeader(HOST.toString()));
        // README : add custom headers here if you need to
        Pump.pump(incomingRequest, outgoingRequest).start();
        incomingRequest.endHandler(voidz -> {
            // pump.stop();
            outgoingRequest.end();
        });

    }
}
