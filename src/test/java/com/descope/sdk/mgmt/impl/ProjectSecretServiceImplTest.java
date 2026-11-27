package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.secret.ProjectSecret;
import com.descope.sdk.mgmt.ProjectSecretService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectSecretServiceImplTest {

  private static final String PROJECT_ID = "P123";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String SECRET_JSON = "{\"id\":\"mysecret\",\"name\":\"MySecret\",\"description\":\"desc\","
      + "\"createdTime\":1700000000,\"modifiedTime\":1700000100,\"somethingNew\":\"x\"}";

  private final ObjectMapper mapper = new ObjectMapper();
  private final List<Request> requests = new ArrayList<>();
  private final AtomicReference<String> responseBody = new AtomicReference<>("{}");
  private HttpServer server;
  private ProjectSecretService service;

  private static class Request {
    private final String method;
    private final String path;
    private final String auth;
    private final String body;

    Request(String method, String path, String auth, String body) {
      this.method = method;
      this.path = path;
      this.auth = auth;
      this.body = body;
    }
  }

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getRawPath();
      byte[] out = "{}".getBytes(StandardCharsets.UTF_8);
      if (!path.equals("/v1/mgmt/license")) {
        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        requests.add(new Request(exchange.getRequestMethod(), path,
            exchange.getRequestHeaders().getFirst("Authorization"), body));
        out = responseBody.get().getBytes(StandardCharsets.UTF_8);
      }
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, out.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(out);
      }
    });
    server.start();
    Client client = Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    service = ManagementServiceBuilder.buildServices(client).getProjectSecretService();
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void testCreateSecret() throws IOException {
    responseBody.set("{\"secret\":" + SECRET_JSON + "}");
    ProjectSecret secret = service.createSecret("MySecret", "s3cr3t", "desc");
    assertSecret(secret);
    Request req = singleRequest();
    assertEquals("POST", req.method);
    assertEquals("/v1/mgmt/secret", req.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, req.auth);
    assertEquals(mapper.readTree("{\"name\":\"MySecret\",\"description\":\"desc\",\"value\":\"s3cr3t\"}"),
        mapper.readTree(req.body));
  }

  @Test
  void testCreateSecretOmitsUnsetDescription() throws IOException {
    responseBody.set("{\"secret\":" + SECRET_JSON + "}");
    service.createSecret("MySecret", "s3cr3t", null);
    assertEquals(mapper.readTree("{\"name\":\"MySecret\",\"value\":\"s3cr3t\"}"),
        mapper.readTree(singleRequest().body));
  }

  @Test
  void testCreateSecretValidation() {
    assertThrows(ServerCommonException.class, () -> service.createSecret(null, "v", null));
    assertThrows(ServerCommonException.class, () -> service.createSecret(" ", "v", null));
    assertThrows(ServerCommonException.class, () -> service.createSecret("n", null, null));
    assertThrows(ServerCommonException.class, () -> service.createSecret("n", "", null));
    assertTrue(requests.isEmpty());
  }

  @Test
  void testUpdateSecret() throws IOException {
    responseBody.set("{\"secret\":" + SECRET_JSON + "}");
    ProjectSecret secret = service.updateSecret("mysecret", "desc", "n3w");
    assertSecret(secret);
    Request req = singleRequest();
    assertEquals("POST", req.method);
    assertEquals("/v1/mgmt/secret/update", req.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, req.auth);
    assertEquals(mapper.readTree("{\"id\":\"mysecret\",\"description\":\"desc\",\"value\":\"n3w\"}"),
        mapper.readTree(req.body));
  }

  @Test
  void testUpdateSecretKeepsValue() throws IOException {
    responseBody.set("{\"secret\":" + SECRET_JSON + "}");
    service.updateSecret("mysecret", null, "");
    assertEquals(mapper.readTree("{\"id\":\"mysecret\"}"), mapper.readTree(singleRequest().body));
  }

  @Test
  void testUpdateSecretValidation() {
    assertThrows(ServerCommonException.class, () -> service.updateSecret(null, "d", "v"));
    assertThrows(ServerCommonException.class, () -> service.updateSecret("", "d", "v"));
    assertTrue(requests.isEmpty());
  }

  @Test
  void testDeleteSecret() throws IOException {
    service.deleteSecret("mysecret");
    Request req = singleRequest();
    assertEquals("POST", req.method);
    assertEquals("/v1/mgmt/secret/delete", req.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, req.auth);
    assertEquals(mapper.readTree("{\"id\":\"mysecret\"}"), mapper.readTree(req.body));
  }

  @Test
  void testDeleteSecretValidation() {
    assertThrows(ServerCommonException.class, () -> service.deleteSecret(null));
    assertThrows(ServerCommonException.class, () -> service.deleteSecret(" "));
    assertTrue(requests.isEmpty());
  }

  @Test
  void testLoadSecret() {
    responseBody.set("{\"secret\":" + SECRET_JSON + "}");
    ProjectSecret secret = service.loadSecret("mysecret");
    assertSecret(secret);
    Request req = singleRequest();
    assertEquals("GET", req.method);
    assertEquals("/v1/mgmt/secret/mysecret", req.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, req.auth);
  }

  @Test
  void testLoadSecretEncodesId() {
    responseBody.set("{\"secret\":" + SECRET_JSON + "}");
    service.loadSecret("a/b c?");
    assertEquals("/v1/mgmt/secret/a%2Fb%20c%3F", singleRequest().path);
  }

  @Test
  void testLoadSecretValidation() {
    assertThrows(ServerCommonException.class, () -> service.loadSecret(null));
    assertThrows(ServerCommonException.class, () -> service.loadSecret(""));
    assertThrows(ServerCommonException.class, () -> service.loadSecret("."));
    assertThrows(ServerCommonException.class, () -> service.loadSecret(".."));
    assertThrows(ServerCommonException.class, () -> service.loadSecret("all"));
    assertTrue(requests.isEmpty());
  }

  @Test
  void testLoadSecretMissingSecret() {
    assertNull(service.loadSecret("mysecret"));
  }

  @Test
  void testLoadAllSecrets() {
    responseBody.set("{\"secrets\":[" + SECRET_JSON + "]}");
    List<ProjectSecret> secrets = service.loadAllSecrets();
    assertEquals(1, secrets.size());
    assertSecret(secrets.get(0));
    Request req = singleRequest();
    assertEquals("GET", req.method);
    assertEquals("/v1/mgmt/secret/all", req.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, req.auth);
  }

  @Test
  void testLoadAllSecretsEmpty() {
    responseBody.set("{\"secrets\":null}");
    assertTrue(service.loadAllSecrets().isEmpty());
  }

  private Request singleRequest() {
    assertEquals(1, requests.size());
    return requests.get(0);
  }

  private static void assertSecret(ProjectSecret secret) {
    assertEquals("mysecret", secret.getId());
    assertEquals("MySecret", secret.getName());
    assertEquals("desc", secret.getDescription());
    assertEquals(Integer.valueOf(1700000000), secret.getCreatedTime());
    assertEquals(Integer.valueOf(1700000100), secret.getModifiedTime());
  }
}
