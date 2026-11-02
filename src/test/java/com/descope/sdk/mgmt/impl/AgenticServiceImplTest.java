package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.GovernanceAccessRequestTarget;
import com.descope.model.agentic.GovernanceSearchField;
import com.descope.model.agentic.GovernanceSettings;
import com.descope.model.agentic.GovernanceSortField;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AgenticServiceImplTest {

  private static final String PROJECT_ID = TestUtils.PROJECT_ID;
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String BASE_URL = "https://api.descope.com";

  // Same settings as the SDK's HTTP proxy
  private static final ObjectMapper READER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final ObjectMapper WRITER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

  private static final String REQUEST_JSON = "{\"id\":\"rid\",\"requestorUserId\":\"uid\",\"requestorName\":\"uname\","
      + "\"clientType\":\"ctype\",\"grantTypes\":[\"authCode\"],\"targets\":[{\"name\":\"tname\",\"kind\":\"resource\","
      + "\"targetId\":\"tid\",\"scopes\":[\"s1\"],\"allScopes\":true}],\"scopes\":[\"s2\"],\"justification\":\"why\","
      + "\"status\":\"approved\",\"resolvedByUserId\":\"\",\"resolvedByName\":\"Management API\","
      + "\"resolutionNote\":\"note\",\"clientId\":\"cid\",\"clientAppId\":\"capp\",\"createdTime\":\"1234567890123\","
      + "\"modifiedTime\":\"1234567890124\",\"version\":\"3\",\"clientName\":\"cname\",\"clientDeleted\":true,"
      + "\"someNewField\":1}";

  private final AgenticService agenticService = ManagementServiceBuilder.buildServices(
      Client.builder().uri(BASE_URL).projectId(PROJECT_ID).managementKey(MANAGEMENT_KEY)
          .sdkInfo(SdkInfo.builder().name("java").build()).build())
      .getAgenticService();

  @Test
  void testMissingArguments() {
    assertInvalid("options", () -> agenticService.searchAgenticIdentities(null));
    assertInvalid("options", () -> agenticService.revokeAgenticIdentities(null));
    assertInvalid("settings", () -> agenticService.setGovernanceSettings(null));
    assertInvalid("options", () -> agenticService.searchGovernanceAccessRequests(null));
    assertInvalid("options.sort.field", () -> agenticService.searchGovernanceAccessRequests(
        GovernanceAccessRequestSearchOptions.builder()
            .sort(Collections.singletonList(GovernanceSortField.builder().desc(true).build())).build()));
    assertInvalid("id", () -> agenticService.getGovernanceAccessRequest(null));
    assertInvalid("id", () -> agenticService.getGovernanceAccessRequest(""));
    assertInvalid("request", () -> agenticService.resolveGovernanceAccessRequest(null));
    assertInvalid("request.id", () -> agenticService.resolveGovernanceAccessRequest(
        GovernanceAccessRequestResolveRequest.builder().action("approve").build()));
    assertInvalid("request.action", () -> agenticService.resolveGovernanceAccessRequest(
        GovernanceAccessRequestResolveRequest.builder().id("rid").build()));
    assertInvalid("request", () -> agenticService.createGovernanceAccessRequest(null));
    assertInvalid("request.requestorUserId", () -> agenticService.createGovernanceAccessRequest(
        GovernanceAccessRequestCreateRequest.builder().grantTypes(Collections.singletonList("authCode")).build()));
    assertInvalid("request.grantTypes", () -> agenticService.createGovernanceAccessRequest(
        GovernanceAccessRequestCreateRequest.builder().requestorUserId("uid")
            .grantTypes(Arrays.asList("authCode", "")).build()));
  }

  @Test
  void testSearchAgenticIdentities() {
    AgenticIdentitySearchResponse parsed = parse("{\"identities\":[{\"consent\":{\"id\":\"cs1\",\"appId\":\"app1\","
        + "\"userId\":\"u1\",\"scopes\":[\"s1\"],\"grantedBy\":\"g1\",\"createdTime\":7,\"tenantId\":\"t1\","
        + "\"ExpireTime\":8,\"modifiedTime\":9,\"authMethod\":\"idJag\"},\"agentName\":\"agent\","
        + "\"resourceId\":\"r1\",\"clientId\":\"c1\",\"createdBy\":\"cb\"},{\"consent\":null,\"agentName\":\"\"}],"
        + "\"total\":2,\"unknown\":true}", AgenticIdentitySearchResponse.class);
    assertEquals(2, parsed.getTotal());
    AgenticIdentity identity = parsed.getIdentities().get(0);
    assertEquals("agent", identity.getAgentName());
    assertEquals("r1", identity.getResourceId());
    assertEquals("c1", identity.getClientId());
    assertEquals("cb", identity.getCreatedBy());
    AgenticIdentityConsent consent = identity.getConsent();
    assertEquals("cs1", consent.getId());
    assertEquals("app1", consent.getAppId());
    assertEquals("u1", consent.getUserId());
    assertEquals(Collections.singletonList("s1"), consent.getScopes());
    assertEquals("g1", consent.getGrantedBy());
    assertEquals(7, consent.getCreatedTime());
    assertEquals("t1", consent.getTenantId());
    assertEquals(8, consent.getExpireTime());
    assertEquals(9, consent.getModifiedTime());
    assertEquals("idJag", consent.getAuthMethod());
    assertNull(parsed.getIdentities().get(1).getConsent());

    AgenticIdentitySearchOptions options = AgenticIdentitySearchOptions.builder().userId("u1").clientId("c1")
        .resourceId("r1").consentId("cs1").page(1).limit(10).build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(AgenticIdentitySearchResponse.class));
    String auth = withProxy(apiProxy, () -> assertSame(parsed, agenticService.searchAgenticIdentities(options)));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/identities/search",
        "{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cs1\",\"page\":1,\"limit\":10}");
  }

  @Test
  void testRevokeAgenticIdentities() {
    AgenticIdentityRevokeResponse parsed = parse("{\"revoked\":3}", AgenticIdentityRevokeResponse.class);
    AgenticIdentityRevokeOptions options = AgenticIdentityRevokeOptions.builder().userId("u1").clientId("c1")
        .resourceId("r1").consentId("cs1").build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(AgenticIdentityRevokeResponse.class));
    String auth = withProxy(apiProxy, () -> assertEquals(3, agenticService.revokeAgenticIdentities(options)));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/identities/revoke",
        "{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cs1\"}");
  }

  @Test
  void testGetGovernanceSettings() {
    GovernanceSettings parsed = parse("{\"configured\":true,\"autoApproval\":false,\"suiteDisabled\":true}",
        GovernanceSettings.class);
    assertTrue(parsed.getConfigured());
    assertFalse(parsed.getAutoApproval());
    assertTrue(parsed.getSuiteDisabled());
    assertNull(parse("{}", GovernanceSettings.class).getConfigured());

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).get(any(), eq(GovernanceSettings.class));
    String auth = withProxy(apiProxy, () -> assertSame(parsed, agenticService.getGovernanceSettings()));
    assertAuth(auth);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), eq(GovernanceSettings.class));
    assertEquals(BASE_URL + "/v1/mgmt/agentic/governance/settings", uri.getValue().toString());
  }

  @Test
  void testSetGovernanceSettings() {
    GovernanceSettings parsed = parse("{\"configured\":true,\"autoApproval\":true,\"suiteDisabled\":false}",
        GovernanceSettings.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(GovernanceSettings.class));
    // Unset fields are left out of the request, so the server keeps their stored values
    GovernanceSettings settings = GovernanceSettings.builder().autoApproval(true).build();
    String auth = withProxy(apiProxy, () -> assertSame(parsed, agenticService.setGovernanceSettings(settings)));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/governance/settings", "{\"autoApproval\":true}");
  }

  @Test
  void testSearchGovernanceAccessRequests() {
    GovernanceAccessRequestSearchResponse parsed = parse("{\"requests\":[" + REQUEST_JSON + "],\"total\":1}",
        GovernanceAccessRequestSearchResponse.class);
    assertEquals(1, parsed.getTotal());
    assertFullRequest(parsed.getRequests().get(0));

    GovernanceAccessRequestSearchOptions options = GovernanceAccessRequestSearchOptions.builder().limit(5).page(1)
        .searchFields(Collections.singletonList(GovernanceSearchField.builder().field("status")
            .valArr(Collections.singletonList("pending")).valStr("x").negative(true).valType("string").build()))
        .sort(Collections.singletonList(GovernanceSortField.builder().field("createdTime").desc(true).build()))
        .text("free text").build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(GovernanceAccessRequestSearchResponse.class));
    String auth = withProxy(apiProxy, () -> assertSame(parsed, agenticService.searchGovernanceAccessRequests(options)));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/governance/requests/search", "{\"limit\":5,\"page\":1,"
        + "\"searchFields\":[{\"field\":\"status\",\"valArr\":[\"pending\"],\"valStr\":\"x\",\"negative\":true,"
        + "\"valType\":\"string\"}],\"sort\":[{\"field\":\"createdTime\",\"desc\":true}],\"text\":\"free text\"}");
  }

  @Test
  void testGetGovernanceAccessRequest() {
    GovernanceAccessRequest parsed = parse(REQUEST_JSON, GovernanceAccessRequest.class);
    assertFullRequest(parsed);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(GovernanceAccessRequest.class));
    String auth = withProxy(apiProxy, () -> assertSame(parsed, agenticService.getGovernanceAccessRequest("rid")));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/governance/requests/get", "{\"id\":\"rid\"}");
  }

  @Test
  void testResolveGovernanceAccessRequest() {
    GovernanceAccessRequest parsed = parse(REQUEST_JSON, GovernanceAccessRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(GovernanceAccessRequest.class));
    GovernanceAccessRequestResolveRequest request = GovernanceAccessRequestResolveRequest.builder().id("rid")
        .action("approve").resolutionNote("note").build();
    String auth = withProxy(apiProxy,
        () -> assertSame(parsed, agenticService.resolveGovernanceAccessRequest(request)));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/governance/requests/resolve",
        "{\"id\":\"rid\",\"action\":\"approve\",\"resolutionNote\":\"note\"}");
  }

  @Test
  void testCreateGovernanceAccessRequest() {
    GovernanceAccessRequest parsed = parse(REQUEST_JSON, GovernanceAccessRequest.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(GovernanceAccessRequest.class));
    GovernanceAccessRequestCreateRequest request = GovernanceAccessRequestCreateRequest.builder()
        .requestorUserId("uid").clientName("cname").clientType("ctype")
        .grantTypes(Collections.singletonList("authCode"))
        .targets(Collections.singletonList(GovernanceAccessRequestTarget.builder().name("tname").kind("resource")
            .scopes(Collections.singletonList("s1")).allScopes(false).build()))
        .scopes(Collections.singletonList("s2")).justification("why").build();
    String auth = withProxy(apiProxy,
        () -> assertSame(parsed, agenticService.createGovernanceAccessRequest(request)));
    assertAuth(auth);
    assertPost(apiProxy, "/v1/mgmt/agentic/governance/requests/create", "{\"requestorUserId\":\"uid\","
        + "\"clientName\":\"cname\",\"clientType\":\"ctype\",\"grantTypes\":[\"authCode\"],\"targets\":[{"
        + "\"name\":\"tname\",\"kind\":\"resource\",\"scopes\":[\"s1\"],\"allScopes\":false}],\"scopes\":[\"s2\"],"
        + "\"justification\":\"why\"}");
  }

  private static void assertFullRequest(GovernanceAccessRequest r) {
    assertEquals("rid", r.getId());
    assertEquals("uid", r.getRequestorUserId());
    assertEquals("uname", r.getRequestorName());
    assertEquals("ctype", r.getClientType());
    assertEquals(Collections.singletonList("authCode"), r.getGrantTypes());
    GovernanceAccessRequestTarget target = r.getTargets().get(0);
    assertEquals("tname", target.getName());
    assertEquals("resource", target.getKind());
    assertEquals("tid", target.getTargetId());
    assertEquals(Collections.singletonList("s1"), target.getScopes());
    assertTrue(target.getAllScopes());
    assertEquals(Collections.singletonList("s2"), r.getScopes());
    assertEquals("why", r.getJustification());
    assertEquals("approved", r.getStatus());
    assertEquals("", r.getResolvedByUserId());
    assertEquals("Management API", r.getResolvedByName());
    assertEquals("note", r.getResolutionNote());
    assertEquals("cid", r.getClientId());
    assertEquals("capp", r.getClientAppId());
    assertEquals(1234567890123L, r.getCreatedTime());
    assertEquals(1234567890124L, r.getModifiedTime());
    assertEquals(3L, r.getVersion());
    assertEquals("cname", r.getClientName());
    assertTrue(r.isClientDeleted());
  }

  private static void assertInvalid(String argument, Executable executable) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown = assertThrows(ServerCommonException.class, executable);
      assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
    }
    verifyNoInteractions(apiProxy);
  }

  private static void assertAuth(String authHeader) {
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader);
  }

  @SneakyThrows
  private static void assertPost(ApiProxy apiProxy, String path, String expectedBody) {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), any(Class.class));
    assertEquals(BASE_URL + path, uri.getValue().toString());
    assertEquals(READER.readTree(expectedBody), READER.readTree(WRITER.writeValueAsString(body.getValue())));
  }

  @SneakyThrows
  private static <T> T parse(String json, Class<T> clz) {
    return READER.readValue(json, clz);
  }

  /** Runs the call with the given proxy and returns the Authorization header the service built. */
  @SuppressWarnings("unchecked")
  private static String withProxy(ApiProxy apiProxy, Runnable call) {
    AtomicReference<String> captured = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.run();
    }
    return captured.get();
  }
}
