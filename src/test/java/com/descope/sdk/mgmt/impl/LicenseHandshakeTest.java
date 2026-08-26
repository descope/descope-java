package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.model.client.Client;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class LicenseHandshakeTest {

  @Test
  void testTierIsCachedFromResponse() throws IOException {
    HttpServer server = serverReturning(200, "{\"rateLimitTier\":\"tier4\"}", null);
    try {
      Client client = client(server, "mgmt-key");
      ManagementServiceBuilder.fetchRateLimitTier(client);
      assertEquals("tier4", client.getRateLimitTier());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testUnknownResponseFieldsAreTolerated() throws IOException {
    HttpServer server =
        serverReturning(200, "{\"rateLimitTier\":\"tier3\",\"somethingNew\":\"x\"}", null);
    try {
      Client client = client(server, "mgmt-key");
      ManagementServiceBuilder.fetchRateLimitTier(client);
      assertEquals("tier3", client.getRateLimitTier());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testErrorResponseLeavesTierUnsetAndIsNotRetried() throws IOException {
    AtomicInteger calls = new AtomicInteger(0);
    HttpServer server = serverReturning(503, "{}", calls);
    try {
      Client client = client(server, "mgmt-key");
      ManagementServiceBuilder.fetchRateLimitTier(client);
      assertNull(client.getRateLimitTier(), "a failed handshake must not set a tier");
      assertEquals(1, calls.get(),
          "the handshake must not inherit the proxy retry ladder");
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testSlowEndpointDoesNotBlockBeyondTimeout() throws IOException {
    HttpServer server = slowServer();
    try {
      Client client = client(server, "mgmt-key");
      long start = System.nanoTime();
      ManagementServiceBuilder.fetchRateLimitTier(client);
      long elapsedMs = (System.nanoTime() - start) / 1_000_000;
      assertNull(client.getRateLimitTier());
      assertTrue(elapsedMs < 9_000,
          "handshake must give up on its own timeout, took " + elapsedMs + "ms");
    } finally {
      server.stop(0);
    }
  }

  // RFC 5737 TEST-NET-1 is guaranteed unrouted, so the SYN is dropped rather than refused. This
  // guards the TCP connect phase, which RequestConfig alone does not bound: httpclient5 defaults
  // ConnectionConfig.connectTimeout to 3 minutes.
  @Test
  @Timeout(value = 25, unit = TimeUnit.SECONDS)
  void testUnreachableEndpointDoesNotBlockOnTcpConnect() {
    Client client = Client.builder()
        .uri("http://192.0.2.1:81")
        .projectId("P123")
        .managementKey("mgmt-key")
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    long start = System.nanoTime();
    ManagementServiceBuilder.fetchRateLimitTier(client);
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertNull(client.getRateLimitTier());
    assertTrue(elapsedMs < 9_000,
        "TCP connect must be bounded by the handshake timeout, took " + elapsedMs + "ms");
  }

  @Test
  void testNoManagementKeySkipsHandshakeEntirely() throws IOException {
    AtomicInteger calls = new AtomicInteger(0);
    HttpServer server = serverReturning(200, "{\"rateLimitTier\":\"tier4\"}", calls);
    try {
      Client client = client(server, "");
      ManagementServiceBuilder.fetchRateLimitTier(client);
      assertNull(client.getRateLimitTier());
      assertEquals(0, calls.get(),
          "no management key means no handshake request at all");
    } finally {
      server.stop(0);
    }
  }

  // --- helpers ---

  private Client client(HttpServer server, String managementKey) {
    return Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId("P123")
        .managementKey(managementKey)
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
  }

  private HttpServer serverReturning(int status, String body, AtomicInteger calls)
      throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/mgmt/license", exchange -> {
      if (calls != null) {
        calls.incrementAndGet();
      }
      byte[] payload = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    return server;
  }

  private HttpServer slowServer() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/mgmt/license", exchange -> {
      // Comfortably past the 5s handshake timeout without making the suite wait much longer.
      try {
        Thread.sleep(8_000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      exchange.sendResponseHeaders(200, 0);
      exchange.close();
    });
    server.start();
    return server;
  }
}
