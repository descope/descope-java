package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.scopeclaimmapping.ClaimTokenTargets;
import com.descope.model.scopeclaimmapping.ScopeClaimMappingEntry;
import com.descope.model.scopeclaimmapping.ScopeClaimMappingResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.ScopeClaimMappingService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ScopeClaimMappingServiceImplTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String RESPONSE_JSON = "{\"mappings\":[{\"scope\":\"email\","
      + "\"claims\":{\"email\":\"{{user.email}}\"},\"description\":\"Email claims\","
      + "\"claimTargets\":{\"email\":{\"tokens\":[\"accessToken\",\"idToken\"]}},"
      + "\"addToNewApplications\":true,\"unknownField\":1},{\"scope\":\"phone\","
      + "\"claims\":{\"phone\":\"{{user.phone}}\"}}]}";

  private ScopeClaimMappingService service;
  private ApiProxy apiProxy;
  private AtomicReference<String> authHeader;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    service = ManagementServiceBuilder.buildServices(client).getScopeClaimMappingService();
    apiProxy = mock(ApiProxy.class);
    authHeader = new AtomicReference<>();
  }

  @Test
  void testGetScopeClaimMapping() throws Exception {
    ScopeClaimMappingResponse response = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(RESPONSE_JSON, ScopeClaimMappingResponse.class);
    doReturn(response).when(apiProxy).post(any(), any(), eq(ScopeClaimMappingResponse.class));
    List<ScopeClaimMappingEntry> mappings;
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      captureAuthHeader(mocked);
      mappings = service.getScopeClaimMapping();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(ScopeClaimMappingResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/scopeClaimMapping/get");
    assertThat(body.getValue()).isEqualTo(Collections.emptyMap());
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);

    assertThat(mappings).hasSize(2);
    ScopeClaimMappingEntry first = mappings.get(0);
    assertThat(first.getScope()).isEqualTo("email");
    assertThat(first.getClaims()).containsEntry("email", "{{user.email}}");
    assertThat(first.getDescription()).isEqualTo("Email claims");
    assertThat(first.getClaimTargets().get("email").getTokens())
        .containsExactly(ClaimTokenTargets.ACCESS_TOKEN, ClaimTokenTargets.ID_TOKEN);
    assertThat(first.getAddToNewApplications()).isTrue();
    ScopeClaimMappingEntry second = mappings.get(1);
    assertThat(second.getScope()).isEqualTo("phone");
    assertThat(second.getDescription()).isNull();
    assertThat(second.getClaimTargets()).isNull();
    assertThat(second.getAddToNewApplications()).isNull();
  }

  @Test
  void testGetScopeClaimMappingEmpty() {
    doReturn(new ScopeClaimMappingResponse()).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      captureAuthHeader(mocked);
      assertThat(service.getScopeClaimMapping()).isEmpty();
    }
  }

  @Test
  void testSetScopeClaimMapping() {
    Map<String, String> claims = new HashMap<>();
    claims.put("email", "{{user.email}}");
    Map<String, ClaimTokenTargets> targets = new HashMap<>();
    targets.put("email", ClaimTokenTargets.builder()
        .tokens(Arrays.asList(ClaimTokenTargets.ACCESS_TOKEN, ClaimTokenTargets.USER_INFO)).build());
    List<ScopeClaimMappingEntry> mappings = Collections.singletonList(ScopeClaimMappingEntry.builder()
        .scope("email")
        .claims(claims)
        .description("Email claims")
        .claimTargets(targets)
        .addToNewApplications(true)
        .build());
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      captureAuthHeader(mocked);
      service.setScopeClaimMapping(mappings);
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(Void.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/scopeClaimMapping/set");
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
    String json = toJson(body.getValue());
    assertThat(json).isEqualTo("{\"mappings\":[{\"scope\":\"email\",\"claims\":{\"email\":\"{{user.email}}\"},"
        + "\"description\":\"Email claims\",\"claimTargets\":{\"email\":{\"tokens\":[\"accessToken\",\"userInfo\"]}},"
        + "\"addToNewApplications\":true}]}");
  }

  @Test
  void testSetScopeClaimMappingOmitsUnsetFields() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      captureAuthHeader(mocked);
      service.setScopeClaimMapping(Collections.singletonList(ScopeClaimMappingEntry.builder().scope("email").build()));
      service.setScopeClaimMapping(null);
    }
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(2)).post(any(), body.capture(), eq(Void.class));
    assertThat(toJson(body.getAllValues().get(0))).isEqualTo("{\"mappings\":[{\"scope\":\"email\"}]}");
    assertThat(toJson(body.getAllValues().get(1))).isEqualTo("{}");
  }

  @Test
  void testDeleteScopeClaimMapping() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      captureAuthHeader(mocked);
      service.deleteScopeClaimMapping();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(Void.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/scopeClaimMapping/delete");
    assertThat(body.getValue()).isEqualTo(Collections.emptyMap());
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
  }

  @SuppressWarnings("unchecked")
  private void captureAuthHeader(MockedStatic<ApiProxyBuilder> mocked) {
    mocked.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
  }

  // Serializes like the API proxy does
  private static String toJson(Object body) {
    try {
      return new ObjectMapper()
          .setSerializationInclusion(Include.NON_NULL)
          .writeValueAsString(body);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
