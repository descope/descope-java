package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.descope.model.agentic.AgenticIdentity;
import com.descope.model.agentic.AgenticIdentityConsent;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.client.Client;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgenticServiceMockTest {

  private static final String PROJECT_ID = "P0000000000000000000000000000";
  private static final String MANAGEMENT_KEY = "test-key";
  private static final String USER_ID = "U2abcdefghijklmnopqrstuvwxyz";

  private AgenticService agenticService;

  @BeforeEach
  void setUp() {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    Client client = Client.builder()
        .uri(System.getenv("DESCOPE_MOCK_URL"))
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .build();
    agenticService = new AgenticServiceImpl(client);
  }

  private static JsonNode lastCall() throws Exception {
    URL url = new URL(System.getenv("DESCOPE_MOCK_URL") + "/__mock/last");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
    final AgenticIdentitySearchResponse res = agenticService.searchIdentities(AgenticIdentitySearchOptions.builder()
        .userId(USER_ID).clientId("client1").resourceId("res1").consentId("cons1").page(1).limit(10).build());

    JsonNode last = lastCall();
    assertEquals("POST", last.get("method").asText());
    assertEquals("/v1/mgmt/agentic/identities/search", last.get("path").asText());
    JsonNode request = last.get("request");
    assertEquals(USER_ID, request.get("userId").asText());
    assertEquals("client1", request.get("clientId").asText());
    assertEquals("res1", request.get("resourceId").asText());
    assertEquals("cons1", request.get("consentId").asText());
    assertEquals(1, request.get("page").asInt());
    assertEquals(10, request.get("limit").asInt());

    JsonNode body = last.get("responseBody");
    assertEquals(body.get("total").asInt(), res.getTotal());
    JsonNode identities = body.get("identities");
    assertEquals(identities.size(), res.getIdentities().length);
    JsonNode expected = identities.get(0);
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
    String[] scopes = new String[expectedConsent.get("scopes").size()];
    for (int i = 0; i < scopes.length; i++) {
      scopes[i] = expectedConsent.get("scopes").get(i).asText();
    }
    assertArrayEquals(scopes, consent.getScopes());
    assertEquals(expectedConsent.get("grantedBy").asText(), consent.getGrantedBy());
    assertEquals(expectedConsent.get("createdTime").asInt(), consent.getCreatedTime());
    assertEquals(expectedConsent.get("tenantId").asText(), consent.getTenantId());
    assertEquals(expectedConsent.get("ExpireTime").asInt(), consent.getExpireTime());
    assertEquals(expectedConsent.get("modifiedTime").asInt(), consent.getModifiedTime());
    assertEquals(expectedConsent.get("authMethod").asText(), consent.getAuthMethod());
  }

  @Test
  void testRevokeIdentities() throws Exception {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    final int revoked = agenticService.revokeIdentities(AgenticIdentityRevokeOptions.builder()
        .userId(USER_ID).clientId("client1").resourceId("res1").consentId("cons1").build());

    JsonNode last = lastCall();
    assertEquals("POST", last.get("method").asText());
    assertEquals("/v1/mgmt/agentic/identities/revoke", last.get("path").asText());
    JsonNode request = last.get("request");
    assertEquals(USER_ID, request.get("userId").asText());
    assertEquals("client1", request.get("clientId").asText());
    assertEquals("res1", request.get("resourceId").asText());
    assertEquals("cons1", request.get("consentId").asText());
    assertEquals(last.get("responseBody").get("revoked").asInt(), revoked);
  }
}
