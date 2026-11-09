package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.engine.Engine;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.EngineService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EngineServiceImplTest {

  private static final String MANAGEMENT_KEY = "test-key";
  private static final String ENGINE_JSON =
      "{\"engine\":{\"id\":\"eng1\",\"name\":\"name1\",\"secret\":\"sec1\",\"createdTime\":1700000000}}";

  private final ObjectMapper mapper = new ObjectMapper();
  private HttpServer server;
  private String responseBody;
  private String method;
  private String path;
  private String query;
  private String authorization;
  private String requestBody;
  private EngineService engineService;

  @BeforeEach
  void setUp() throws IOException {
    responseBody = "{}";
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      method = exchange.getRequestMethod();
      path = exchange.getRequestURI().getPath();
      query = exchange.getRequestURI().getQuery();
      authorization = exchange.getRequestHeaders().getFirst("Authorization");
      requestBody = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
      byte[] payload = responseBody.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    Client client = Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    engineService = new EngineServiceImpl(client);
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void testMethodsForMissingArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> engineService.create(""));
    assertEquals("The name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> engineService.update("", "name"));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> engineService.update("id", null));
    assertEquals("The name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> engineService.delete(null));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> engineService.load(""));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> engineService.rotateSecret(" "));
    assertEquals("The id argument is invalid", thrown.getMessage());
    assertNull(method);
  }

  @Test
  void testCreateForSuccess() throws IOException {
    responseBody = ENGINE_JSON;
    Engine engine = engineService.create("name1");
    assertRequest("POST", "/v1/mgmt/engine/create");
    assertEquals(mapOf("name", "name1"), body());
    assertEngine(engine);
  }

  @Test
  void testUpdateForSuccess() throws IOException {
    responseBody = ENGINE_JSON;
    Engine engine = engineService.update("eng1", "name1");
    assertRequest("POST", "/v1/mgmt/engine/update");
    Map<String, Object> expected = mapOf("id", "eng1");
    expected.put("name", "name1");
    assertEquals(expected, body());
    assertEngine(engine);
  }

  @Test
  void testDeleteForSuccess() throws IOException {
    engineService.delete("eng1");
    assertRequest("POST", "/v1/mgmt/engine/delete");
    assertEquals(mapOf("id", "eng1"), body());
  }

  @Test
  void testLoadForSuccess() {
    responseBody = ENGINE_JSON;
    Engine engine = engineService.load("eng1");
    assertRequest("GET", "/v1/mgmt/engine/load");
    assertEquals("id=eng1", query);
    assertEngine(engine);
  }

  @Test
  void testLoadWithNullEngine() {
    responseBody = "{\"engine\":null,\"unknown\":1}";
    assertNull(engineService.load("eng1"));
  }

  @Test
  void testRotateSecretForSuccess() throws IOException {
    responseBody = "{\"secret\":\"sec2\"}";
    String secret = engineService.rotateSecret("eng1");
    assertRequest("POST", "/v1/mgmt/engine/rotate");
    assertEquals(mapOf("id", "eng1"), body());
    assertEquals("sec2", secret);
  }

  private void assertRequest(String expectedMethod, String expectedPath) {
    assertEquals(expectedMethod, method);
    assertEquals(expectedPath, path);
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY, authorization);
  }

  private void assertEngine(Engine engine) {
    assertEquals("eng1", engine.getId());
    assertEquals("name1", engine.getName());
    assertEquals("sec1", engine.getSecret());
    assertEquals(1700000000, engine.getCreatedTime());
  }

  private Map<String, Object> body() throws IOException {
    return mapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
  }

  private static Map<String, Object> mapOf(String key, Object value) {
    Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    return map;
  }
}
