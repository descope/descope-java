package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.descoper.Descoper;
import com.descope.model.descoper.DescoperAttributes;
import com.descope.model.descoper.DescoperProjectRole;
import com.descope.model.descoper.DescoperRBAC;
import com.descope.model.descoper.DescoperTagRole;
import com.descope.model.descoper.DescopersListResponse;
import com.descope.sdk.mgmt.DescoperService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescoperServiceImplTest {

  private static final String PROJECT_ID = "P123";
  private static final String MANAGEMENT_KEY = "mgmt-key";
  private static final String DESCOPER_JSON = "{\"id\":\"U1\",\"status\":\"enabled\",\"loginIDs\":[\"a@example.com\"],"
      + "\"attributes\":{\"displayName\":\"Name\",\"email\":\"a@example.com\",\"phone\":\"+15555555555\"},"
      + "\"rbac\":{\"isCompanyAdmin\":true,\"tags\":[{\"tags\":[\"t1\"],\"role\":\"admin\"}],"
      + "\"projects\":[{\"projectIds\":[\"P1\"],\"role\":\"developer\"}]},\"unknownField\":1}";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private HttpServer server;
  private final AtomicReference<String> method = new AtomicReference<>();
  private final AtomicReference<String> path = new AtomicReference<>();
  private final AtomicReference<String> query = new AtomicReference<>();
  private final AtomicReference<String> authorization = new AtomicReference<>();
  private final AtomicReference<String> body = new AtomicReference<>();
  private final AtomicReference<String> response = new AtomicReference<>("{}");
  private DescoperService descoperService;

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/mgmt/descoper", exchange -> {
      method.set(exchange.getRequestMethod());
      path.set(exchange.getRequestURI().getPath());
      query.set(exchange.getRequestURI().getRawQuery());
      authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
      body.set(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8));
      byte[] payload = response.get().getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
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
    descoperService = ManagementServiceBuilder.buildServices(client).getDescoperService();
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void testMethodsForMissingRequestParts() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> descoperService.getDescoper(""));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> descoperService.updateDescoper(null, null, null));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> descoperService.deleteDescoper(" "));
    assertEquals("The id argument is invalid", thrown.getMessage());
    assertNull(method.get());
  }

  @Test
  void testGetDescoperSuccess() {
    response.set("{\"descoper\":" + DESCOPER_JSON + "}");
    final Descoper descoper = descoperService.getDescoper("U1");
    assertEquals("GET", method.get());
    assertEquals("/v1/mgmt/descoper", path.get());
    assertEquals("id=U1", query.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authorization.get());
    assertFullDescoper(descoper);
  }

  @Test
  void testGetDescoperNullDescoper() {
    response.set("{\"descoper\":null}");
    assertNull(descoperService.getDescoper("U1"));
  }

  @Test
  void testUpdateDescoperSuccess() throws IOException {
    response.set("{\"descoper\":" + DESCOPER_JSON + "}");
    DescoperRBAC rbac = DescoperRBAC.builder()
        .isCompanyAdmin(false)
        .tags(Collections.singletonList(
            DescoperTagRole.builder().tags(Arrays.asList("t1", "t2")).role("admin").build()))
        .projects(Collections.singletonList(
            DescoperProjectRole.builder().projectIds(Collections.singletonList("P1")).role("developer").build()))
        .build();
    final Descoper descoper = descoperService.updateDescoper("U1",
        DescoperAttributes.builder().displayName("Name").email("a@example.com").phone("+15555555555").build(),
        rbac);
    assertEquals("PATCH", method.get());
    assertEquals("/v1/mgmt/descoper", path.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authorization.get());
    JsonNode req = objectMapper.readTree(body.get());
    assertEquals("U1", req.get("id").asText());
    assertEquals("Name", req.get("attributes").get("displayName").asText());
    assertEquals("a@example.com", req.get("attributes").get("email").asText());
    assertEquals("+15555555555", req.get("attributes").get("phone").asText());
    assertFalse(req.get("rbac").get("isCompanyAdmin").asBoolean());
    assertEquals("t2", req.get("rbac").get("tags").get(0).get("tags").get(1).asText());
    assertEquals("admin", req.get("rbac").get("tags").get(0).get("role").asText());
    assertEquals("P1", req.get("rbac").get("projects").get(0).get("projectIds").get(0).asText());
    assertEquals("developer", req.get("rbac").get("projects").get(0).get("role").asText());
    assertFullDescoper(descoper);
  }

  @Test
  void testUpdateDescoperOmitsUnsetFields() throws IOException {
    response.set("{\"descoper\":" + DESCOPER_JSON + "}");
    descoperService.updateDescoper("U1", DescoperAttributes.builder().displayName("Name").build(), null);
    JsonNode req = objectMapper.readTree(body.get());
    assertEquals(2, req.size());
    assertEquals(1, req.get("attributes").size());
    assertFalse(req.has("rbac"));
  }

  @Test
  void testDeleteDescoperSuccess() {
    descoperService.deleteDescoper("U 1");
    assertEquals("DELETE", method.get());
    assertEquals("/v1/mgmt/descoper", path.get());
    assertEquals("id=U+1", query.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authorization.get());
  }

  @Test
  void testListDescopersSuccess() throws IOException {
    response.set("{\"descopers\":[" + DESCOPER_JSON + "],\"total\":1}");
    final DescopersListResponse res = descoperService.listDescopers();
    assertEquals("POST", method.get());
    assertEquals("/v1/mgmt/descoper/list", path.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authorization.get());
    assertEquals(0, objectMapper.readTree(body.get()).size());
    assertEquals(Integer.valueOf(1), res.getTotal());
    assertEquals(1, res.getDescopers().size());
    assertFullDescoper(res.getDescopers().get(0));
  }

  private void assertFullDescoper(Descoper descoper) {
    assertEquals("U1", descoper.getId());
    assertEquals("enabled", descoper.getStatus());
    assertEquals(Collections.singletonList("a@example.com"), descoper.getLoginIDs());
    assertEquals("Name", descoper.getAttributes().getDisplayName());
    assertEquals("a@example.com", descoper.getAttributes().getEmail());
    assertEquals("+15555555555", descoper.getAttributes().getPhone());
    assertTrue(descoper.getRbac().getIsCompanyAdmin());
    assertEquals(Collections.singletonList("t1"), descoper.getRbac().getTags().get(0).getTags());
    assertEquals("admin", descoper.getRbac().getTags().get(0).getRole());
    assertEquals(Collections.singletonList("P1"), descoper.getRbac().getProjects().get(0).getProjectIds());
    assertEquals("developer", descoper.getRbac().getProjects().get(0).getRole());
  }
}
