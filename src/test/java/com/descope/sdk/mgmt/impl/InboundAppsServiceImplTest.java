package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppAddSecretResponse;
import com.descope.model.inbound.InboundAppCibaSettings;
import com.descope.model.inbound.InboundAppClientAssertion;
import com.descope.model.inbound.InboundAppConsent;
import com.descope.model.inbound.InboundAppConsentCreateRequest;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppIdJagTargetClient;
import com.descope.model.inbound.InboundAppIssuerSettings;
import com.descope.model.inbound.InboundAppJarSettings;
import com.descope.model.inbound.InboundAppJwtBearerSettings;
import com.descope.model.inbound.InboundAppLoadAllOptions;
import com.descope.model.inbound.InboundAppMtlsClientAuth;
import com.descope.model.inbound.InboundAppMtlsClientAuthMethods;
import com.descope.model.inbound.InboundAppMtlsCredential;
import com.descope.model.inbound.InboundAppRarSettings;
import com.descope.model.inbound.InboundAppRarTypeSchema;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppScope;
import com.descope.model.inbound.InboundAppScopeClaimMapping;
import com.descope.model.inbound.InboundAppSecret;
import com.descope.model.inbound.InboundAppSecretMeta;
import com.descope.model.inbound.InboundAppSessionSettings;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.InboundAppWorkloadClientAssertion;
import com.descope.model.inbound.LoadAllApplicationsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.InboundAppsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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
  private static final String KEYED_PROJECT_ID = "P123456789012345678901234567";
  private static final String KEYED_MANAGEMENT_KEY = "some-management-key";
  private InboundAppsService inboundAppsService;
  private InboundAppsService keyedService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.inboundAppsService = ManagementServiceBuilder.buildServices(client).getInboundAppsService();
    Client keyedClient = Client.builder().uri("https://api.descope.com").projectId(KEYED_PROJECT_ID)
        .managementKey(KEYED_MANAGEMENT_KEY).sdkInfo(SdkInfo.builder().name("java").build()).build();
    this.keyedService = ManagementServiceBuilder.buildServices(keyedClient).getInboundAppsService();
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
  void testCreateApplicationNewFields() throws Exception {
    Map<String, InboundAppIssuerSettings> issuers = new HashMap<>();
    issuers.put("https://issuer.com", InboundAppIssuerSettings.builder().jwksUri("https://issuer.com/jwks")
        .signAlgorithm(InboundAppIssuerSettings.SIGN_ALGORITHM_RS256).jitDisabled(true).build());
    Map<String, InboundAppIdJagTargetClient> targets = new HashMap<>();
    targets.put("c1", InboundAppIdJagTargetClient.builder().targetClientId("c2").build());
    InboundAppRequest request = InboundAppRequest.builder()
        .name("n1")
        .consentFlowId("flow1")
        .logoUrl("https://logo.com")
        .jwtBearerSettings(InboundAppJwtBearerSettings.builder().issuers(issuers).build())
        .sessionSettings(InboundAppSessionSettings.builder().enabled(true).refreshTokenExpiration(3)
            .refreshTokenExpirationUnit(InboundAppSessionSettings.UNIT_DAYS).build())
        .nonConfidentialClient(false)
        .clientId("cid")
        .clientSecret("csecret")
        .defaultAudience(InboundAppRequest.DEFAULT_AUDIENCE_CLIENT_ID)
        .customAttributes(Collections.singletonMap("k", "v"))
        .clientType(InboundAppRequest.CLIENT_TYPE_PUBLIC)
        .forcePkce(true)
        .scopeClaimMapping(new InboundAppScopeClaimMapping[] {InboundAppScopeClaimMapping.builder().scope("s1")
            .claims(Collections.singletonMap("c", "{{user.email}}")).build()})
        .mtlsClientAuthMethods(InboundAppMtlsClientAuthMethods.builder().tlsClientAuth(InboundAppMtlsClientAuth
            .builder().credentials(new InboundAppMtlsCredential[] {InboundAppMtlsCredential.builder().name("m1")
                .build()}).build()).build())
        .idJagTargetClients(targets)
        .clientAuthMethod(InboundAppRequest.CLIENT_AUTH_METHOD_PRIVATE_KEY_JWT)
        .clientAssertions(new InboundAppClientAssertion[] {InboundAppClientAssertion.builder()
            .keySource(InboundAppClientAssertion.KEY_SOURCE_JWKS).jwksUri("https://jwks.com").build()})
        .tags(new String[] {"t1"})
        .noScopeBehavior(InboundAppRequest.NO_SCOPE_BEHAVIOR_GRANT_DEFAULT_SCOPES)
        .defaultScopes(new String[] {"s1"})
        .agentic(true)
        .createdBy("u1")
        .build();
    ClassicHttpRequest sent = send("{\"id\":\"i1\",\"cleartext\":\"secret\"}",
        () -> {
          InboundAppCreateResponse res = keyedService.createApplication(request);
          assertEquals("i1", res.getId());
          assertEquals("secret", res.getSecret());
        });
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/app/create");
    Map<?, ?> body = body(sent);
    assertEquals("n1", body.get("name"));
    assertEquals("flow1", body.get("consentFlowId"));
    assertEquals("https://logo.com", body.get("logoUrl"));
    assertEquals("RS256", path(body, "jwtBearerSettings", "issuers", "https://issuer.com", "signAlgorithm"));
    assertEquals(true, path(body, "jwtBearerSettings", "issuers", "https://issuer.com", "jitDisabled"));
    assertEquals("days", path(body, "sessionSettings", "refreshTokenExpirationUnit"));
    assertEquals(3, path(body, "sessionSettings", "refreshTokenExpiration"));
    assertEquals(false, body.get("nonConfidentialClient"));
    assertEquals("cid", body.get("clientId"));
    assertEquals("csecret", body.get("clientSecret"));
    assertEquals("clientId", body.get("defaultAudience"));
    assertEquals("v", path(body, "customAttributes", "k"));
    assertEquals("public", body.get("clientType"));
    assertEquals(true, body.get("forcePkce"));
    assertEquals("s1", ((Map<?, ?>) ((List<?>) body.get("scopeClaimMapping")).get(0)).get("scope"));
    assertEquals("m1", ((Map<?, ?>) ((List<?>) path(body, "mtlsClientAuthMethods", "tlsClientAuth", "credentials"))
        .get(0)).get("name"));
    assertEquals("c2", path(body, "idJagTargetClients", "c1", "targetClientId"));
    assertEquals("privateKeyJwt", body.get("clientAuthMethod"));
    assertEquals("jwks", ((Map<?, ?>) ((List<?>) body.get("clientAssertions")).get(0)).get("keySource"));
    assertEquals(Collections.singletonList("t1"), body.get("tags"));
    assertEquals("grantDefaultScopes", body.get("noScopeBehavior"));
    assertEquals(Collections.singletonList("s1"), body.get("defaultScopes"));
    assertEquals(true, body.get("agentic"));
    assertEquals("u1", body.get("createdBy"));
    assertFalse(body.containsKey("forceDpop"));
    assertFalse(body.containsKey("cibaEnabled"));
    assertFalse(body.containsKey("rarSettings"));
  }

  @Test
  void testUpdateApplicationNewFields() throws Exception {
    ClassicHttpRequest sent = send("{}", () -> keyedService.updateApplication(InboundAppRequest.builder().id("a")
        .name("n1").agentic(false).createdBy("").forceDpop(false).cibaEnabled(true)
        .cibaSettings(InboundAppCibaSettings.builder().enabled(true)
            .notificationMethod(InboundAppCibaSettings.NOTIFICATION_METHOD_PUSH).build())
        .jarSettings(InboundAppJarSettings.builder().enforce(true).maxLifetimeSeconds(60).build())
        .tags(new String[0]).build()));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/app/update");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("id"));
    assertEquals(false, body.get("agentic"));
    assertEquals("", body.get("createdBy"));
    assertEquals(false, body.get("forceDpop"));
    assertEquals(true, body.get("cibaEnabled"));
    assertEquals("push", path(body, "cibaSettings", "notificationMethod"));
    assertEquals(60, path(body, "jarSettings", "maxLifetimeSeconds"));
    assertEquals(Collections.emptyList(), body.get("tags"));
    assertFalse(body.containsKey("forcePkce"));
    assertFalse(body.containsKey("clientType"));
  }

  @Test
  void testPatchApplicationNewFields() throws Exception {
    ClassicHttpRequest sent = send("{}", () -> keyedService.patchApplication(InboundAppRequest.builder().id("a")
        .forcePar(true).forceMtls(false).authorizationCodeDisabled(true).refreshTokenDisabled(false)
        .rarSettings(InboundAppRarSettings.builder().schemas(new InboundAppRarTypeSchema[] {
            InboundAppRarTypeSchema.builder().type("payment").schema(Collections.singletonMap("type", "object"))
                .build()}).build())
        .workloadClientAssertions(new InboundAppWorkloadClientAssertion[] {InboundAppWorkloadClientAssertion
            .builder().federationType(InboundAppWorkloadClientAssertion.FEDERATION_TYPE_SPIFFE)
            .trustDomain("example.org").allowedSubjects(new String[] {"spiffe://example.org/a"}).build()})
        .build()));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/app/patch");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("id"));
    assertEquals(true, body.get("forcePar"));
    assertEquals(false, body.get("forceMtls"));
    assertEquals(true, body.get("authorizationCodeDisabled"));
    assertEquals(false, body.get("refreshTokenDisabled"));
    assertEquals("payment", ((Map<?, ?>) ((List<?>) path(body, "rarSettings", "schemas")).get(0)).get("type"));
    assertEquals("spiffe", ((Map<?, ?>) ((List<?>) body.get("workloadClientAssertions")).get(0))
        .get("federationType"));
    assertFalse(body.containsKey("name"));
    assertFalse(body.containsKey("agentic"));
    assertFalse(body.containsKey("createdBy"));
  }

  @Test
  void testUpdateAndPatchRejectCreateOnlyFields() {
    InboundAppRequest[] requests = {
        InboundAppRequest.builder().id("a").name("n").consentFlowId("f").build(),
        InboundAppRequest.builder().id("a").name("n").nonConfidentialClient(true).build(),
        InboundAppRequest.builder().id("a").name("n").clientId("c").build(),
        InboundAppRequest.builder().id("a").name("n").clientSecret("s").build(),
    };
    String[] fields = {"consentFlowId", "nonConfidentialClient", "clientId", "clientSecret"};
    for (int i = 0; i < requests.length; i++) {
      InboundAppRequest request = requests[i];
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> inboundAppsService.updateApplication(request));
      assertEquals("The request." + fields[i] + " argument is invalid", thrown.getMessage());
      thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.patchApplication(request));
      assertEquals("The request." + fields[i] + " argument is invalid", thrown.getMessage());
    }
  }

  @Test
  void testDeleteApplications() throws Exception {
    ClassicHttpRequest sent = send("{}", () -> keyedService.deleteApplications(Arrays.asList("a1", "a2")));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/app/delete/batch");
    assertEquals(Arrays.asList("a1", "a2"), body(sent).get("ids"));

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.deleteApplications(null));
    assertEquals("The ids argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.deleteApplications(Collections.emptyList()));
    assertEquals("The ids argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadApplicationNewFields() throws Exception {
    String response = "{\"id\":\"a\",\"name\":\"n\",\"clientId\":\"c\",\"dynamic\":true,\"status\":\"active\","
        + "\"logoUrl\":\"https://logo.com\",\"jwtBearerSettings\":{\"issuers\":{\"iss\":{\"jwksUri\":\"j\","
        + "\"attributeMapping\":{\"email\":\"mail\"}}},\"jwtBearerGrantTypeScopeToUse\":\"sc\"},"
        + "\"sessionSettings\":{\"enabled\":true,\"inactivityTime\":5,\"inactivityTimeUnit\":\"hours\"},"
        + "\"nonConfidentialClient\":true,\"audienceWhitelist\":[\"aud\"],\"forceAddAllAuthorizationInfo\":true,"
        + "\"connectionsScopes\":[{\"name\":\"cs\"}],\"defaultAudience\":\"projectId\",\"skipConsentScreen\":true,"
        + "\"useResources\":true,\"customAttributes\":{\"k\":\"v\"},\"cibaSettings\":{\"pushEmailFallback\":true},"
        + "\"jarSettings\":{\"fapi\":true},\"allowedTenants\":[\"t1\"],\"clientType\":\"confidential\","
        + "\"forcePkce\":true,\"allowPartialScopes\":true,\"scopeClaimMapping\":[{\"scope\":\"s\",\"claimTargets\":"
        + "{\"c\":{\"tokens\":[\"idToken\"]}}}],\"mtlsClientAuthMethods\":{\"selfSignedTlsClientAuth\":"
        + "{\"credentials\":[{\"name\":\"m\",\"createdAt\":7}]}},\"createdTime\":1700000000,\"forceDpop\":true,"
        + "\"rarSettings\":{\"schemas\":[{\"type\":\"r\"}]},\"idJagTargetClients\":{\"x\":{\"targetClientId\":\"y\"}},"
        + "\"clientAuthMethod\":\"mtls\",\"clientAssertions\":[{\"keySource\":\"publicKey\"}],"
        + "\"workloadClientAssertions\":[{\"federationType\":\"oidc\",\"issuer\":\"i\"}],\"forcePar\":true,"
        + "\"forceMtls\":true,\"authorizationCodeDisabled\":true,\"clientCredentialsDisabled\":true,"
        + "\"refreshTokenDisabled\":true,\"jwtBearerDisabled\":true,\"deviceCodeDisabled\":true,"
        + "\"tokenExchangeDisabled\":true,\"cibaEnabled\":true,\"tags\":[\"tag\"],\"noScopeBehavior\":\"grantAllScopes\","
        + "\"defaultScopes\":[\"ds\"],\"agentic\":true,\"createdBy\":\"Owner\",\"unknownField\":1}";
    InboundApp[] app = new InboundApp[1];
    ClassicHttpRequest sent = send(response, () -> app[0] = keyedService.loadApplication("a"));
    assertRequest(sent, "GET", "/v1/mgmt/thirdparty/app/load?id=a");
    InboundApp a = app[0];
    assertEquals("c", a.getClientId());
    assertTrue(a.getDynamic());
    assertEquals("active", a.getStatus());
    assertEquals("https://logo.com", a.getLogoUrl());
    assertEquals("mail", a.getJwtBearerSettings().getIssuers().get("iss").getAttributeMapping().getEmail());
    assertEquals("sc", a.getJwtBearerSettings().getJwtBearerGrantTypeScopeToUse());
    assertEquals(5, a.getSessionSettings().getInactivityTime());
    assertTrue(a.getNonConfidentialClient());
    assertEquals("aud", a.getAudienceWhitelist()[0]);
    assertTrue(a.getForceAddAllAuthorizationInfo());
    assertEquals("cs", a.getConnectionsScopes()[0].getName());
    assertEquals("projectId", a.getDefaultAudience());
    assertTrue(a.getSkipConsentScreen());
    assertTrue(a.getUseResources());
    assertEquals("v", a.getCustomAttributes().get("k"));
    assertTrue(a.getCibaSettings().getPushEmailFallback());
    assertTrue(a.getJarSettings().getFapi());
    assertEquals("t1", a.getAllowedTenants()[0]);
    assertEquals("confidential", a.getClientType());
    assertTrue(a.getForcePkce());
    assertTrue(a.getAllowPartialScopes());
    assertEquals("idToken", a.getScopeClaimMapping()[0].getClaimTargets().get("c").getTokens()[0]);
    assertEquals(7, a.getMtlsClientAuthMethods().getSelfSignedTlsClientAuth().getCredentials()[0].getCreatedAt());
    assertEquals(1700000000, a.getCreatedTime());
    assertTrue(a.getForceDpop());
    assertEquals("r", a.getRarSettings().getSchemas()[0].getType());
    assertEquals("y", a.getIdJagTargetClients().get("x").getTargetClientId());
    assertEquals("mtls", a.getClientAuthMethod());
    assertEquals("publicKey", a.getClientAssertions()[0].getKeySource());
    assertEquals("i", a.getWorkloadClientAssertions()[0].getIssuer());
    assertTrue(a.getForcePar());
    assertTrue(a.getForceMtls());
    assertTrue(a.getAuthorizationCodeDisabled());
    assertTrue(a.getClientCredentialsDisabled());
    assertTrue(a.getRefreshTokenDisabled());
    assertTrue(a.getJwtBearerDisabled());
    assertTrue(a.getDeviceCodeDisabled());
    assertTrue(a.getTokenExchangeDisabled());
    assertTrue(a.getCibaEnabled());
    assertEquals("tag", a.getTags()[0]);
    assertEquals("grantAllScopes", a.getNoScopeBehavior());
    assertEquals("ds", a.getDefaultScopes()[0]);
    assertTrue(a.getAgentic());
    assertEquals("Owner", a.getCreatedBy());
  }

  @Test
  void testGetApplicationSecretByName() throws Exception {
    String[] secret = new String[1];
    ClassicHttpRequest sent = send("{\"cleartext\":\"s1\"}",
        () -> secret[0] = keyedService.getApplicationSecret("a", "named"));
    assertRequest(sent, "GET", "/v1/mgmt/thirdparty/app/secret");
    assertTrue(sent.getRequestUri().contains("id=a"));
    assertTrue(sent.getRequestUri().contains("secretName=named"));
    assertEquals("s1", secret[0]);

    sent = send("{\"cleartext\":\"s1\"}", () -> keyedService.getApplicationSecret("a", ""));
    assertTrue(sent.getRequestUri().endsWith("/v1/mgmt/thirdparty/app/secret?id=a"));

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.getApplicationSecret("", "named"));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testAddApplicationSecret() throws Exception {
    InboundAppAddSecretResponse[] res = new InboundAppAddSecretResponse[1];
    ClassicHttpRequest sent = send("{\"meta\":{\"id\":\"sid\",\"name\":\"n\",\"status\":\"active\","
        + "\"expireTime\":100,\"createdTime\":50},\"cleartext\":\"clear\"}",
        () -> res[0] = keyedService.addApplicationSecret("a", "n", 100));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/app/secret/add");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("id"));
    assertEquals("n", body.get("name"));
    assertEquals(100, body.get("expireTime"));
    assertEquals("clear", res[0].getSecret());
    InboundAppSecretMeta meta = res[0].getMeta();
    assertEquals("sid", meta.getId());
    assertEquals("n", meta.getName());
    assertEquals("active", meta.getStatus());
    assertEquals(100, meta.getExpireTime());
    assertEquals(50, meta.getCreatedTime());

    sent = send("{}", () -> keyedService.addApplicationSecret("a", "n", null));
    assertFalse(body(sent).containsKey("expireTime"));

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.addApplicationSecret("", "n", null));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.addApplicationSecret("a", "", null));
    assertEquals("The name argument is invalid", thrown.getMessage());
  }

  @Test
  void testRevokeApplicationSecret() throws Exception {
    InboundAppSecretMeta[][] res = new InboundAppSecretMeta[1][];
    ClassicHttpRequest sent = send("{\"secrets\":[{\"id\":\"s2\",\"name\":\"other\",\"status\":\"active\","
        + "\"expireTime\":0,\"createdTime\":5}]}", () -> res[0] = keyedService.revokeApplicationSecret("a", "n"));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/app/secret/revoke");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("id"));
    assertEquals("n", body.get("name"));
    assertEquals(1, res[0].length);
    assertEquals("s2", res[0][0].getId());
    assertEquals("other", res[0][0].getName());
    assertEquals("active", res[0][0].getStatus());
    assertEquals(0, res[0][0].getExpireTime());
    assertEquals(5, res[0][0].getCreatedTime());

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.revokeApplicationSecret("", "n"));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.revokeApplicationSecret("a", ""));
    assertEquals("The name argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllApplicationsWithOptions() throws Exception {
    LoadAllApplicationsResponse[] res = new LoadAllApplicationsResponse[1];
    ClassicHttpRequest sent = send("{\"apps\":[{\"id\":\"a\",\"agentic\":true,\"createdBy\":\"Owner\"}],\"total\":3}",
        () -> res[0] = keyedService.loadAllApplications(InboundAppLoadAllOptions.builder().page(1).limit(2)
            .createdBy("Owner Name").agentic(false).build()));
    assertRequest(sent, "GET", "/v1/mgmt/thirdparty/apps/load");
    String uri = sent.getRequestUri();
    assertTrue(uri.contains("page=1"));
    assertTrue(uri.contains("limit=2"));
    assertTrue(uri.contains("createdBy=Owner+Name"));
    assertTrue(uri.contains("agentic=false"));
    assertEquals(3, res[0].getTotal());
    assertEquals("a", res[0].getApps()[0].getId());
    assertEquals("Owner", res[0].getApps()[0].getCreatedBy());

    sent = send("{}", () -> keyedService.loadAllApplications((InboundAppLoadAllOptions) null));
    assertTrue(sent.getRequestUri().endsWith("/v1/mgmt/thirdparty/apps/load"));

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.loadAllApplications(InboundAppLoadAllOptions.builder().page(-1).build()));
    assertEquals("The options.page argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.loadAllApplications(InboundAppLoadAllOptions.builder().limit(-1).build()));
    assertEquals("The options.limit argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateConsent() throws Exception {
    InboundAppConsent[] res = new InboundAppConsent[1];
    ClassicHttpRequest sent = send("{\"consent\":{\"id\":\"c1\",\"appId\":\"a\",\"userId\":\"u\","
        + "\"scopes\":[\"s\"],\"grantedBy\":\"g\",\"createdTime\":10,\"tenantId\":\"t\",\"ExpireTime\":20,"
        + "\"modifiedTime\":30,\"authMethod\":\"idJag\",\"blocked\":true}}",
        () -> res[0] = keyedService.createConsent(InboundAppConsentCreateRequest.builder().appId("a").userId("u")
            .scopes(new String[] {"s"}).tenantId("t").expireTime(20).build()));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/consents/create");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("appId"));
    assertEquals("u", body.get("userId"));
    assertEquals(Collections.singletonList("s"), body.get("scopes"));
    assertEquals("t", body.get("tenantId"));
    assertEquals(20, body.get("expireTime"));
    InboundAppConsent c = res[0];
    assertEquals("c1", c.getId());
    assertEquals("a", c.getAppId());
    assertEquals("u", c.getUserId());
    assertEquals("s", c.getScopes()[0]);
    assertEquals("g", c.getGrantedBy());
    assertEquals(10, c.getCreatedTime());
    assertEquals("t", c.getTenantId());
    assertEquals(20, c.getExpireTime());
    assertEquals(30, c.getModifiedTime());
    assertEquals("idJag", c.getAuthMethod());
    assertTrue(c.getBlocked());

    send("{\"consent\":{\"expireTime\":40}}", () -> res[0] = keyedService.createConsent(
        InboundAppConsentCreateRequest.builder().appId("a").userId("u").build()));
    assertEquals(40, res[0].getExpireTime());

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createConsent(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.createConsent(
        InboundAppConsentCreateRequest.builder().userId("u").build()));
    assertEquals("The request.appId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.createConsent(
        InboundAppConsentCreateRequest.builder().appId("a").build()));
    assertEquals("The request.userId argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchConsentsLimit() throws Exception {
    ClassicHttpRequest sent = send("{\"consents\":[],\"total\":0}",
        () -> keyedService.searchConsents(InboundAppConsentSearchOptions.builder().appId("a").limit(5).build()));
    assertRequest(sent, "POST", "/v1/mgmt/thirdparty/consents/search");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("appId"));
    assertEquals(5, body.get("limit"));
  }

  private static void assertRequest(ClassicHttpRequest request, String method, String path) {
    assertEquals(method, request.getMethod());
    String uri = request.getRequestUri();
    assertTrue(uri.endsWith(path) || uri.contains(path + "?"), uri);
    assertEquals("Bearer " + KEYED_PROJECT_ID + ":" + KEYED_MANAGEMENT_KEY,
        request.getFirstHeader("Authorization").getValue());
  }

  private static Map<?, ?> body(ClassicHttpRequest request) throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }

  private static Object path(Map<?, ?> body, String... keys) {
    Object value = body;
    for (String key : keys) {
      value = ((Map<?, ?>) value).get(key);
    }
    return value;
  }

  private static ClassicHttpRequest send(String responseBody, Runnable call) throws IOException {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, responseBody);
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      call.run();
    }
    return captor.getValue();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static CloseableHttpClient mockHttpClient(ArgumentCaptor<ClassicHttpRequest> captor, String responseBody)
      throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    return httpClient;
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
