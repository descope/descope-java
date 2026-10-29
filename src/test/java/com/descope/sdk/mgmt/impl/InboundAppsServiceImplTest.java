package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.inbound.AgenticIdentity;
import com.descope.model.inbound.AgenticIdentityConsent;
import com.descope.model.inbound.AgenticIdentityRevokeOptions;
import com.descope.model.inbound.AgenticIdentityRevokeResponse;
import com.descope.model.inbound.AgenticIdentitySearchOptions;
import com.descope.model.inbound.AgenticIdentitySearchResponse;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppConsent;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppScope;
import com.descope.model.inbound.InboundAppSecret;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.LoadAllApplicationsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.InboundAppsService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class InboundAppsServiceImplTest {

  private final InboundApp mockInboundApp = InboundApp.builder().id("someId").build();
  private final InboundAppSecret mockSecret = InboundAppSecret.builder().secret("secret").build();
  private final InboundAppConsentSearchResponse mockSearchConsentResponse = InboundAppConsentSearchResponse.builder()
      .total(1).consents(new InboundAppConsent[] { InboundAppConsent.builder().build() }).build();
  private InboundAppsService inboundAppsService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.inboundAppsService = ManagementServiceBuilder.buildServices(client).getInboundAppsService();
  }

  @Test
  void testMethodsForMissingRequestParts() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createApplication(InboundAppRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The request.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.updateApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.updateApplication(InboundAppRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The request.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.updateApplication(InboundAppRequest.builder().id("a").build()));
    assertNotNull(thrown);
    assertEquals("The request.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.patchApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.patchApplication(InboundAppRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The request.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.deleteApplication(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.loadApplication(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.loadApplicationByClientId(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.getApplicationSecret(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.rotateApplicationSecret(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.deleteConsents(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.deleteTenantConsents(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.searchConsents(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.searchAgenticIdentities(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.revokeAgenticIdentities(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(InboundAppCreateResponse.builder().id(mockInboundApp.getId()).build()).when(apiProxy).post(any(), any(),
        any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundAppCreateResponse app = inboundAppsService
          .createApplication(InboundAppRequest.builder().name("n1").id("i1").build());
      assertNotNull(app);
      assertEquals("someId", app.getId());
    }
  }

  @Test
  void testUpdateApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.updateApplication(InboundAppRequest.builder().id("a").name("n1").build());
    }
  }

  @Test
  void testPatchApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.patchApplication(InboundAppRequest.builder().id("a").build());
    }
  }

  @Test
  void testDeleteApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.deleteApplication("a");
    }
  }

  @Test
  void testLoadApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockInboundApp).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundApp app = inboundAppsService.loadApplication("a");
      assertNotNull(app);
      assertEquals("someId", app.getId());
    }
  }

  @Test
  void testLoadApplicationByClientSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockInboundApp).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundApp app = inboundAppsService.loadApplicationByClientId("a");
      assertNotNull(app);
      assertEquals("someId", app.getId());
    }
  }

  @Test
  void testGetApplicationSecretSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSecret).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String secret = inboundAppsService.getApplicationSecret("a");
      assertEquals("secret", secret);
    }
  }

  @Test
  void testRotateApplicationSecretSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSecret).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String secret = inboundAppsService.rotateApplicationSecret("a");
      assertEquals("secret", secret);
    }
  }

  @Test
  void testLoadAllApplicationsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(LoadAllApplicationsResponse.builder().apps(new InboundApp[] { mockInboundApp }).build()).when(apiProxy)
        .get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundApp[] apps = inboundAppsService.loadAllApplications();
      assertEquals(1, apps.length);
    }
  }

  @Test
  void testDeleteConsentsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.deleteConsents(InboundAppConsentDeleteOptions.builder().build());
    }
  }

  @Test
  void testDeleteTenantConsentsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.deleteTenantConsents(InboundAppTenantConsentDeleteOptions.builder().build());
    }
  }

  @Test
  void testSearchConsentsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSearchConsentResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundAppConsentSearchResponse res = inboundAppsService
          .searchConsents(InboundAppConsentSearchOptions.builder().build());
      assertEquals(1, res.getTotal());
      assertEquals(1, res.getConsents().length);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSearchAgenticIdentitiesSuccess() throws Exception {
    InboundAppsService service = ManagementServiceBuilder.buildServices(agenticClient()).getInboundAppsService();
    AgenticIdentitySearchResponse parsed = mapper().readValue(AGENTIC_SEARCH_RESPONSE,
        AgenticIdentitySearchResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      AgenticIdentitySearchOptions options = AgenticIdentitySearchOptions.builder().userId("u1").clientId("c1")
          .resourceId("r1").consentId("cs1").page(1).limit(10).build();
      AgenticIdentitySearchResponse res = service.searchAgenticIdentities(options);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AgenticIdentitySearchResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/mgmt/agentic/identities/search");
      assertThat(authHeader.get()).isEqualTo("Bearer " + AGENTIC_PROJECT_ID + ":" + AGENTIC_MANAGEMENT_KEY);
      JsonNode body = mapper().valueToTree(bodyCaptor.getValue());
      assertThat(body).isEqualTo(mapper().readTree(
          "{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cs1\","
              + "\"page\":1,\"limit\":10}"));
      assertThat(mapper().valueToTree(AgenticIdentitySearchOptions.builder().build()).size()).isZero();

      assertThat(res.getTotal()).isEqualTo(1);
      assertThat(res.getIdentities()).hasSize(1);
      AgenticIdentity identity = res.getIdentities()[0];
      assertThat(identity.getAgentName()).isEqualTo("agent");
      assertThat(identity.getResourceId()).isEqualTo("r1");
      assertThat(identity.getClientId()).isEqualTo("c1");
      assertThat(identity.getCreatedBy()).isEqualTo("creator");
      AgenticIdentityConsent consent = identity.getConsent();
      assertThat(consent.getId()).isEqualTo("cs1");
      assertThat(consent.getAppId()).isEqualTo("app1");
      assertThat(consent.getUserId()).isEqualTo("u1");
      assertThat(consent.getScopes()).containsExactly("s1", "s2");
      assertThat(consent.getGrantedBy()).isEqualTo("granter");
      assertThat(consent.getCreatedTime()).isEqualTo(100);
      assertThat(consent.getTenantId()).isEqualTo("t1");
      assertThat(consent.getExpireTime()).isEqualTo(200);
      assertThat(consent.getModifiedTime()).isEqualTo(300);
    }
  }

  @Test
  void testSearchAgenticIdentitiesNullConsent() throws Exception {
    AgenticIdentitySearchResponse res = mapper().readValue(
        "{\"identities\":[{\"consent\":null,\"agentName\":\"\"}],\"total\":0}",
        AgenticIdentitySearchResponse.class);
    assertThat(res.getIdentities()).hasSize(1);
    assertThat(res.getIdentities()[0].getConsent()).isNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testRevokeAgenticIdentitiesSuccess() throws Exception {
    InboundAppsService service = ManagementServiceBuilder.buildServices(agenticClient()).getInboundAppsService();
    AgenticIdentityRevokeResponse parsed = mapper().readValue("{\"revoked\":3}",
        AgenticIdentityRevokeResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      int revoked = service.revokeAgenticIdentities(AgenticIdentityRevokeOptions.builder().userId("u1")
          .clientId("c1").resourceId("r1").consentId("cs1").build());
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AgenticIdentityRevokeResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/mgmt/agentic/identities/revoke");
      assertThat(authHeader.get()).isEqualTo("Bearer " + AGENTIC_PROJECT_ID + ":" + AGENTIC_MANAGEMENT_KEY);
      JsonNode body = mapper().valueToTree(bodyCaptor.getValue());
      assertThat(body).isEqualTo(mapper().readTree(
          "{\"userId\":\"u1\",\"clientId\":\"c1\",\"resourceId\":\"r1\",\"consentId\":\"cs1\"}"));
      assertThat(revoked).isEqualTo(3);
    }
  }

  private static final String AGENTIC_PROJECT_ID = "P123456789012345678901234567";
  private static final String AGENTIC_MANAGEMENT_KEY = "some-management-key";
  private static final String AGENTIC_SEARCH_RESPONSE = "{\"identities\":[{\"consent\":{\"id\":\"cs1\","
      + "\"appId\":\"app1\",\"userId\":\"u1\",\"scopes\":[\"s1\",\"s2\"],\"grantedBy\":\"granter\","
      + "\"createdTime\":100,\"tenantId\":\"t1\",\"ExpireTime\":200,\"modifiedTime\":300},"
      + "\"agentName\":\"agent\",\"resourceId\":\"r1\",\"clientId\":\"c1\",\"createdBy\":\"creator\","
      + "\"unknownField\":\"x\"}],\"total\":1}";

  private static Client agenticClient() {
    return Client.builder()
        .uri("https://api.descope.com")
        .projectId(AGENTIC_PROJECT_ID)
        .managementKey(AGENTIC_MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
  }

  // Same settings the SDK proxy uses: omit nulls when sending, ignore unknown fields when parsing
  private static ObjectMapper mapper() {
    return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalInboundApps() {
    // create app
    String name = TestUtils.getRandomName("iban-");
    InboundAppScope[] scopes = new InboundAppScope[] {
        InboundAppScope.builder().name("s1").values(new String[] { "v1", "v2" }).build() };
    InboundAppCreateResponse initialApp = inboundAppsService.createApplication(
        InboundAppRequest.builder().name(name).id(TestUtils.getRandomName("ibaid-")).permissionsScopes(scopes).build());
    try {
      // get secret
      String secret = inboundAppsService.getApplicationSecret(initialApp.getId());
      assertEquals(initialApp.getSecret(), secret);
      // rotate secret
      secret = inboundAppsService.rotateApplicationSecret(initialApp.getId());
      assertNotEquals(initialApp.getSecret(), secret);
      // get secret
      String newSecret = inboundAppsService.getApplicationSecret(initialApp.getId());
      assertEquals(secret, newSecret);
      // load all apps
      InboundApp[] allApps = inboundAppsService.loadAllApplications();
      InboundApp[] apps = Arrays.stream(allApps).filter(a -> a.getId().equals(initialApp.getId()))
          .toArray(InboundApp[]::new);
      assertEquals(1, apps.length);
      assertEquals(initialApp.getId(), apps[0].getId());
      // update app
      inboundAppsService.updateApplication(
          InboundAppRequest.builder().id(initialApp.getId()).name(name + "2").permissionsScopes(scopes).loginPageUrl("https://example.com").build());
      // load app validate
      InboundApp lapp = inboundAppsService.loadApplication(initialApp.getId());
      assertEquals(initialApp.getId(), lapp.getId());
      assertEquals(name + "2", lapp.getName());
      // patch app
      inboundAppsService.patchApplication(InboundAppRequest.builder().id(initialApp.getId()).name(name + "3").build());
      // load app validate
      lapp = inboundAppsService.loadApplication(initialApp.getId());
      assertEquals(initialApp.getId(), lapp.getId());
      assertEquals(name + "3", lapp.getName());
    } finally {
      // delete app
      inboundAppsService.deleteApplication(initialApp.getId());
      // load all apps
      InboundApp[] allApps = inboundAppsService.loadAllApplications();
      InboundApp[] apps = Arrays.stream(allApps).filter(a -> a.getId().equals(initialApp.getId()))
          .toArray(InboundApp[]::new);
      assertEquals(0, apps.length);
    }
  }
}
