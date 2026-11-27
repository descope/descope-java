package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.sessionclassification.SessionClassification;
import com.descope.model.sessionclassification.SessionTimeLimitRule;
import com.descope.sdk.mgmt.SessionClassificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SessionClassificationServiceImplTest {

  private static final String PROJECT_ID = "P123";
  private static final String MANAGEMENT_KEY = "mgmt-key";
  private static final String CLASSIFICATION_JSON = "{\"name\":\"admins\",\"description\":\"desc\","
      + "\"sessionTimeLimitRule\":\"SESSION_TIME_LIMIT_RULE_MIN_OF_TENANTS\",\"accessAllTenants\":true,"
      + "\"maxConcurrentSessions\":3,\"createdTime\":100,\"modifiedTime\":200,\"version\":4,\"unknown\":\"x\"}";

  private final AtomicReference<String> method = new AtomicReference<>();
  private final AtomicReference<String> path = new AtomicReference<>();
  private final AtomicReference<String> query = new AtomicReference<>();
  private final AtomicReference<String> auth = new AtomicReference<>();
  private final AtomicReference<String> body = new AtomicReference<>();
  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void testMethodsForMissingRequestParts() {
    SessionClassificationService service = new SessionClassificationServiceImpl(Client.builder().build());
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> service.load(""));
    assertEquals("The name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> service.set(null));
    assertEquals("The classification argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> service.set(SessionClassification.builder().description("d").build()));
    assertEquals("The classification.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> service.delete(" "));
    assertEquals("The name argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoad() throws IOException {
    SessionClassification res = service(CLASSIFICATION_JSON).load("admins");
    assertEquals("GET", method.get());
    assertEquals("/v1/mgmt/sessionclassification", path.get());
    assertEquals("name=admins", query.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth.get());
    assertFullClassification(res);
  }

  @Test
  void testSet() throws IOException {
    SessionClassification res = service(CLASSIFICATION_JSON).set(SessionClassification.builder()
        .name("admins")
        .description("desc")
        .sessionTimeLimitRule(SessionTimeLimitRule.MIN_OF_TENANTS)
        .accessAllTenants(true)
        .maxConcurrentSessions(3)
        .version(4)
        .build());
    assertEquals("POST", method.get());
    assertEquals("/v1/mgmt/sessionclassification", path.get());
    assertNull(query.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth.get());
    JsonNode sent = new ObjectMapper().readTree(body.get());
    assertEquals("admins", sent.get("name").asText());
    assertEquals("desc", sent.get("description").asText());
    assertEquals(SessionTimeLimitRule.MIN_OF_TENANTS, sent.get("sessionTimeLimitRule").asText());
    assertTrue(sent.get("accessAllTenants").asBoolean());
    assertEquals(3, sent.get("maxConcurrentSessions").asInt());
    assertEquals(4, sent.get("version").asInt());
    assertFalse(sent.has("createdTime"));
    assertFalse(sent.has("modifiedTime"));
    assertFullClassification(res);
  }

  @Test
  void testSetOmitsUnsetFields() throws IOException {
    service(CLASSIFICATION_JSON).set(SessionClassification.builder().name("admins").build());
    JsonNode sent = new ObjectMapper().readTree(body.get());
    assertEquals(1, sent.size());
    assertEquals("admins", sent.get("name").asText());
  }

  @Test
  void testDelete() throws IOException {
    service("{}").delete("admins");
    assertEquals("DELETE", method.get());
    assertEquals("/v1/mgmt/sessionclassification", path.get());
    assertEquals("name=admins", query.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth.get());
  }

  @Test
  void testLoadAll() throws IOException {
    List<SessionClassification> res = service("{\"classifications\":[" + CLASSIFICATION_JSON + "]}").loadAll();
    assertEquals("GET", method.get());
    assertEquals("/v1/mgmt/sessionclassification/all", path.get());
    assertNull(query.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth.get());
    assertEquals(1, res.size());
    assertFullClassification(res.get(0));
  }

  private static void assertFullClassification(SessionClassification res) {
    assertEquals("admins", res.getName());
    assertEquals("desc", res.getDescription());
    assertEquals(SessionTimeLimitRule.MIN_OF_TENANTS, res.getSessionTimeLimitRule());
    assertTrue(res.getAccessAllTenants());
    assertEquals(3, res.getMaxConcurrentSessions());
    assertEquals(100, res.getCreatedTime());
    assertEquals(200, res.getModifiedTime());
    assertEquals(4, res.getVersion());
  }

  private SessionClassificationService service(String response) throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      method.set(exchange.getRequestMethod());
      path.set(exchange.getRequestURI().getPath());
      query.set(exchange.getRequestURI().getQuery());
      auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      body.set(readAll(exchange.getRequestBody()));
      byte[] payload = response.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    Client client = Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    return new SessionClassificationServiceImpl(client);
  }

  private static String readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int n;
    while ((n = in.read(buf)) != -1) {
      out.write(buf, 0, n);
    }
    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }
}
