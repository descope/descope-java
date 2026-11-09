package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticIdentity;
import com.descope.model.agentic.AgenticIdentityConsent;
import com.descope.model.agentic.AgenticIdentityRevokeRequest;
import com.descope.model.agentic.AgenticIdentityRevokeResponse;
import com.descope.model.agentic.AgenticIdentitySearchRequest;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AgenticServiceImplTest {

  private static final String MANAGEMENT_KEY = "some-management-key";

  private AgenticService agenticService;
  private ApiProxy apiProxy;
  private final AtomicReference<String> authHeader = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    agenticService = new AgenticServiceImpl(client);
    apiProxy = mock(ApiProxy.class);
  }

  @Test
  void testServiceIsBuilt() {
    assertNotNull(ManagementServiceBuilder.buildServices(TestUtils.getClient()).getAgenticService());
  }

  @Test
  void testMethodsForMissingRequestParts() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> agenticService.searchIdentities(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> agenticService.revokeIdentities(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchIdentitiesSuccess() throws Exception {
    String json = "{\"identities\":[{\"consent\":{\"id\":\"cid\",\"appId\":\"app\",\"userId\":\"uid\","
        + "\"scopes\":[\"s1\"],\"grantedBy\":\"gb\",\"createdTime\":10,\"tenantId\":\"t1\",\"ExpireTime\":20,"
        + "\"modifiedTime\":30,\"authMethod\":\"idJag\",\"blocked\":true,\"unknown\":1},"
        + "\"agentName\":\"agent\",\"resourceId\":\"res\",\"clientId\":\"client\",\"createdBy\":\"creator\"}],"
        + "\"total\":1}";
    AgenticIdentitySearchResponse response = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, AgenticIdentitySearchResponse.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AgenticIdentitySearchRequest request = AgenticIdentitySearchRequest.builder()
        .userId("uid").clientId("client").resourceId("res").consentId("cid").page(0).limit(10).build();

    final AgenticIdentitySearchResponse res = withProxy(() -> agenticService.searchIdentities(request));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(AgenticIdentitySearchResponse.class));
    assertEquals("/v1/mgmt/agentic/identities/search", uri.getValue().getPath());
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    assertEquals(new ObjectMapper().readTree("{\"userId\":\"uid\",\"clientId\":\"client\",\"resourceId\":\"res\","
        + "\"consentId\":\"cid\",\"page\":0,\"limit\":10}"), toJson(body.getValue()));

    assertEquals(1, res.getTotal());
    assertEquals(1, res.getIdentities().length);
    AgenticIdentity identity = res.getIdentities()[0];
    assertEquals("agent", identity.getAgentName());
    assertEquals("res", identity.getResourceId());
    assertEquals("client", identity.getClientId());
    assertEquals("creator", identity.getCreatedBy());
    AgenticIdentityConsent consent = identity.getConsent();
    assertEquals("cid", consent.getId());
    assertEquals("app", consent.getAppId());
    assertEquals("uid", consent.getUserId());
    assertEquals("s1", consent.getScopes()[0]);
    assertEquals("gb", consent.getGrantedBy());
    assertEquals(10, consent.getCreatedTime());
    assertEquals("t1", consent.getTenantId());
    assertEquals(20, consent.getExpireTime());
    assertEquals(30, consent.getModifiedTime());
    assertEquals("idJag", consent.getAuthMethod());
    assertEquals(true, consent.isBlocked());
  }

  @Test
  void testSearchIdentitiesOmitsUnsetFields() throws Exception {
    doReturn(new AgenticIdentitySearchResponse()).when(apiProxy).post(any(), any(), any());
    withProxy(() -> agenticService.searchIdentities(AgenticIdentitySearchRequest.builder().build()));
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(any(), body.capture(), any());
    assertEquals(new ObjectMapper().readTree("{}"), toJson(body.getValue()));
  }

  @Test
  void testRevokeIdentitiesSuccess() throws Exception {
    doReturn(new AgenticIdentityRevokeResponse(3)).when(apiProxy).post(any(), any(), any());
    AgenticIdentityRevokeRequest request = AgenticIdentityRevokeRequest.builder()
        .userId("uid").clientId("client").resourceId("res").consentId("cid").build();

    final int revoked = withProxy(() -> agenticService.revokeIdentities(request));

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(AgenticIdentityRevokeResponse.class));
    assertEquals("/v1/mgmt/agentic/identities/revoke", uri.getValue().getPath());
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    assertEquals(new ObjectMapper().readTree(
        "{\"userId\":\"uid\",\"clientId\":\"client\",\"resourceId\":\"res\",\"consentId\":\"cid\"}"),
        toJson(body.getValue()));
    assertEquals(3, revoked);
  }

  @Test
  void testRevokeIdentitiesOnlyUserId() throws Exception {
    AgenticIdentityRevokeResponse response = new AgenticIdentityRevokeResponse(0);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    int revoked = withProxy(
        () -> agenticService.revokeIdentities(AgenticIdentityRevokeRequest.builder().userId("uid").build()));
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(any(), body.capture(), any());
    assertEquals(new ObjectMapper().readTree("{\"userId\":\"uid\"}"), toJson(body.getValue()));
    assertEquals(0, revoked);
  }

  @SuppressWarnings("unchecked")
  private <T> T withProxy(Supplier<T> call) {
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      return call.get();
    }
  }

  private static JsonNode toJson(Object body) throws Exception {
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    return mapper.readTree(mapper.writeValueAsString(body));
  }
}
