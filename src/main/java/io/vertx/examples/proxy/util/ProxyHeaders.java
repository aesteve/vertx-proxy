package io.vertx.examples.proxy.util;

public enum ProxyHeaders {
    X_FORWARDED_FOR("X-Forwarded-For"), X_FORWARDED_HOST("X-Forwarded-Host");

    private String representation;

    private ProxyHeaders(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return representation;
    }
}
