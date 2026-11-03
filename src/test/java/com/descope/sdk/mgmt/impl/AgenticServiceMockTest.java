package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestResolveRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchField;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSortField;
import com.descope.model.agentic.GovernanceAccessRequestTarget;
import com.descope.model.agentic.GovernanceSettings;
import com.descope.model.client.Client;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Runs against the mock backend at DESCOPE_MOCK_URL and is skipped when it is unset.
class AgenticServiceMockTest {

  private static final String USER_ID = "U2aaaaaaaaaaaaaaaaaaaaaaaaaa";

  private final ObjectMapper mapper = new ObjectMapper();
  private String mockUrl;
  private AgenticService agenticService;

  @BeforeEach
  void setUp() {
    mockUrl = System.getenv("DESCOPE_MOCK_URL");
    Assumptions.assumeTrue(mockUrl != null);
    Client client = Client.builder()
        .uri(mockUrl)
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("test-key")
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    agenticService = ManagementServiceBuilder.buildServices(client).getAgenticService();
  }

  @Test
  void testSearchIdentities() throws IOException {
    Object res = agenticService.searchIdentities(AgenticIdentitySearchOptions.builder()
        .userId(USER_ID).clientId("cl1").resourceId("r1").consentId("c1").page(2).limit(5).build());
    assertLastCall("/v1/mgmt/agentic/identities/search",
        "{\"userId\":\"" + USER_ID + "\",\"clientId\":\"cl1\",\"resourceId\":\"r1\",\"consentId\":\"c1\","
            + "\"page\":2,\"limit\":5}", res);
  }

  @Test
  void testRevokeIdentities() throws IOException {
    Object res = agenticService.revokeIdentities(AgenticIdentityRevokeOptions.builder()
        .userId(USER_ID).clientId("cl1").resourceId("r1").consentId("c1").build());
    assertLastCall("/v1/mgmt/agentic/identities/revoke",
        "{\"userId\":\"" + USER_ID + "\",\"clientId\":\"cl1\",\"resourceId\":\"r1\",\"consentId\":\"c1\"}", res);
  }

  @Test
  void testGetGovernanceSettings() throws IOException {
    Object res = agenticService.getGovernanceSettings();
    assertLastCall("/v1/mgmt/agentic/governance/settings", "{}", res);
  }

  @Test
  void testSetGovernanceSettings() throws IOException {
    Object res = agenticService.setGovernanceSettings(GovernanceSettings.builder()
        .configured(true).autoApproval(true).suiteDisabled(true).build());
    assertLastCall("/v1/mgmt/agentic/governance/settings",
        "{\"configured\":true,\"autoApproval\":true,\"suiteDisabled\":true}", res);
  }

  @Test
  void testSearchGovernanceAccessRequests() throws IOException {
    Object res = agenticService.searchGovernanceAccessRequests(GovernanceAccessRequestSearchOptions.builder()
        .limit(10)
        .page(1)
        .searchFields(Collections.singletonList(GovernanceAccessRequestSearchField.builder()
            .field("status").valArr(Arrays.asList("pending", "approved")).valStr("x").negative(true)
            .valType("string").build()))
        .sort(Collections.singletonList(GovernanceAccessRequestSortField.builder()
            .field("createdTime").desc(true).build()))
        .text("agent")
        .build());
    assertLastCall("/v1/mgmt/agentic/governance/requests/search",
        "{\"limit\":10,\"page\":1,\"searchFields\":[{\"field\":\"status\",\"valArr\":[\"pending\",\"approved\"],"
            + "\"valStr\":\"x\",\"negative\":true,\"valType\":\"string\"}],"
            + "\"sort\":[{\"field\":\"createdTime\",\"desc\":true}],\"text\":\"agent\"}", res);
  }

