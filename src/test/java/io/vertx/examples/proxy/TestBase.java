package io.vertx.examples.proxy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import org.junit.After;
import org.junit.Before;

public abstract class TestBase {

    protected final static String TEST_SERVER_HOST = "localhost";
    protected final static Integer TEST_SERVER_PORT = 9191;

    protected final static String PROXY_HOST = "localhost";
    protected final static Integer PROXY_PORT = 9090;

    protected Vertx vertx;
    protected HttpServer testServer;

    @Before
    public void setupProxy(TestContext context) {
        if (vertx == null) {
            vertx = Vertx.vertx();
        }
        JsonObject config = new JsonObject();
        JsonObject serverOptions = new JsonObject();
        serverOptions.put("host", PROXY_HOST);
        serverOptions.put("port", PROXY_PORT);
        JsonObject clientOptions = new JsonObject();
        clientOptions.put("host", TEST_SERVER_HOST);
        clientOptions.put("port", TEST_SERVER_PORT);
        config.put("server", serverOptions);
        config.put("client", clientOptions);
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(config);
        // options.setInstances(4);
        vertx.deployVerticle(ProxyVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @Before
    public void setupTestServer(TestContext context) {
        if (vertx == null) {
            vertx = Vertx.vertx();
        }
        testServer = vertx.createHttpServer(testServerOptions());
        createRequestHandler();
        testServer.listen(context.asyncAssertSuccess());
    }

    @After
    public void tearDownEverything(TestContext context) {
        if (vertx != null) {
            vertx.close(context.asyncAssertSuccess());
        }
    }

    protected HttpServerOptions testServerOptions() {
        HttpServerOptions options = new HttpServerOptions();
        options.setHost(TEST_SERVER_HOST);
        options.setPort(TEST_SERVER_PORT);
        return options;
    }

    protected HttpClient client() {
        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultHost(PROXY_HOST);
        options.setDefaultPort(PROXY_PORT);
        return vertx.createHttpClient(options);
    }

    abstract protected void createRequestHandler();
}
