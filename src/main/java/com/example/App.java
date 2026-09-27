package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class App {
    static final int DEFAULT_PORT = 8080;

    private App() {
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = start(portFrom(System.getenv("PORT")));
        System.out.println("listening on " + server.getAddress().getPort());
    }

    static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        return server;
    }

    static int portFrom(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(raw.trim());
    }
}