  @Test
  void testGetGovernanceAccessRequest() throws IOException {
    Object res = agenticService.getGovernanceAccessRequest("ar1");
    assertLastCall("/v1/mgmt/agentic/governance/requests/get", "{\"id\":\"ar1\"}", res);
  }

  @Test
  void testResolveGovernanceAccessRequest() throws IOException {
    Object res = agenticService.resolveGovernanceAccessRequest(GovernanceAccessRequestResolveRequest.builder()
        .id("ar1").action("approve").resolutionNote("ok").build());
    assertLastCall("/v1/mgmt/agentic/governance/requests/resolve",
        "{\"id\":\"ar1\",\"action\":\"approve\",\"resolutionNote\":\"ok\"}", res);
  }

  @Test
  void testCreateGovernanceAccessRequest() throws IOException {
    Object res = agenticService.createGovernanceAccessRequest(GovernanceAccessRequestCreateRequest.builder()
        .requestorUserId(USER_ID)
        .clientName("cn")
        .clientType("ct")
        .grantTypes(Arrays.asList("authCode", "clientCredentials"))
        .targets(Collections.singletonList(GovernanceAccessRequestTarget.builder()
            .name("api").kind("resource").targetId("t1").scopes(Collections.singletonList("read")).allScopes(true)
            .build()))
        .scopes(Collections.singletonList("s1"))
        .justification("why")
        .build());
    assertLastCall("/v1/mgmt/agentic/governance/requests/create",
        "{\"requestorUserId\":\"" + USER_ID + "\",\"clientName\":\"cn\",\"clientType\":\"ct\","
            + "\"grantTypes\":[\"authCode\",\"clientCredentials\"],"
            + "\"targets\":[{\"name\":\"api\",\"kind\":\"resource\",\"targetId\":\"t1\",\"scopes\":[\"read\"],"
            + "\"allScopes\":true}],\"scopes\":[\"s1\"],\"justification\":\"why\"}", res);
  }

  // Checks that the backend decoded every field the test set, and that every response field reached the
  // parsed result. The access request version is the only response field the SDK does not expose.
  private void assertLastCall(String expectedPath, String expectedRequest, Object parsed) throws IOException {
    JsonNode last = fetchLast();
    assertEquals(expectedPath, last.get("path").asText());
    assertEquals(200, last.get("status").asInt());
    assertContains("request", mapper.readTree(expectedRequest), last.get("request"));
    JsonNode responseBody = last.get("responseBody");
    assertNotNull(responseBody);
    JsonNode parsedNode = mapper.valueToTree(parsed);
    assertContains("response", stripVersion(responseBody), parsedNode);
  }

  private JsonNode stripVersion(JsonNode node) {
    if (node.isObject()) {
      ((ObjectNode) node).remove("version");
      Iterator<JsonNode> it = node.elements();
      while (it.hasNext()) {
        stripVersion(it.next());
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        stripVersion(child);
      }
    }
    return node;
  }

  // Every field of expected must be present in actual with the same value (64-bit integers may be strings).
  private static void assertContains(String at, JsonNode expected, JsonNode actual) {
    assertNotNull(actual, at + " is missing");
    if (expected.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> f = fields.next();
        assertTrue(actual.has(f.getKey()), at + "." + f.getKey() + " is missing");
        assertContains(at + "." + f.getKey(), f.getValue(), actual.get(f.getKey()));
      }
    } else if (expected.isArray()) {
      assertEquals(expected.size(), actual.size(), at + " size");
      for (int i = 0; i < expected.size(); i++) {
        assertContains(at + "[" + i + "]", expected.get(i), actual.get(i));
      }
    } else {
      assertEquals(expected.asText(), actual.asText(), at);
    }
  }

  private JsonNode fetchLast() throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(mockUrl + "/__mock/last").openConnection();
    conn.setRequestMethod("GET");
    try (InputStream in = conn.getInputStream()) {
      return mapper.readTree(in);
    } finally {
      conn.disconnect();
    }
  }
}
