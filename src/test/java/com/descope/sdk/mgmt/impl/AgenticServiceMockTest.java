package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestResolveRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestTarget;
import com.descope.model.agentic.GovernanceSearchField;
import com.descope.model.agentic.GovernanceSettings;
import com.descope.model.agentic.GovernanceSortField;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs the agentic methods against the mock backend at DESCOPE_MOCK_URL, and skips when it is unset.
 */
class AgenticServiceMockTest {

  private static final String USER_ID = "U2abcdefghijklmnopqrstuvwxyz";
  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

  private String mockUrl;
  private AgenticService agenticService;

  @BeforeEach
  void setUp() {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    mockUrl = System.getenv("DESCOPE_MOCK_URL");
    Client client = Client.builder()
        .uri(mockUrl)
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("some-management-key")
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    agenticService = ManagementServiceBuilder.buildServices(client).getAgenticService();
  }

  @Test
  void testSearchAgenticIdentities() {
    AgenticIdentitySearchOptions options = AgenticIdentitySearchOptions.builder().userId(USER_ID).clientId("c1")
        .resourceId("r1").consentId("cs1").page(1).limit(10).build();
    Object res = agenticService.searchAgenticIdentities(options);
    assertLast("POST", "/v1/mgmt/agentic/identities/search", options, res);
  }

  @Test
  void testRevokeAgenticIdentities() {
    AgenticIdentityRevokeOptions options = AgenticIdentityRevokeOptions.builder().userId(USER_ID).clientId("c1")
        .resourceId("r1").consentId("cs1").build();
    int revoked = agenticService.revokeAgenticIdentities(options);
    assertLast("POST", "/v1/mgmt/agentic/identities/revoke", options,
        Collections.singletonMap("revoked", revoked));
  }

  @Test
  void testGetGovernanceSettings() {
    GovernanceSettings res = agenticService.getGovernanceSettings();
    assertLast("GET", "/v1/mgmt/agentic/governance/settings", null, res);
  }

  @Test
  void testSetGovernanceSettings() {
    GovernanceSettings settings = GovernanceSettings.builder().configured(true).autoApproval(false)
        .suiteDisabled(true).build();
    GovernanceSettings res = agenticService.setGovernanceSettings(settings);
    assertLast("POST", "/v1/mgmt/agentic/governance/settings", settings, res);
  }

  @Test
  void testSearchGovernanceAccessRequests() {
    GovernanceAccessRequestSearchOptions options = GovernanceAccessRequestSearchOptions.builder().limit(5).page(1)
        .searchFields(Collections.singletonList(GovernanceSearchField.builder().field("status")
            .valArr(Collections.singletonList("pending")).valStr("x").negative(true).valType("string").build()))
        .sort(Collections.singletonList(GovernanceSortField.builder().field("createdTime").desc(true).build()))
        .text("free text").build();
    Object res = agenticService.searchGovernanceAccessRequests(options);
    assertLast("POST", "/v1/mgmt/agentic/governance/requests/search", options, res);
  }

  @Test
  void testGetGovernanceAccessRequest() {
    Object res = agenticService.getGovernanceAccessRequest("rid");
    assertLast("POST", "/v1/mgmt/agentic/governance/requests/get", Collections.singletonMap("id", "rid"), res);
  }

  @Test
  void testResolveGovernanceAccessRequest() {
    GovernanceAccessRequestResolveRequest request = GovernanceAccessRequestResolveRequest.builder().id("rid")
        .action("approve").resolutionNote("note").build();
    Object res = agenticService.resolveGovernanceAccessRequest(request);
    assertLast("POST", "/v1/mgmt/agentic/governance/requests/resolve", request, res);
  }

  @Test
  void testCreateGovernanceAccessRequest() {
    GovernanceAccessRequestCreateRequest request = GovernanceAccessRequestCreateRequest.builder()
        .requestorUserId(USER_ID).clientName("cname").clientType("ctype")
        .grantTypes(Collections.singletonList("authCode"))
        .targets(Collections.singletonList(GovernanceAccessRequestTarget.builder().name("tname").kind("resource")
            .targetId("tid").scopes(Collections.singletonList("s1")).allScopes(true).build()))
        .scopes(Collections.singletonList("s2")).justification("why").build();
    Object res = agenticService.createGovernanceAccessRequest(request);
    assertLast("POST", "/v1/mgmt/agentic/governance/requests/create", request, res);
  }

  /**
   * Checks the last call the mock received: its method and path, that the backend decoded every
   * field that was sent, and that every response field came through in the parsed result.
   */
  @SneakyThrows
  private void assertLast(String method, String path, Object sent, Object parsed) {
    JsonNode last = fetchLast();
    assertEquals(method, last.get("method").asText());
    assertEquals(path, last.get("path").asText());
    assertEquals(200, last.get("status").asInt());
    if (sent != null) {
      assertSubset("request", MAPPER.valueToTree(sent), last.get("request"));
    }
    JsonNode responseBody = last.get("responseBody");
    assertNotNull(responseBody);
    assertTrue(responseBody.size() > 0);
    assertSubset("response", responseBody, MAPPER.valueToTree(parsed));
  }

  /** Asserts every field of expected is present in actual with the same value. */
  private static void assertSubset(String at, JsonNode expected, JsonNode actual) {
    assertNotNull(actual, at + " is missing");
    if (expected.isObject()) {
      assertTrue(actual.isObject(), at + " is not an object");
      Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        assertSubset(at + "." + field.getKey(), field.getValue(), actual.get(field.getKey()));
      }
    } else if (expected.isArray()) {
      assertTrue(actual.isArray(), at + " is not an array");
      assertEquals(expected.size(), actual.size(), at + " size");
      for (int i = 0; i < expected.size(); i++) {
        assertSubset(at + "[" + i + "]", expected.get(i), actual.get(i));
      }
    } else {
      // 64-bit integers come as strings
      assertEquals(expected.asText(), actual.asText(), at);
    }
  }

  @SneakyThrows
  private JsonNode fetchLast() {
    HttpURLConnection conn = (HttpURLConnection) new URL(mockUrl + "/__mock/last").openConnection();
    conn.setRequestMethod("GET");
    try (InputStream in = conn.getInputStream()) {
      return MAPPER.readTree(in);
    } finally {
      conn.disconnect();
    }
  }
}
