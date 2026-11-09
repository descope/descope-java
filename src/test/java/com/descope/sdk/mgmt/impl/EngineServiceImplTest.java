package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.model.client.Client;
import com.descope.model.engine.Engine;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class EngineServiceImplTest {

  @Test
  void testLoadAll() throws IOException {
    AtomicReference<String> method = new AtomicReference<>();
    AtomicReference<String> path = new AtomicReference<>();
    AtomicReference<String> auth = new AtomicReference<>();
    HttpServer server = serverReturning(
        "{\"engines\":[{\"id\":\"e1\",\"name\":\"engine1\",\"secret\":\"\",\"createdTime\":1700000000,"
            + "\"unknown\":\"x\"},{\"id\":\"e2\",\"name\":\"engine2\"}]}",
        method, path, auth);
    try {
      List<Engine> engines = new EngineServiceImpl(client(server)).loadAll();
      assertEquals("GET", method.get());
      assertEquals("/v1/mgmt/engines/load", path.get());
      assertEquals("Bearer P123:mgmt-key", auth.get());
      assertEquals(2, engines.size());
      assertEquals("e1", engines.get(0).getId());
      assertEquals("engine1", engines.get(0).getName());
      assertEquals("", engines.get(0).getSecret());
      assertEquals(1700000000, engines.get(0).getCreatedTime());
      assertEquals("e2", engines.get(1).getId());
      assertEquals("engine2", engines.get(1).getName());
      assertNull(engines.get(1).getSecret());
      assertNull(engines.get(1).getCreatedTime());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testLoadAllEmpty() throws IOException {
    HttpServer server = serverReturning("{\"engines\":null}", new AtomicReference<>(), new AtomicReference<>(),
        new AtomicReference<>());
    try {
      assertTrue(new EngineServiceImpl(client(server)).loadAll().isEmpty());
    } finally {
      server.stop(0);
    }
  }

  private Client client(HttpServer server) {
    return Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId("P123")
        .managementKey("mgmt-key")
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
  }

  private HttpServer serverReturning(String body, AtomicReference<String> method, AtomicReference<String> path,
      AtomicReference<String> auth) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      method.set(exchange.getRequestMethod());
      path.set(exchange.getRequestURI().getPath());
      auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      byte[] payload = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    return server;
  }
}
