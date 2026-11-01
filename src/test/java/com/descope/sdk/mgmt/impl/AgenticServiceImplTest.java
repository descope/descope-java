package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
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
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AgenticService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AgenticServiceImplTest {

  private static final String PROJECT_ID = "someProjectId";
  private static final String MANAGEMENT_KEY = "someManagementKey";
  private static final String BASE_URL = "https://api.descope.com";

  private AgenticService agenticService;

  @BeforeEach
  void setUp() {
    Client client = Client.builder().uri(BASE_URL).projectId(PROJECT_ID).managementKey(MANAGEMENT_KEY).build();
    this.agenticService = new AgenticServiceImpl(client);
  }

  @Test
  void testMethodsForMissingRequestParts() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> agenticService.searchIdentities(null));
      assertNotNull(thrown);
      assertEquals("The options argument is invalid", thrown.getMessage());
      thrown = assertThrows(ServerCommonException.class, () -> agenticService.revokeIdentities(null));
      assertNotNull(thrown);
      assertEquals("The options argument is invalid", thrown.getMessage());
      verifyNoInteractions(apiProxy);
    }
  }

  @Test
  void testSearchIdentitiesSuccess() throws Exception {
    String json = "{\"identities\":[{\"consent\":{\"id\":\"cs1\",\"appId\":\"app1\",\"userId\":\"u1\","
        + "\"scopes\":[\"s1\",\"s2\"],\"grantedBy\":\"g1\",\"createdTime\":11,\"tenantId\":\"t1\","
        + "\"ExpireTime\":12,\"modifiedTime\":13,\"authMethod\":\"idJag\"},\"agentName\":\"agent1\","
        + "\"resourceId\":\"r1\",\"clientId\":\"c1\",\"createdBy\":\"cb1\",\"unknownField\":\"x\"},"
        + "{\"consent\":null,\"agentName\":\"\",\"resourceId\":\"\",\"clientId\":\"\",\"createdBy\":\"\"}],"
        + "\"total\":2}";
    AgenticIdentitySearchResponse parsed = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, AgenticIdentitySearchResponse.class);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), any());
    AtomicReference<Supplier<String>> authHeader = new AtomicReference<>();
    AgenticIdentitySearchOptions options = AgenticIdentitySearchOptions.builder()
        .userId("u1").clientId("c1").resourceId("r1").consentId("cs1").page(1).limit(10).build();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(inv.getArgument(0));
        return apiProxy;
      });
      final AgenticIdentitySearchResponse res = agenticService.searchIdentities(options);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).post(uriCaptor.capture(), same(options), eq(AgenticIdentitySearchResponse.class));
      assertEquals(BASE_URL + "/v1/mgmt/agentic/identities/search", uriCaptor.getValue().toString());
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get().get());

      assertEquals(2, res.getTotal());
      assertEquals(2, res.getIdentities().length);
      AgenticIdentity identity = res.getIdentities()[0];
      assertEquals("agent1", identity.getAgentName());
      assertEquals("r1", identity.getResourceId());
      assertEquals("c1", identity.getClientId());
      assertEquals("cb1", identity.getCreatedBy());
      AgenticIdentityConsent consent = identity.getConsent();
      assertEquals("cs1", consent.getId());
      assertEquals("app1", consent.getAppId());
      assertEquals("u1", consent.getUserId());
      assertArrayEquals(new String[] {"s1", "s2"}, consent.getScopes());
      assertEquals("g1", consent.getGrantedBy());
      assertEquals(11, consent.getCreatedTime());
      assertEquals("t1", consent.getTenantId());
      assertEquals(12, consent.getExpireTime());
      assertEquals(13, consent.getModifiedTime());
      assertEquals("idJag", consent.getAuthMethod());
      assertNull(res.getIdentities()[1].getConsent());
    }
  }

  @Test
  void testSearchIdentitiesRequestBody() throws Exception {
    ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    String body = mapper.writeValueAsString(AgenticIdentitySearchOptions.builder()
        .userId("u1").clientId("c1").resourceId("r1").consentId("cs1").page(1).limit(10).build());
    assertEquals("{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cs1\","
        + "\"page\":1,\"limit\":10}", body);
    assertEquals("{}", mapper.writeValueAsString(AgenticIdentitySearchOptions.builder().build()));
  }

  @Test
  void testRevokeIdentitiesSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(AgenticIdentityRevokeResponse.builder().revoked(3).build()).when(apiProxy).post(any(), any(), any());
    AtomicReference<Supplier<String>> authHeader = new AtomicReference<>();
    AgenticIdentityRevokeOptions options = AgenticIdentityRevokeOptions.builder()
        .userId("u1").clientId("c1").resourceId("r1").consentId("cs1").build();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(inv.getArgument(0));
        return apiProxy;
      });
      final int revoked = agenticService.revokeIdentities(options);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).post(uriCaptor.capture(), same(options), eq(AgenticIdentityRevokeResponse.class));
      assertEquals(BASE_URL + "/v1/mgmt/agentic/identities/revoke", uriCaptor.getValue().toString());
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get().get());
      assertEquals(3, revoked);
    }

    String body = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .writeValueAsString(options);
    assertEquals("{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cs1\"}", body);
    AgenticIdentityRevokeResponse parsed = new ObjectMapper()
        .readValue("{\"revoked\":5}", AgenticIdentityRevokeResponse.class);
    assertEquals(5, parsed.getRevoked());
  }
}
