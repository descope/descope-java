package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.descope.model.agentic.AgenticIdentity;
import com.descope.model.agentic.AgenticIdentityConsent;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.client.Client;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Runs against the mock backend at DESCOPE_MOCK_URL, skipped when it is unset.
class AgenticServiceMockTest {

  private static final String USER_ID = "U2abcdefghijklmnopqrstuvwxyz";

  private String mockUrl;
  private AgenticService agenticService;

  @BeforeEach
  void setUp() {
    mockUrl = System.getenv("DESCOPE_MOCK_URL");
    Assumptions.assumeTrue(mockUrl != null);
    Client client = Client.builder()
        .uri(mockUrl)
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("someManagementKey")
        .build();
    agenticService = ManagementServiceBuilder.buildServices(client).getAgenticService();
  }

  private JsonNode lastCall() throws Exception {
    HttpURLConnection conn = (HttpURLConnection) new URL(mockUrl + "/__mock/last").openConnection();
    conn.setRequestMethod("GET");
    try (InputStream in = conn.getInputStream()) {
      return new ObjectMapper().readTree(in);
    } finally {
      conn.disconnect();
    }
  }

  @Test
  void testSearchIdentities() throws Exception {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    AgenticIdentitySearchResponse res = agenticService.searchIdentities(AgenticIdentitySearchOptions.builder()
        .userId(USER_ID).clientId("c1").resourceId("r1").consentId("cs1").page(1).limit(10).build());

    JsonNode last = lastCall();
    assertEquals("POST", last.get("method").asText());
    assertEquals("/v1/mgmt/agentic/identities/search", last.get("path").asText());
    JsonNode req = last.get("request");
    assertEquals(USER_ID, req.get("userId").asText());
    assertEquals("c1", req.get("clientId").asText());
    assertEquals("r1", req.get("resourceId").asText());
    assertEquals("cs1", req.get("consentId").asText());
    assertEquals(1, req.get("page").asInt());
    assertEquals(10, req.get("limit").asInt());

    JsonNode body = last.get("responseBody");
    assertEquals(body.get("total").asInt(), res.getTotal());
    assertEquals(body.get("identities").size(), res.getIdentities().length);
    JsonNode expected = body.get("identities").get(0);
    AgenticIdentity identity = res.getIdentities()[0];
    assertEquals(expected.get("agentName").asText(), identity.getAgentName());
    assertEquals(expected.get("resourceId").asText(), identity.getResourceId());
    assertEquals(expected.get("clientId").asText(), identity.getClientId());
    assertEquals(expected.get("createdBy").asText(), identity.getCreatedBy());
    JsonNode expectedConsent = expected.get("consent");
    AgenticIdentityConsent consent = identity.getConsent();
    assertNotNull(consent);
    assertEquals(expectedConsent.get("id").asText(), consent.getId());
    assertEquals(expectedConsent.get("appId").asText(), consent.getAppId());
    assertEquals(expectedConsent.get("userId").asText(), consent.getUserId());
    assertEquals(expectedConsent.get("scopes").size(), consent.getScopes().length);
    assertEquals(expectedConsent.get("scopes").get(0).asText(), consent.getScopes()[0]);
    assertEquals(expectedConsent.get("grantedBy").asText(), consent.getGrantedBy());
    assertEquals(expectedConsent.get("createdTime").asLong(), consent.getCreatedTime());
    assertEquals(expectedConsent.get("tenantId").asText(), consent.getTenantId());
    assertEquals(expectedConsent.get("ExpireTime").asLong(), consent.getExpireTime());
    assertEquals(expectedConsent.get("modifiedTime").asLong(), consent.getModifiedTime());
    assertEquals(expectedConsent.get("authMethod").asText(), consent.getAuthMethod());
  }

  @Test
  void testRevokeIdentities() throws Exception {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    int revoked = agenticService.revokeIdentities(AgenticIdentityRevokeOptions.builder()
        .userId(USER_ID).clientId("c1").resourceId("r1").consentId("cs1").build());

    JsonNode last = lastCall();
    assertEquals("POST", last.get("method").asText());
    assertEquals("/v1/mgmt/agentic/identities/revoke", last.get("path").asText());
    JsonNode req = last.get("request");
    assertEquals(USER_ID, req.get("userId").asText());
    assertEquals("c1", req.get("clientId").asText());
    assertEquals("r1", req.get("resourceId").asText());
    assertEquals("cs1", req.get("consentId").asText());
    assertEquals(last.get("responseBody").get("revoked").asInt(), revoked);
  }
}
