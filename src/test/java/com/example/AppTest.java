package com.example;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    @Test
    void healthRespondsOk() throws Exception {
        HttpServer server = App.start(0);
        try {
            int port = server.getAddress().getPort();
            HttpURLConnection connection = (HttpURLConnection) URI.create("http://127.0.0.1:" + port + "/health")
                    .toURL()
                    .openConnection();
            assertEquals(200, connection.getResponseCode());
            String body = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("ok", body);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void missingPortUsesDefault() {
        assertEquals(App.DEFAULT_PORT, App.portFrom(null));
        assertEquals(App.DEFAULT_PORT, App.portFrom("  "));
    }

    @Test
    void readsPortFromValue() {
        assertEquals(9090, App.portFrom("9090"));
    }
}
