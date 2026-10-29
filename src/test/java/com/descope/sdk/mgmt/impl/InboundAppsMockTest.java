package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.inbound.AgenticIdentityRevokeOptions;
import com.descope.model.inbound.AgenticIdentitySearchOptions;
import com.descope.model.inbound.AgenticIdentitySearchResponse;
import com.descope.sdk.mgmt.InboundAppsService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Runs against the mock backend at DESCOPE_MOCK_URL; skipped when it is not set. */
class InboundAppsMockTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final ObjectMapper MAPPER =
      new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private String mockUrl;
  private InboundAppsService inboundAppsService;

  @BeforeEach
  void setUp() {
    mockUrl = System.getenv("DESCOPE_MOCK_URL");
    Assumptions.assumeTrue(mockUrl != null);
    Client client = Client.builder()
        .uri(mockUrl)
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    inboundAppsService = ManagementServiceBuilder.buildServices(client).getInboundAppsService();
  }

  @Test
  void testSearchAgenticIdentities() throws Exception {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    AgenticIdentitySearchOptions options = AgenticIdentitySearchOptions.builder()
        .userId("U2abcdefghijklmnopqrstuvwxyz")
        .clientId("client1")
        .resourceId("resource1")
        .consentId("consent1")
        .page(1)
        .limit(10)
        .build();
    AgenticIdentitySearchResponse res = inboundAppsService.searchAgenticIdentities(options);

    JsonNode last = lastCall();
    assertThat(last.get("path").asText()).isEqualTo("/v1/mgmt/agentic/identities/search");
    assertThat(last.get("request")).isEqualTo(MAPPER.valueToTree(options));
    assertThat(last.get("request").size()).isEqualTo(6);
    assertThat(MAPPER.<JsonNode>valueToTree(res)).isEqualTo(last.get("responseBody"));
  }

  @Test
  void testRevokeAgenticIdentities() throws Exception {
    Assumptions.assumeTrue(System.getenv("DESCOPE_MOCK_URL") != null);
    AgenticIdentityRevokeOptions options = AgenticIdentityRevokeOptions.builder()
        .userId("U2abcdefghijklmnopqrstuvwxyz")
        .clientId("client1")
        .resourceId("resource1")
        .consentId("consent1")
        .build();
    int revoked = inboundAppsService.revokeAgenticIdentities(options);

    JsonNode last = lastCall();
    assertThat(last.get("path").asText()).isEqualTo("/v1/mgmt/agentic/identities/revoke");
    assertThat(last.get("request")).isEqualTo(MAPPER.valueToTree(options));
    assertThat(last.get("request").size()).isEqualTo(4);
    assertThat(last.get("responseBody").size()).isEqualTo(1);
    assertThat(revoked).isEqualTo(last.get("responseBody").get("revoked").asInt());
  }

  private JsonNode lastCall() throws Exception {
    HttpURLConnection conn = (HttpURLConnection) new URL(mockUrl + "/__mock/last").openConnection();
    try (InputStream in = conn.getInputStream()) {
      return MAPPER.readTree(in);
    } finally {
      conn.disconnect();
    }
  }
}
