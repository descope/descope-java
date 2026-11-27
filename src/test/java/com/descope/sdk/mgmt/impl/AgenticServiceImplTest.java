package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticGovernanceSettings;
import com.descope.model.agentic.AgenticIdentitiesRevokeOptions;
import com.descope.model.agentic.AgenticIdentity;
import com.descope.model.agentic.AgenticIdentityConsent;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.agentic.GovernanceAccessRequest;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.GovernanceAccessRequestTarget;
import com.descope.model.agentic.GovernanceGrantType;
import com.descope.model.agentic.SearchField;
import com.descope.model.agentic.SortField;
import com.descope.model.agentic.XaaAgenticIdentitiesResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AgenticServiceImplTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String BASE_URL = "https://api.descope.com";
  private static final ObjectMapper READER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final ObjectMapper WRITER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

  private static final String TARGET_JSON = "{\"name\":\"api\",\"kind\":\"resource\",\"targetId\":\"r1\","
      + "\"scopes\":[\"read\"],\"allScopes\":true}";
  private static final String REQUEST_JSON = "{\"id\":\"req1\",\"requestorUserId\":\"u1\",\"requestorName\":\"User\","
      + "\"clientType\":\"agent\",\"grantTypes\":[\"authCode\"],\"targets\":[" + TARGET_JSON + "],"
      + "\"scopes\":[\"s1\"],\"justification\":\"why\",\"status\":\"approved\",\"resolvedByUserId\":\"u2\","
      + "\"resolvedByName\":\"Admin\",\"resolutionNote\":\"ok\",\"clientId\":\"c1\",\"clientAppId\":\"a1\","
      + "\"createdTime\":\"1700000000\",\"modifiedTime\":1700000001,\"clientName\":\"bot\",\"clientDeleted\":true,"
      + "\"unknownField\":1}";
  private static final String IDENTITY_JSON = "{\"consent\":{\"id\":\"cons1\",\"appId\":\"app1\",\"userId\":\"u1\","
      + "\"scopes\":[\"s1\"],\"grantedBy\":\"u1\",\"createdTime\":10,\"tenantId\":\"t1\",\"ExpireTime\":20,"
      + "\"modifiedTime\":30,\"authMethod\":\"idJag\",\"blocked\":true},\"agentName\":\"agent\","
      + "\"resourceId\":\"r1\",\"clientId\":\"c1\",\"createdBy\":\"u1\"}";

  private final AgenticServiceImpl agenticService = new AgenticServiceImpl(Client.builder()
      .uri(BASE_URL)
      .projectId(PROJECT_ID)
      .managementKey(MANAGEMENT_KEY)
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build());

  /** Records the request a call sends and answers it with the given JSON. */
  private static class Recorded {
    String method;
    URI uri;
    String body;
    String authorization;
  }

  @SuppressWarnings("unchecked")
  private static <T> T call(String responseJson, Recorded rec, Supplier<T> call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doAnswer(inv -> {
      rec.method = "POST";
      rec.uri = inv.getArgument(0);
      rec.body = WRITER.writeValueAsString(inv.getArgument(1));
      return READER.readValue(responseJson, (Class<?>) inv.getArgument(2));
    }).when(apiProxy).post(any(), any(), any());
    doAnswer(inv -> {
      rec.method = "GET";
      rec.uri = inv.getArgument(0);
      return READER.readValue(responseJson, (Class<?>) inv.getArgument(1));
    }).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        rec.authorization = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      return call.get();
    }
  }

  private static void assertRequest(Recorded rec, String method, String path, String body) {
    assertEquals(method, rec.method);
    assertEquals(BASE_URL + path, rec.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, rec.authorization);
    assertEquals(body, rec.body);
  }

  private static void assertFullRequest(GovernanceAccessRequest r) {
    assertThat(r.getId()).isEqualTo("req1");
    assertThat(r.getRequestorUserId()).isEqualTo("u1");
    assertThat(r.getRequestorName()).isEqualTo("User");
    assertThat(r.getClientType()).isEqualTo("agent");
    assertThat(r.getGrantTypes()).containsExactly(GovernanceGrantType.AUTH_CODE);
    assertThat(r.getTargets()).containsExactly(GovernanceAccessRequestTarget.builder().name("api").kind("resource")
        .targetId("r1").scopes(Arrays.asList("read")).allScopes(true).build());
    assertThat(r.getScopes()).containsExactly("s1");
    assertThat(r.getJustification()).isEqualTo("why");
    assertThat(r.getStatus()).isEqualTo("approved");
    assertThat(r.getResolvedByUserId()).isEqualTo("u2");
    assertThat(r.getResolvedByName()).isEqualTo("Admin");
    assertThat(r.getResolutionNote()).isEqualTo("ok");
    assertThat(r.getClientId()).isEqualTo("c1");
    assertThat(r.getClientAppId()).isEqualTo("a1");
    assertThat(r.getCreatedTime()).isEqualTo(1700000000L);
    assertThat(r.getModifiedTime()).isEqualTo(1700000001L);
    assertThat(r.getClientName()).isEqualTo("bot");
    assertThat(r.getClientDeleted()).isTrue();
  }

  private static void assertFullIdentities(List<AgenticIdentity> identities) {
    assertThat(identities).containsExactly(AgenticIdentity.builder()
        .consent(AgenticIdentityConsent.builder().id("cons1").appId("app1").userId("u1")
            .scopes(Arrays.asList("s1")).grantedBy("u1").createdTime(10L).tenantId("t1").expireTime(20L)
            .modifiedTime(30L).authMethod("idJag").blocked(true).build())
        .agentName("agent").resourceId("r1").clientId("c1").createdBy("u1").build());
  }

  private static void assertInvalid(String message, Runnable call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown = assertThrows(ServerCommonException.class, call::run);
      assertEquals(message, thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  void testCreateGovernanceAccessRequest() {
    Recorded rec = new Recorded();
    GovernanceAccessRequest res = call(REQUEST_JSON, rec, () -> agenticService.createGovernanceAccessRequest(
        GovernanceAccessRequestCreateRequest.builder()
            .requestorUserId("u1")
            .clientName("bot")
            .clientType("agent")
            .grantTypes(Arrays.asList(GovernanceGrantType.AUTH_CODE, GovernanceGrantType.CIBA))
            .targets(Arrays.asList(GovernanceAccessRequestTarget.builder().name("api").kind("resource")
                .scopes(Arrays.asList("read")).build()))
            .scopes(Arrays.asList("s1"))
            .justification("why")
            .build()));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/governance/requests/create",
        "{\"requestorUserId\":\"u1\",\"clientName\":\"bot\",\"clientType\":\"agent\","
            + "\"grantTypes\":[\"authCode\",\"ciba\"],\"targets\":[{\"name\":\"api\",\"kind\":\"resource\","
            + "\"scopes\":[\"read\"]}],\"scopes\":[\"s1\"],\"justification\":\"why\"}");
    assertFullRequest(res);
  }

  @Test
  void testCreateGovernanceAccessRequestInvalid() {
    assertInvalid("The request argument is invalid", () -> agenticService.createGovernanceAccessRequest(null));
    assertInvalid("The request.requestorUserId argument is invalid", () -> agenticService
        .createGovernanceAccessRequest(GovernanceAccessRequestCreateRequest.builder().clientName("bot").build()));
  }

  @Test
  void testLoadGovernanceAccessRequest() {
    Recorded rec = new Recorded();
    GovernanceAccessRequest res = call(REQUEST_JSON, rec, () -> agenticService.loadGovernanceAccessRequest("req1"));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/governance/requests/get", "{\"id\":\"req1\"}");
    assertFullRequest(res);
  }

  @Test
  void testLoadGovernanceAccessRequestInvalid() {
    assertInvalid("The id argument is invalid", () -> agenticService.loadGovernanceAccessRequest(""));
  }

  @Test
  void testResolveGovernanceAccessRequest() {
    Recorded rec = new Recorded();
    GovernanceAccessRequest res = call(REQUEST_JSON, rec,
        () -> agenticService.resolveGovernanceAccessRequest("req1", "approve", "ok"));
    // Map key order is unspecified, so compare the body as JSON
    assertRequest(rec, "POST", "/v1/mgmt/agentic/governance/requests/resolve", rec.body);
    assertEquals(tree("{\"id\":\"req1\",\"action\":\"approve\",\"resolutionNote\":\"ok\"}"), tree(rec.body));
    assertFullRequest(res);
  }

  @Test
  void testResolveGovernanceAccessRequestWithoutNote() {
    Recorded rec = new Recorded();
    call(REQUEST_JSON, rec, () -> agenticService.resolveGovernanceAccessRequest("req1", "deny", null));
    assertEquals(tree("{\"id\":\"req1\",\"action\":\"deny\"}"), tree(rec.body));
  }

  @Test
  void testResolveGovernanceAccessRequestInvalid() {
    assertInvalid("The id argument is invalid", () -> agenticService.resolveGovernanceAccessRequest("", "deny", null));
    assertInvalid("The action argument is invalid",
        () -> agenticService.resolveGovernanceAccessRequest("req1", " ", null));
  }

  @Test
  void testSearchGovernanceAccessRequests() {
    Recorded rec = new Recorded();
    GovernanceAccessRequestSearchResponse res = call("{\"requests\":[" + REQUEST_JSON + "],\"total\":7}", rec,
        () -> agenticService.searchGovernanceAccessRequests(GovernanceAccessRequestSearchOptions.builder()
            .limit(10)
            .page(1)
            .searchFields(Arrays.asList(SearchField.builder().field("status").valArr(Arrays.asList("pending"))
                .valStr("pending").negative(false).valType("string").build()))
            .sort(Arrays.asList(SortField.builder().field("createdTime").desc(true).build()))
            .text("bot")
            .build()));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/governance/requests/search",
        "{\"limit\":10,\"page\":1,\"searchFields\":[{\"field\":\"status\",\"valArr\":[\"pending\"],"
            + "\"valStr\":\"pending\",\"negative\":false,\"valType\":\"string\"}],"
            + "\"sort\":[{\"field\":\"createdTime\",\"desc\":true}],\"text\":\"bot\"}");
    assertEquals(7, res.getTotal());
    assertThat(res.getRequests()).hasSize(1);
    assertFullRequest(res.getRequests().get(0));
  }

  @Test
  void testSearchGovernanceAccessRequestsInvalid() {
    assertInvalid("The options argument is invalid", () -> agenticService.searchGovernanceAccessRequests(null));
  }

  @Test
  void testLoadGovernanceSettings() {
    Recorded rec = new Recorded();
    AgenticGovernanceSettings res = call("{\"configured\":true,\"autoApproval\":false,\"suiteDisabled\":true}", rec,
        () -> agenticService.loadGovernanceSettings());
    assertRequest(rec, "GET", "/v1/mgmt/agentic/governance/settings", null);
    assertThat(res).isEqualTo(AgenticGovernanceSettings.builder().configured(true).autoApproval(false)
        .suiteDisabled(true).build());
  }

  @Test
  void testUpdateGovernanceSettings() {
    Recorded rec = new Recorded();
    AgenticGovernanceSettings res = call("{\"configured\":true,\"autoApproval\":true,\"suiteDisabled\":false}", rec,
        () -> agenticService.updateGovernanceSettings(AgenticGovernanceSettings.builder().autoApproval(true).build()));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/governance/settings", "{\"autoApproval\":true}");
    assertThat(res).isEqualTo(AgenticGovernanceSettings.builder().configured(true).autoApproval(true)
        .suiteDisabled(false).build());
  }

  @Test
  void testUpdateGovernanceSettingsInvalid() {
    assertInvalid("The settings argument is invalid", () -> agenticService.updateGovernanceSettings(null));
  }

  @Test
  void testRevokeAgenticIdentities() {
    Recorded rec = new Recorded();
    int revoked = call("{\"revoked\":3}", rec, () -> agenticService.revokeAgenticIdentities(
        AgenticIdentitiesRevokeOptions.builder().userId("u1").clientId("c1").resourceId("r1").consentId("cons1")
            .build()));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/identities/revoke",
        "{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cons1\"}");
    assertEquals(3, revoked);
  }

  @Test
  void testRevokeAgenticIdentitiesInvalid() {
    assertInvalid("The options argument is invalid", () -> agenticService.revokeAgenticIdentities(null));
    assertInvalid("The userId, clientId, resourceId or consentId argument is invalid",
        () -> agenticService.revokeAgenticIdentities(AgenticIdentitiesRevokeOptions.builder().userId("").build()));
  }

  @Test
  void testSearchAgenticIdentities() {
    Recorded rec = new Recorded();
    AgenticIdentitySearchResponse res = call("{\"identities\":[" + IDENTITY_JSON + "],\"total\":4}", rec,
        () -> agenticService.searchAgenticIdentities(AgenticIdentitySearchOptions.builder().userId("u1")
            .clientId("c1").resourceId("r1").consentId("cons1").page(2).limit(5).build()));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/identities/search",
        "{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cons1\",\"page\":2,"
            + "\"limit\":5}");
    assertEquals(4, res.getTotal());
    assertFullIdentities(res.getIdentities());
  }

  @Test
  void testSearchAgenticIdentitiesInvalid() {
    assertInvalid("The options argument is invalid", () -> agenticService.searchAgenticIdentities(null));
  }

  @Test
  void testBlockXaaAgenticIdentities() {
    Recorded rec = new Recorded();
    XaaAgenticIdentitiesResponse res = call("{\"identities\":[" + IDENTITY_JSON + "],\"skippedConsentIds\":[\"x\"]}",
        rec, () -> agenticService.blockXaaAgenticIdentities(Arrays.asList("cons1", "x")));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/identities/xaa/block", "{\"consentIds\":[\"cons1\",\"x\"]}");
    assertFullIdentities(res.getIdentities());
    assertThat(res.getSkippedConsentIds()).containsExactly("x");
  }

  @Test
  void testUnblockXaaAgenticIdentities() {
    Recorded rec = new Recorded();
    XaaAgenticIdentitiesResponse res = call("{\"identities\":[" + IDENTITY_JSON + "],\"skippedConsentIds\":[\"x\"]}",
        rec, () -> agenticService.unblockXaaAgenticIdentities(Arrays.asList("cons1", "x")));
    assertRequest(rec, "POST", "/v1/mgmt/agentic/identities/xaa/unblock", "{\"consentIds\":[\"cons1\",\"x\"]}");
    assertFullIdentities(res.getIdentities());
    assertThat(res.getSkippedConsentIds()).containsExactly("x");
  }

  @Test
  void testBlockXaaAgenticIdentitiesInvalid() {
    assertInvalid("The consentIds argument is invalid", () -> agenticService.blockXaaAgenticIdentities(null));
    assertInvalid("The consentIds argument is invalid",
        () -> agenticService.unblockXaaAgenticIdentities(Collections.emptyList()));
  }

  @Test
  void testNullFieldsInResponse() {
    Recorded rec = new Recorded();
    AgenticIdentitySearchResponse res = call("{\"identities\":[{\"consent\":null}]}", rec,
        () -> agenticService.searchAgenticIdentities(AgenticIdentitySearchOptions.builder().build()));
    assertEquals("{}", rec.body);
    assertEquals(0, res.getTotal());
    assertThat(res.getIdentities().get(0).getConsent()).isNull();
  }

  private static JsonNode tree(String json) {
    try {
      return READER.readTree(json);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
