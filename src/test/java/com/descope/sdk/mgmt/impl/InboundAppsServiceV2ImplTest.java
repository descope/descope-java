package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.inbound.InboundAppCibaSettings;
import com.descope.model.inbound.InboundAppClaimTokenTargets;
import com.descope.model.inbound.InboundAppClientAssertion;
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
import com.descope.model.inbound.InboundAppScope;
import com.descope.model.inbound.InboundAppScopeClaimMapping;
import com.descope.model.inbound.InboundAppSessionSettings;
import com.descope.model.inbound.InboundAppV2;
import com.descope.model.inbound.InboundAppV2Request;
import com.descope.model.inbound.InboundAppWorkloadClientAssertion;
import com.descope.model.inbound.LoadAllApplicationsV2Response;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.InboundAppsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class InboundAppsServiceV2ImplTest {

  private static final String KEYED_PROJECT_ID = "P123456789012345678901234567";
  private static final String KEYED_MANAGEMENT_KEY = "some-management-key";
  private static final String FULL_APP = "{\"id\":\"a\",\"name\":\"n\",\"description\":\"d\",\"clientId\":\"c\","
      + "\"logo\":\"l\",\"loginPageUrl\":\"https://login.com\",\"approvedCallbackUrls\":[\"https://cb.com\"],"
      + "\"permissionsScopes\":[{\"name\":\"ps\",\"description\":\"psd\",\"values\":[\"v\"],\"optional\":true}],"
      + "\"dynamic\":true,\"status\":\"active\","
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
      + "\"tokenExchangeDisabled\":true,\"cibaEnabled\":true,\"tags\":[\"tag\"],"
      + "\"noScopeBehavior\":\"grantAllScopes\","
      + "\"defaultScopes\":[\"ds\"],\"agentic\":true,\"createdBy\":\"Owner\",\"unknownField\":1}";
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
  void testCreateApplicationV2() throws Exception {
    Map<String, InboundAppIssuerSettings> issuers = new HashMap<>();
    issuers.put("https://issuer.com", InboundAppIssuerSettings.builder().jwksUri("https://issuer.com/jwks")
        .signAlgorithm(InboundAppIssuerSettings.SIGN_ALGORITHM_RS256).jitDisabled(true).build());
    Map<String, InboundAppIdJagTargetClient> targets = new HashMap<>();
    targets.put("c1", InboundAppIdJagTargetClient.builder().targetClientId("c2").build());
    InboundAppV2Request request = InboundAppV2Request.builder()
        .name("n1")
        .description("d1")
        .logo("l1")
        .loginPageUrl("https://login.com")
        .approvedCallbackUrls(new String[] {"https://cb.com"})
        .permissionsScopes(new InboundAppScope[] {InboundAppScope.builder().name("ps").build()})
        .consentFlowId("flow1")
        .logoUrl("https://logo.com")
        .jwtBearerSettings(InboundAppJwtBearerSettings.builder().issuers(issuers).build())
        .sessionSettings(InboundAppSessionSettings.builder().enabled(true).refreshTokenExpiration(3)
            .refreshTokenExpirationUnit(InboundAppSessionSettings.UNIT_DAYS).build())
        .nonConfidentialClient(false)
        .clientId("cid")
        .clientSecret("csecret")
        .defaultAudience(InboundAppV2Request.DEFAULT_AUDIENCE_CLIENT_ID)
        .customAttributes(Collections.singletonMap("k", "v"))
        .clientType(InboundAppV2Request.CLIENT_TYPE_PUBLIC)
        .forcePkce(true)
        .scopeClaimMapping(new InboundAppScopeClaimMapping[] {InboundAppScopeClaimMapping.builder().scope("s1")
            .claims(Collections.singletonMap("c", "{{user.email}}")).mandatory(true).build()})
        .mtlsClientAuthMethods(InboundAppMtlsClientAuthMethods.builder().tlsClientAuth(InboundAppMtlsClientAuth
            .builder().credentials(new InboundAppMtlsCredential[] {InboundAppMtlsCredential.builder().name("m1")
                .build()}).build()).build())
        .idJagTargetClients(targets)
        .clientAuthMethod(InboundAppV2Request.CLIENT_AUTH_METHOD_PRIVATE_KEY_JWT)
        .clientAssertions(new InboundAppClientAssertion[] {InboundAppClientAssertion.builder()
            .keySource(InboundAppClientAssertion.KEY_SOURCE_JWKS).jwksUri("https://jwks.com").build()})
        .tags(new String[] {"t1"})
        .noScopeBehavior(InboundAppV2Request.NO_SCOPE_BEHAVIOR_GRANT_DEFAULT_SCOPES)
        .defaultScopes(new String[] {"s1"})
        .agentic(true)
        .createdBy("u1")
        .build();
    InboundAppCreateResponse[] res = new InboundAppCreateResponse[1];
    ClassicHttpRequest sent = send("{\"id\":\"i1\",\"cleartext\":\"secret\",\"clientId\":\"cid\"}",
        () -> res[0] = keyedService.createApplicationV2(request));
    assertRequest(sent, "POST", "/v2/mgmt/thirdparty/app/create");
    assertEquals("i1", res[0].getId());
    assertEquals("secret", res[0].getSecret());
    assertEquals("cid", res[0].getClientId());
    Map<?, ?> body = body(sent);
    assertFalse(body.containsKey("id"));
    assertEquals("n1", body.get("name"));
    assertEquals("d1", body.get("description"));
    assertEquals("l1", body.get("logo"));
    assertEquals("https://login.com", body.get("loginPageUrl"));
    assertEquals(Collections.singletonList("https://cb.com"), body.get("approvedCallbackUrls"));
    assertEquals("ps", ((Map<?, ?>) ((List<?>) body.get("permissionsScopes")).get(0)).get("name"));
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
    Map<?, ?> mapping = (Map<?, ?>) ((List<?>) body.get("scopeClaimMapping")).get(0);
    assertEquals("s1", mapping.get("scope"));
    assertEquals(true, mapping.get("mandatory"));
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
    assertFalse(body.containsKey("attributesScopes"));
    assertFalse(body.containsKey("forceDpop"));
    assertFalse(body.containsKey("cibaEnabled"));
    assertFalse(body.containsKey("rarSettings"));
  }

  @Test
  void testCreateApplicationV2Checks() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createApplicationV2(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createApplicationV2(InboundAppV2Request.builder().build()));
    assertEquals("The request.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createApplicationV2(InboundAppV2Request.builder().id("a").name("n").build()));
    assertEquals("The request.id argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateApplicationV2() throws Exception {
    ClassicHttpRequest sent = send("{}", () -> keyedService.updateApplicationV2(InboundAppV2Request.builder()
        .id("a").name("n1").loginPageUrl("https://login.com").agentic(false).createdBy("").forceDpop(false)
        .cibaEnabled(true).cibaSettings(InboundAppCibaSettings.builder().enabled(true)
            .notificationMethod(InboundAppCibaSettings.NOTIFICATION_METHOD_PUSH).build())
        .jarSettings(InboundAppJarSettings.builder().enforce(true).maxLifetimeSeconds(60).build())
        .tags(new String[0]).build()));
    assertRequest(sent, "POST", "/v2/mgmt/thirdparty/app/update");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("id"));
    assertEquals("n1", body.get("name"));
    assertEquals("https://login.com", body.get("loginPageUrl"));
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
  void testUpdateApplicationV2Checks() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.updateApplicationV2(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.updateApplicationV2(InboundAppV2Request.builder().name("n")
            .loginPageUrl("u").build()));
    assertEquals("The request.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.updateApplicationV2(InboundAppV2Request.builder().id("a")
            .loginPageUrl("u").build()));
    assertEquals("The request.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.updateApplicationV2(InboundAppV2Request.builder().id("a").name("n").build()));
    assertEquals("The request.loginPageUrl argument is invalid", thrown.getMessage());
  }

  @Test
  void testPatchApplicationV2() throws Exception {
    ClassicHttpRequest sent = send("{}", () -> keyedService.patchApplicationV2(InboundAppV2Request.builder().id("a")
        .forcePar(true).forceMtls(false).authorizationCodeDisabled(true).refreshTokenDisabled(false)
        .rarSettings(InboundAppRarSettings.builder().schemas(new InboundAppRarTypeSchema[] {
            InboundAppRarTypeSchema.builder().type("payment").schema(Collections.singletonMap("type", "object"))
                .build()}).build())
        .workloadClientAssertions(new InboundAppWorkloadClientAssertion[] {InboundAppWorkloadClientAssertion
            .builder().federationType(InboundAppWorkloadClientAssertion.FEDERATION_TYPE_SPIFFE)
            .trustDomain("example.org").allowedSubjects(new String[] {"spiffe://example.org/a"}).build()})
        .scopeClaimMapping(new InboundAppScopeClaimMapping[] {InboundAppScopeClaimMapping.builder().scope("s")
            .claimTargets(Collections.singletonMap("c", InboundAppClaimTokenTargets.builder()
                .tokens(new String[] {InboundAppClaimTokenTargets.TOKEN_ACCESS_TOKEN}).build())).build()})
        .build()));
    assertRequest(sent, "POST", "/v2/mgmt/thirdparty/app/patch");
    Map<?, ?> body = body(sent);
    assertEquals("a", body.get("id"));
    assertEquals(true, body.get("forcePar"));
    assertEquals(false, body.get("forceMtls"));
    assertEquals(true, body.get("authorizationCodeDisabled"));
    assertEquals(false, body.get("refreshTokenDisabled"));
    assertEquals("payment", ((Map<?, ?>) ((List<?>) path(body, "rarSettings", "schemas")).get(0)).get("type"));
    assertEquals("spiffe", ((Map<?, ?>) ((List<?>) body.get("workloadClientAssertions")).get(0))
        .get("federationType"));
    Map<?, ?> mapping = (Map<?, ?>) ((List<?>) body.get("scopeClaimMapping")).get(0);
    assertEquals(Collections.singletonList("accessToken"), path(mapping, "claimTargets", "c", "tokens"));
    assertFalse(body.containsKey("name"));
    assertFalse(body.containsKey("agentic"));
    assertFalse(body.containsKey("createdBy"));
  }

  @Test
  void testPatchApplicationV2Checks() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.patchApplicationV2(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.patchApplicationV2(InboundAppV2Request.builder().name("n").build()));
    assertEquals("The request.id argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateAndPatchV2RejectCreateOnlyFields() {
    InboundAppV2Request[] requests = {
        InboundAppV2Request.builder().id("a").name("n").loginPageUrl("u").consentFlowId("f").build(),
        InboundAppV2Request.builder().id("a").name("n").loginPageUrl("u").nonConfidentialClient(true).build(),
        InboundAppV2Request.builder().id("a").name("n").loginPageUrl("u").clientId("c").build(),
        InboundAppV2Request.builder().id("a").name("n").loginPageUrl("u").clientSecret("s").build(),
    };
    String[] fields = {"consentFlowId", "nonConfidentialClient", "clientId", "clientSecret"};
    for (int i = 0; i < requests.length; i++) {
      InboundAppV2Request request = requests[i];
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> inboundAppsService.updateApplicationV2(request));
      assertEquals("The request." + fields[i] + " argument is invalid", thrown.getMessage());
      thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.patchApplicationV2(request));
      assertEquals("The request." + fields[i] + " argument is invalid", thrown.getMessage());
    }
  }

  @Test
  void testLoadApplicationV2() throws Exception {
    InboundAppV2[] app = new InboundAppV2[1];
    ClassicHttpRequest sent = send(FULL_APP, () -> app[0] = keyedService.loadApplicationV2("a"));
    assertRequest(sent, "GET", "/v2/mgmt/thirdparty/app/load?id=a");
    assertFullApp(app[0]);

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.loadApplicationV2(""));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadApplicationByClientIdV2() throws Exception {
    InboundAppV2[] app = new InboundAppV2[1];
    ClassicHttpRequest sent = send(FULL_APP, () -> app[0] = keyedService.loadApplicationByClientIdV2("c"));
    assertRequest(sent, "GET", "/v2/mgmt/thirdparty/app/load?clientId=c");
    assertFullApp(app[0]);

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.loadApplicationByClientIdV2(""));
    assertEquals("The clientId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllApplicationsV2() throws Exception {
    LoadAllApplicationsV2Response[] res = new LoadAllApplicationsV2Response[1];
    ClassicHttpRequest sent = send("{\"apps\":[" + FULL_APP + "],\"total\":3}",
        () -> res[0] = keyedService.loadAllApplicationsV2(InboundAppLoadAllOptions.builder().page(1).limit(2)
            .createdBy("Owner Name").agentic(false).build()));
    assertRequest(sent, "POST", "/v2/mgmt/thirdparty/apps/load");
    Map<?, ?> body = body(sent);
    assertEquals(1, body.get("page"));
    assertEquals(2, body.get("limit"));
    assertEquals("Owner Name", body.get("createdBy"));
    assertEquals(false, body.get("agentic"));
    assertEquals(3, res[0].getTotal());
    assertEquals(1, res[0].getApps().length);
    assertFullApp(res[0].getApps()[0]);

    sent = send("{}", () -> res[0] = keyedService.loadAllApplicationsV2(null));
    assertTrue(body(sent).isEmpty());

    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.loadAllApplicationsV2(InboundAppLoadAllOptions.builder().page(-1).build()));
    assertEquals("The options.page argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.loadAllApplicationsV2(InboundAppLoadAllOptions.builder().limit(-1).build()));
    assertEquals("The options.limit argument is invalid", thrown.getMessage());
  }

  private static void assertFullApp(InboundAppV2 a) {
    assertEquals("a", a.getId());
    assertEquals("n", a.getName());
    assertEquals("d", a.getDescription());
    assertEquals("c", a.getClientId());
    assertEquals("l", a.getLogo());
    assertEquals("https://login.com", a.getLoginPageUrl());
    assertEquals("https://cb.com", a.getApprovedCallbackUrls()[0]);
    assertEquals("ps", a.getPermissionsScopes()[0].getName());
    assertEquals("psd", a.getPermissionsScopes()[0].getDescription());
    assertEquals("v", a.getPermissionsScopes()[0].getValues()[0]);
    assertTrue(a.getPermissionsScopes()[0].isOptional());
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
    assertEquals(7L, a.getMtlsClientAuthMethods().getSelfSignedTlsClientAuth().getCredentials()[0].getCreatedAt());
    assertEquals(1700000000L, a.getCreatedTime());
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

  private static void assertRequest(ClassicHttpRequest request, String method, String path) {
    assertEquals(method, request.getMethod());
    String uri = request.getRequestUri();
    assertTrue(uri.endsWith(path), uri);
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
}
