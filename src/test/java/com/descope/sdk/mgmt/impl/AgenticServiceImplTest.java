package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_REVOKE_IDENTITIES;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_SEARCH_IDENTITIES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import com.descope.utils.UriUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AgenticServiceImplTest {

  private static final String BASE_URI = "https://api.descope.com";
  private static final String PROJECT_ID = "P0000000000000000000000000000";
  private static final String MANAGEMENT_KEY = "test-key";
  private static final ObjectMapper WRITER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final ObjectMapper READER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private AgenticService agenticService;

  @BeforeEach
  void setUp() {
    Client client = Client.builder().uri(BASE_URI).projectId(PROJECT_ID).managementKey(MANAGEMENT_KEY).build();
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
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSearchIdentitiesSuccess() throws Exception {
    String responseJson = "{\"identities\":[{\"consent\":{\"id\":\"cons1\",\"appId\":\"app1\",\"userId\":\"user1\","
        + "\"scopes\":[\"s1\",\"s2\"],\"grantedBy\":\"granter\",\"createdTime\":11,\"tenantId\":\"t1\","
        + "\"ExpireTime\":22,\"modifiedTime\":33,\"authMethod\":\"idJag\",\"newField\":1},"
        + "\"agentName\":\"agent\",\"resourceId\":\"res1\",\"clientId\":\"client1\",\"createdBy\":\"creator\"},"
        + "{\"consent\":null,\"agentName\":\"\",\"resourceId\":\"\",\"clientId\":\"\",\"createdBy\":\"\"}],"
        + "\"total\":2,\"unknown\":\"x\"}";
    AgenticIdentitySearchResponse parsed = READER.readValue(responseJson, AgenticIdentitySearchResponse.class);
    assertEquals(2, parsed.getTotal());
    assertEquals(2, parsed.getIdentities().length);
    AgenticIdentity identity = parsed.getIdentities()[0];
    assertEquals("agent", identity.getAgentName());
    assertEquals("res1", identity.getResourceId());
    assertEquals("client1", identity.getClientId());
    assertEquals("creator", identity.getCreatedBy());
    AgenticIdentityConsent consent = identity.getConsent();
    assertEquals("cons1", consent.getId());
    assertEquals("app1", consent.getAppId());
    assertEquals("user1", consent.getUserId());
    assertArrayEquals(new String[] {"s1", "s2"}, consent.getScopes());
    assertEquals("granter", consent.getGrantedBy());
    assertEquals(11, consent.getCreatedTime());
    assertEquals("t1", consent.getTenantId());
    assertEquals(22, consent.getExpireTime());
    assertEquals(33, consent.getModifiedTime());
    assertEquals("idJag", consent.getAuthMethod());
    assertNull(parsed.getIdentities()[1].getConsent());

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(AgenticIdentitySearchResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AgenticIdentitySearchOptions options = AgenticIdentitySearchOptions.builder()
          .userId("user1").clientId("client1").resourceId("res1").consentId("cons1").page(1).limit(10).build();
      AgenticIdentitySearchResponse res = agenticService.searchIdentities(options);
      assertEquals(parsed, res);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AgenticIdentitySearchResponse.class));
      assertEquals(UriUtils.getUri(BASE_URI, MANAGEMENT_AGENTIC_SEARCH_IDENTITIES), uriCaptor.getValue());
      assertEquals("/v1/mgmt/agentic/identities/search", uriCaptor.getValue().getPath());
      JsonNode body = WRITER.valueToTree(bodyCaptor.getValue());
      assertEquals(READER.readTree("{\"userId\":\"user1\",\"clientId\":\"client1\",\"resourceId\":\"res1\","
          + "\"consentId\":\"cons1\",\"page\":1,\"limit\":10}"), body);

      ArgumentCaptor<Supplier<String>> authCaptor = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(authCaptor.capture(), any()));
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authCaptor.getValue().get());
    }
  }

  @Test
  void testSearchIdentitiesOmitsUnsetFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new AgenticIdentitySearchResponse()).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      agenticService.searchIdentities(AgenticIdentitySearchOptions.builder().build());
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), any());
      assertEquals(0, WRITER.valueToTree(bodyCaptor.getValue()).size());
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testRevokeIdentitiesSuccess() throws Exception {
    AgenticIdentityRevokeResponse parsed =
        READER.readValue("{\"revoked\":3,\"unknown\":true}", AgenticIdentityRevokeResponse.class);
    assertEquals(3, parsed.getRevoked());

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), eq(AgenticIdentityRevokeResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AgenticIdentityRevokeOptions options = AgenticIdentityRevokeOptions.builder()
          .userId("user1").clientId("client1").resourceId("res1").consentId("cons1").build();
      assertEquals(3, agenticService.revokeIdentities(options));

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AgenticIdentityRevokeResponse.class));
      assertEquals(UriUtils.getUri(BASE_URI, MANAGEMENT_AGENTIC_REVOKE_IDENTITIES), uriCaptor.getValue());
      assertEquals("/v1/mgmt/agentic/identities/revoke", uriCaptor.getValue().getPath());
      JsonNode body = WRITER.valueToTree(bodyCaptor.getValue());
      assertEquals(READER.readTree("{\"userId\":\"user1\",\"clientId\":\"client1\",\"resourceId\":\"res1\","
          + "\"consentId\":\"cons1\"}"), body);

      ArgumentCaptor<Supplier<String>> authCaptor = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(authCaptor.capture(), any()));
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authCaptor.getValue().get());
    }
  }
}
