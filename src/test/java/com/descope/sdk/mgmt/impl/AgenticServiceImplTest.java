package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticIdentity;
import com.descope.model.agentic.AgenticIdentityConsent;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentityRevokeResponse;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.agentic.GovernanceAccessRequest;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestResolveRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchField;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.GovernanceAccessRequestSortField;
import com.descope.model.agentic.GovernanceAccessRequestTarget;
import com.descope.model.agentic.GovernanceSettings;
import com.descope.model.client.Client;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgenticServiceImplTest {

  private static final String PROJECT_ID = "P0000000000000000000000000000";
  private static final String MANAGEMENT_KEY = "test-key";
  private static final String ACCESS_REQUEST_JSON = "{\"id\":\"ar1\",\"requestorUserId\":\"U1\","
      + "\"requestorName\":\"Dev\",\"clientType\":\"ct\",\"grantTypes\":[\"authCode\"],"
      + "\"targets\":[{\"name\":\"api\",\"kind\":\"resource\",\"targetId\":\"t1\",\"scopes\":[\"read\"],"
      + "\"allScopes\":true}],\"scopes\":[\"s1\"],\"justification\":\"why\",\"status\":\"approved\","
      + "\"resolvedByUserId\":\"\",\"resolvedByName\":\"Management API\",\"resolutionNote\":\"ok\","
      + "\"clientId\":\"cid\",\"clientAppId\":\"aid\",\"createdTime\":\"1234567890123\","
      + "\"modifiedTime\":\"1234567890124\",\"version\":\"3\",\"clientName\":\"cn\",\"clientDeleted\":true,"
      + "\"somethingNew\":1}";

  private final ObjectMapper mapper = new ObjectMapper();
  private HttpServer server;
  private final AtomicReference<String> method = new AtomicReference<>();
  private final AtomicReference<String> path = new AtomicReference<>();
  private final AtomicReference<String> auth = new AtomicReference<>();
  private final AtomicReference<String> body = new AtomicReference<>();
  private final AtomicReference<String> response = new AtomicReference<>("{}");
  private AgenticService agenticService;

  @BeforeEach
  void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      method.set(exchange.getRequestMethod());
      path.set(exchange.getRequestURI().toString());
      auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      body.set(readAll(exchange.getRequestBody()));
      byte[] payload = response.get().getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    Client client = Client.builder()
        .uri("http://127.0.0.1:" + server.getAddress().getPort())
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    agenticService = new AgenticServiceImpl(client);
  }

  @AfterEach
  void tearDown() {
    server.stop(0);
  }

  @Test
  void testMethodsForMissingRequestParts() {
    assertInvalid("options", () -> agenticService.searchIdentities(null));
    assertInvalid("options", () -> agenticService.revokeIdentities(null));
    assertInvalid("options", () -> agenticService.revokeIdentities(AgenticIdentityRevokeOptions.builder().build()));
    assertInvalid("settings", () -> agenticService.setGovernanceSettings(null));
    assertInvalid("options", () -> agenticService.searchGovernanceAccessRequests(null));
    assertInvalid("options.sort.field", () -> agenticService.searchGovernanceAccessRequests(
        GovernanceAccessRequestSearchOptions.builder()
            .sort(Collections.singletonList(GovernanceAccessRequestSortField.builder().field("").build()))
            .build()));
    assertInvalid("id", () -> agenticService.getGovernanceAccessRequest(""));
    assertInvalid("request", () -> agenticService.resolveGovernanceAccessRequest(null));
    assertInvalid("request.id", () -> agenticService.resolveGovernanceAccessRequest(
        GovernanceAccessRequestResolveRequest.builder().action("approve").build()));
    assertInvalid("request.action", () -> agenticService.resolveGovernanceAccessRequest(
        GovernanceAccessRequestResolveRequest.builder().id("ar1").build()));
    assertInvalid("request", () -> agenticService.createGovernanceAccessRequest(null));
    assertInvalid("request.requestorUserId", () -> agenticService.createGovernanceAccessRequest(
        GovernanceAccessRequestCreateRequest.builder().grantTypes(Collections.singletonList("authCode")).build()));
    assertInvalid("request.grantTypes", () -> agenticService.createGovernanceAccessRequest(
        GovernanceAccessRequestCreateRequest.builder().requestorUserId("U1")
            .grantTypes(Collections.singletonList("")).build()));
    assertNull(path.get(), "no request may be sent when an argument is invalid");
  }

  @Test
  void testSearchIdentities() throws IOException {
    response.set("{\"identities\":[{\"consent\":{\"id\":\"c1\",\"appId\":\"a1\",\"userId\":\"U1\","
        + "\"scopes\":[\"s1\"],\"grantedBy\":\"g\",\"createdTime\":10,\"tenantId\":\"t1\",\"ExpireTime\":20,"
        + "\"modifiedTime\":30,\"authMethod\":\"idJag\"},\"agentName\":\"agent\",\"resourceId\":\"r1\","
        + "\"clientId\":\"cl1\",\"createdBy\":\"me\"}],\"total\":1}");
    AgenticIdentitySearchResponse res = agenticService.searchIdentities(AgenticIdentitySearchOptions.builder()
        .userId("U1").clientId("cl1").resourceId("r1").consentId("c1").page(2).limit(5).build());
    assertRequest("POST", "/v1/mgmt/agentic/identities/search",
        "{\"userId\":\"U1\",\"clientId\":\"cl1\",\"resourceId\":\"r1\",\"consentId\":\"c1\",\"page\":2,"
            + "\"limit\":5}");
    assertEquals(1, res.getTotal());
    AgenticIdentity identity = res.getIdentities().get(0);
    assertEquals("agent", identity.getAgentName());
    assertEquals("r1", identity.getResourceId());
    assertEquals("cl1", identity.getClientId());
    assertEquals("me", identity.getCreatedBy());
    AgenticIdentityConsent consent = identity.getConsent();
    assertEquals("c1", consent.getId());
    assertEquals("a1", consent.getAppId());
    assertEquals("U1", consent.getUserId());
    assertEquals(Collections.singletonList("s1"), consent.getScopes());
    assertEquals("g", consent.getGrantedBy());
    assertEquals(10, consent.getCreatedTime());
    assertEquals("t1", consent.getTenantId());
    assertEquals(20, consent.getExpireTime());
    assertEquals(30, consent.getModifiedTime());
    assertEquals("idJag", consent.getAuthMethod());
  }

  @Test
  void testSearchIdentitiesNullConsent() {
    response.set("{\"identities\":[{\"consent\":null,\"agentName\":\"agent\"}],\"total\":1}");
    AgenticIdentitySearchResponse res = agenticService.searchIdentities(AgenticIdentitySearchOptions.builder().build());
    assertNull(res.getIdentities().get(0).getConsent());
  }

  @Test
  void testRevokeIdentities() throws IOException {
    response.set("{\"revoked\":3}");
    AgenticIdentityRevokeResponse res = agenticService.revokeIdentities(AgenticIdentityRevokeOptions.builder()
        .userId("U1").clientId("cl1").resourceId("r1").consentId("c1").build());
    assertRequest("POST", "/v1/mgmt/agentic/identities/revoke",
        "{\"userId\":\"U1\",\"clientId\":\"cl1\",\"resourceId\":\"r1\",\"consentId\":\"c1\"}");
    assertEquals(3, res.getRevoked());
  }

  @Test
  void testRevokeIdentitiesByUserOnly() throws IOException {
    response.set("{\"revoked\":1}");
    agenticService.revokeIdentities(AgenticIdentityRevokeOptions.builder().userId("U1").build());
    assertRequest("POST", "/v1/mgmt/agentic/identities/revoke", "{\"userId\":\"U1\"}");
  }

  @Test
  void testGetGovernanceSettings() {
    response.set("{\"configured\":true,\"autoApproval\":false,\"suiteDisabled\":true}");
    GovernanceSettings res = agenticService.getGovernanceSettings();
    assertEquals("GET", method.get());
    assertEquals("/v1/mgmt/agentic/governance/settings", path.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth.get());
    assertTrue(res.getConfigured());
    assertFalse(res.getAutoApproval());
    assertTrue(res.getSuiteDisabled());
  }

  @Test
  void testGetGovernanceSettingsAbsentFields() {
    response.set("{}");
    GovernanceSettings res = agenticService.getGovernanceSettings();
    assertNull(res.getConfigured());
    assertNull(res.getAutoApproval());
    assertNull(res.getSuiteDisabled());
  }

  @Test
  void testSetGovernanceSettings() throws IOException {
    response.set("{\"configured\":true,\"autoApproval\":true,\"suiteDisabled\":false}");
    GovernanceSettings res = agenticService.setGovernanceSettings(GovernanceSettings.builder()
        .configured(true).autoApproval(true).suiteDisabled(false).build());
    assertRequest("POST", "/v1/mgmt/agentic/governance/settings",
        "{\"configured\":true,\"autoApproval\":true,\"suiteDisabled\":false}");
    assertTrue(res.getConfigured());
    assertTrue(res.getAutoApproval());
    assertFalse(res.getSuiteDisabled());
  }

  @Test
  void testSetGovernanceSettingsOmitsUnsetFields() throws IOException {
    response.set("{\"configured\":true,\"autoApproval\":true,\"suiteDisabled\":false}");
    agenticService.setGovernanceSettings(GovernanceSettings.builder().autoApproval(false).build());
    assertRequest("POST", "/v1/mgmt/agentic/governance/settings", "{\"autoApproval\":false}");
  }

  @Test
  void testSearchGovernanceAccessRequests() throws IOException {
    response.set("{\"requests\":[" + ACCESS_REQUEST_JSON + "],\"total\":1}");
    GovernanceAccessRequestSearchResponse res = agenticService.searchGovernanceAccessRequests(
        GovernanceAccessRequestSearchOptions.builder()
            .limit(10)
            .page(1)
            .searchFields(Collections.singletonList(GovernanceAccessRequestSearchField.builder()
                .field("status").valArr(Arrays.asList("pending", "approved")).valStr("x").negative(true)
                .valType("string").build()))
            .sort(Collections.singletonList(GovernanceAccessRequestSortField.builder()
                .field("createdTime").desc(true).build()))
            .text("agent")
            .build());
    assertRequest("POST", "/v1/mgmt/agentic/governance/requests/search",
        "{\"limit\":10,\"page\":1,\"searchFields\":[{\"field\":\"status\",\"valArr\":[\"pending\",\"approved\"],"
            + "\"valStr\":\"x\",\"negative\":true,\"valType\":\"string\"}],"
            + "\"sort\":[{\"field\":\"createdTime\",\"desc\":true}],\"text\":\"agent\"}");
    assertEquals(1, res.getTotal());
    assertAccessRequest(res.getRequests().get(0));
  }

  @Test
  void testGetGovernanceAccessRequest() throws IOException {
    response.set(ACCESS_REQUEST_JSON);
    GovernanceAccessRequest res = agenticService.getGovernanceAccessRequest("ar1");
    assertRequest("POST", "/v1/mgmt/agentic/governance/requests/get", "{\"id\":\"ar1\"}");
    assertAccessRequest(res);
  }

  @Test
  void testResolveGovernanceAccessRequest() throws IOException {
    response.set(ACCESS_REQUEST_JSON);
    GovernanceAccessRequest res = agenticService.resolveGovernanceAccessRequest(
        GovernanceAccessRequestResolveRequest.builder().id("ar1").action("approve").resolutionNote("ok").build());
    assertRequest("POST", "/v1/mgmt/agentic/governance/requests/resolve",
        "{\"id\":\"ar1\",\"action\":\"approve\",\"resolutionNote\":\"ok\"}");
    assertAccessRequest(res);
  }

  @Test
  void testCreateGovernanceAccessRequest() throws IOException {
    response.set(ACCESS_REQUEST_JSON);
    GovernanceAccessRequest res = agenticService.createGovernanceAccessRequest(
        GovernanceAccessRequestCreateRequest.builder()
            .requestorUserId("U1")
            .clientName("cn")
            .clientType("ct")
            .grantTypes(Collections.singletonList("authCode"))
            .targets(Collections.singletonList(GovernanceAccessRequestTarget.builder()
                .name("api").kind("resource").scopes(Collections.singletonList("read")).build()))
            .scopes(Collections.singletonList("s1"))
            .justification("why")
            .build());
    assertRequest("POST", "/v1/mgmt/agentic/governance/requests/create",
        "{\"requestorUserId\":\"U1\",\"clientName\":\"cn\",\"clientType\":\"ct\",\"grantTypes\":[\"authCode\"],"
            + "\"targets\":[{\"name\":\"api\",\"kind\":\"resource\",\"scopes\":[\"read\"],\"allScopes\":false}],"
            + "\"scopes\":[\"s1\"],\"justification\":\"why\"}");
    assertAccessRequest(res);
  }

  private void assertAccessRequest(GovernanceAccessRequest req) {
    assertEquals("ar1", req.getId());
    assertEquals("U1", req.getRequestorUserId());
    assertEquals("Dev", req.getRequestorName());
    assertEquals("ct", req.getClientType());
    assertEquals(Collections.singletonList("authCode"), req.getGrantTypes());
    GovernanceAccessRequestTarget target = req.getTargets().get(0);
    assertEquals("api", target.getName());
    assertEquals("resource", target.getKind());
    assertEquals("t1", target.getTargetId());
    assertEquals(Collections.singletonList("read"), target.getScopes());
    assertTrue(target.isAllScopes());
    assertEquals(Collections.singletonList("s1"), req.getScopes());
    assertEquals("why", req.getJustification());
    assertEquals("approved", req.getStatus());
    assertEquals("", req.getResolvedByUserId());
    assertEquals("Management API", req.getResolvedByName());
    assertEquals("ok", req.getResolutionNote());
    assertEquals("cid", req.getClientId());
    assertEquals("aid", req.getClientAppId());
    assertEquals(1234567890123L, req.getCreatedTime());
    assertEquals(1234567890124L, req.getModifiedTime());
    assertEquals("cn", req.getClientName());
    assertTrue(req.isClientDeleted());
  }

  private void assertRequest(String expectedMethod, String expectedPath, String expectedBody) throws IOException {
    assertEquals(expectedMethod, method.get());
    assertEquals(expectedPath, path.get());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth.get());
    JsonNode expected = mapper.readTree(expectedBody);
    JsonNode actual = mapper.readTree(body.get());
    assertEquals(expected, actual);
  }

  private static void assertInvalid(String argument, Runnable call) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, call::run);
    assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
  }

  private static String readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int n;
    while ((n = in.read(buf)) > 0) {
      out.write(buf, 0, n);
    }
    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }
}
