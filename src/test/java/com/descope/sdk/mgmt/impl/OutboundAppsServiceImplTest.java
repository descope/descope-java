package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.outbound.CreateOutboundAppByTemplateRequest;
import com.descope.model.outbound.CreateOutboundSCIMConfigurationRequest;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.model.outbound.LoadAllOutboundApplicationsResponse;
import com.descope.model.outbound.OutboundApp;
import com.descope.model.outbound.OutboundAppConstants;
import com.descope.model.outbound.OutboundAppCreateResponse;
import com.descope.model.outbound.OutboundAppRequest;
import com.descope.model.outbound.OutboundAppTemplateOverrides;
import com.descope.model.outbound.OutboundAppTenantTokenUploadItem;
import com.descope.model.outbound.OutboundAppToken;
import com.descope.model.outbound.OutboundAppTokenUploadFailure;
import com.descope.model.outbound.OutboundAppURLParam;
import com.descope.model.outbound.OutboundAppUserTokenUploadItem;
import com.descope.model.outbound.OutboundSCIMConfiguration;
import com.descope.model.outbound.UpdateOutboundSCIMConfigurationRequest;
import com.descope.model.outbound.UploadOutboundAppTenantAPIKeyRequest;
import com.descope.model.outbound.UploadOutboundAppTenantTokenRequest;
import com.descope.model.outbound.UploadOutboundAppUserAPIKeyRequest;
import com.descope.model.outbound.UploadOutboundAppUserTokenRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.OutboundAppsService;
import com.descope.utils.UriUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class OutboundAppsServiceImplTest {

  private final Instant mockInstant = Instant.now();
  private final OutboundAppToken mockToken = 
      OutboundAppToken.builder()
        .id("someId")
        .appId("someAppId")
        .userId("someUserId")
        .lastRefreshTime(mockInstant)
        .build();
  private final FetchOutboundAppUserTokenResponse fetchOutboundAppUserTokenResponse = 
      FetchOutboundAppUserTokenResponse.builder()
        .token(mockToken)
        .build();
  private OutboundAppsService outboundAppsService;
  private final OutboundApp mockApp = OutboundApp.builder().id("aid").name("aname").build();

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.outboundAppsService =
        ManagementServiceBuilder.buildServices(client).getOutboundAppsService();
  }

  @Test
  void testCrudValidation() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.createApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.createApplication(OutboundAppRequest.builder().build()));
    assertEquals("The request.name argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class, () -> outboundAppsService.updateApplication(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.updateApplication(OutboundAppRequest.builder().build()));
    assertEquals("The request.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.updateApplication(OutboundAppRequest.builder().id("a").build()));
    assertEquals("The request.name argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class, () -> outboundAppsService.deleteApplication(""));
    assertEquals("The id argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class, () -> outboundAppsService.loadApplication(""));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testCrudSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(OutboundAppCreateResponse.builder().id("aid").build()).when(apiProxy).post(any(), any(), any());
    LoadAllOutboundApplicationsResponse listResp =
        LoadAllOutboundApplicationsResponse.builder().apps(new OutboundApp[] { mockApp }).build();
    doReturn(mockApp, listResp).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(SdkInfo.class))).thenReturn(apiProxy);
      OutboundAppCreateResponse created = outboundAppsService
          .createApplication(OutboundAppRequest.builder().name("n1").id("i1").build());
      assertEquals("aid", created.getId());
      outboundAppsService.updateApplication(OutboundAppRequest.builder().id("aid").name("n2").build());
      OutboundApp loaded = outboundAppsService.loadApplication("aid");
      assertEquals("aid", loaded.getId());
      OutboundApp[] all = outboundAppsService.loadAllApplications();
      assertEquals(1, all.length);
      outboundAppsService.deleteApplication("aid");
    }
  }

  @Test
  void testFetchForMissingRequestParts() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> outboundAppsService.fetchOutboundAppUserToken(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> outboundAppsService.fetchOutboundAppUserToken(new FetchOutboundAppUserTokenRequest()));
    assertNotNull(thrown);
    assertEquals("The appId argument is invalid", thrown.getMessage());
    thrown =
      assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchOutboundAppUserToken(
          new FetchOutboundAppUserTokenRequest("appId", "", null, null)));
    assertNotNull(thrown);
    assertEquals("The userId argument is invalid", thrown.getMessage());
    thrown =
      assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchOutboundAppUserToken(
          new FetchOutboundAppUserTokenRequest("appId", "userId", null, null)));
    assertNotNull(thrown);
    assertEquals("The scopes argument is invalid", thrown.getMessage());
  }

  @Test
  void testFetchForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(fetchOutboundAppUserTokenResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      // Also stub the single-argument overload used when projectId is blank
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(SdkInfo.class))).thenReturn(apiProxy);
      FetchOutboundAppUserTokenResponse response = outboundAppsService.fetchOutboundAppUserToken(
          FetchOutboundAppUserTokenRequest.builder()
          .appId("someAppId")
          .userId("someUserId")
          .scopes(Arrays.asList("scope1"))
          .build());
      assertNotNull(response.getToken());
      assertEquals("someUserId", response.getToken().getUserId());
      assertEquals("someAppId", response.getToken().getAppId());
      assertEquals("someId", response.getToken().getId());
      assertEquals(mockInstant, response.getToken().getLastRefreshTime());
    }
  }

  @Test
  void testDeleteByIdForMissingRequestParts() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> outboundAppsService.deleteOutboundAppTokenById(null));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteAllForMissingRequestParts() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> outboundAppsService.deleteOutboundAppUserTokens(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> outboundAppsService.deleteOutboundAppUserTokens(new DeleteOutboundAppUserTokensRequest()));
    assertNotNull(thrown);
    assertEquals("The appId argument is invalid", thrown.getMessage());
    thrown =
      assertThrows(ServerCommonException.class,
        () -> outboundAppsService.deleteOutboundAppUserTokens(
          new DeleteOutboundAppUserTokensRequest("appId", "")));
    assertNotNull(thrown);
    assertEquals("The userId argument is invalid", thrown.getMessage());
  }

  @Test
  void testFetchLatestUserTokenValidation() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> outboundAppsService.fetchLatestOutboundAppUserToken(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchLatestOutboundAppUserToken(
            FetchLatestOutboundAppUserTokenRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The appId argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchLatestOutboundAppUserToken(
            FetchLatestOutboundAppUserTokenRequest.builder().appId("app").build()));
    assertNotNull(thrown);
    assertEquals("The userId argument is invalid", thrown.getMessage());
  }

  @Test
  void testFetchLatestUserTokenSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(fetchOutboundAppUserTokenResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(SdkInfo.class))).thenReturn(apiProxy);

      FetchOutboundAppUserTokenResponse response = outboundAppsService.fetchLatestOutboundAppUserToken(
          FetchLatestOutboundAppUserTokenRequest.builder().appId("someAppId").userId("someUserId").build());
      assertNotNull(response);
      assertNotNull(response.getToken());
      assertEquals("someUserId", response.getToken().getUserId());
    }
  }

  @Test
  void testFetchTenantTokenByScopesValidation() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchOutboundAppTenantTokenByScopes(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchOutboundAppTenantTokenByScopes(
            FetchOutboundAppTenantTokenRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The appId argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchOutboundAppTenantTokenByScopes(
            FetchOutboundAppTenantTokenRequest.builder().appId("app").build()));
    assertNotNull(thrown);
    assertEquals("The tenantId argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchOutboundAppTenantTokenByScopes(
            FetchOutboundAppTenantTokenRequest.builder().appId("app").tenantId("t").build()));
    assertNotNull(thrown);
    assertEquals("The scopes argument is invalid", thrown.getMessage());
  }

  @Test
  void testFetchTenantTokenByScopesSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    FetchOutboundAppTenantTokenResponse tenantResp = FetchOutboundAppTenantTokenResponse.builder()
        .token(mockToken)
        .build();
    doReturn(tenantResp).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(SdkInfo.class))).thenReturn(apiProxy);
      FetchOutboundAppTenantTokenResponse response = outboundAppsService.fetchOutboundAppTenantTokenByScopes(
          FetchOutboundAppTenantTokenRequest.builder()
              .appId("someAppId")
              .tenantId("someTenant")
              .scopes(Arrays.asList("scope1"))
              .build());
      assertNotNull(response.getToken());
      assertEquals("someAppId", response.getToken().getAppId());
    }
  }

  @Test
  void testFetchLatestTenantTokenValidation() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchLatestOutboundAppTenantToken(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchLatestOutboundAppTenantToken(
            FetchOutboundAppTenantTokenRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The appId argument is invalid", thrown.getMessage());

    thrown = assertThrows(ServerCommonException.class,
        () -> outboundAppsService.fetchLatestOutboundAppTenantToken(
            FetchOutboundAppTenantTokenRequest.builder().appId("app").build()));
    assertNotNull(thrown);
    assertEquals("The tenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testFetchLatestTenantTokenSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    FetchOutboundAppTenantTokenResponse tenantResp = FetchOutboundAppTenantTokenResponse.builder()
        .token(mockToken)
        .build();
    doReturn(tenantResp).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(SdkInfo.class))).thenReturn(apiProxy);
      FetchOutboundAppTenantTokenResponse response = outboundAppsService.fetchLatestOutboundAppTenantToken(
          FetchOutboundAppTenantTokenRequest.builder()
              .appId("someAppId")
              .tenantId("someTenant")
              .build());
      assertNotNull(response.getToken());
      assertEquals("someAppId", response.getToken().getAppId());
    }
  }

  private static final String MGMT_PROJECT_ID = "P123456789012345678901234567";
  private static final String MGMT_KEY = "some-management-key";

  private OutboundAppsService mgmtKeyService() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(MGMT_PROJECT_ID)
        .managementKey(MGMT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return ManagementServiceBuilder.buildServices(client).getOutboundAppsService();
  }

  // Runs the call against a mocked HTTP client and returns the request it sent.
  @SuppressWarnings({"unchecked", "rawtypes"})
  private ClassicHttpRequest captureRequest(String responseBody, Runnable call) throws IOException {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
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
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      call.run();
    }
    ClassicHttpRequest request = captor.getValue();
    assertEquals("Bearer " + MGMT_PROJECT_ID + ":" + MGMT_KEY, request.getFirstHeader("Authorization").getValue());
    return request;
  }

  private static Map<?, ?> requestBody(ClassicHttpRequest request) throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }

  private static void assertInvalid(String name, Executable call) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, call);
    assertEquals("The " + name + " argument is invalid", thrown.getMessage());
  }

  private static final String APP_JSON = "{\"app\":{\"id\":\"aid\",\"name\":\"n\",\"description\":\"d\","
      + "\"clientId\":\"cid\",\"clientSecret\":\"cs\",\"logo\":\"l\",\"discoveryUrl\":\"du\","
      + "\"authorizationUrl\":\"au\",\"authorizationUrlParams\":[{\"key\":\"k\",\"value\":\"v\"}],"
      + "\"tokenUrl\":\"tu\",\"tokenUrlParams\":[{\"key\":\"k2\",\"value\":\"v2\"}],\"revocationUrl\":\"ru\","
      + "\"defaultScopes\":[\"s\"],\"defaultRedirectUrl\":\"dr\",\"callbackDomain\":\"cd\",\"pkce\":true,"
      + "\"accessType\":\"offline\",\"prompt\":[\"consent\"],\"appType\":\"oauth\",\"useDcr\":true,"
      + "\"dcrUrl\":\"dcr\",\"tenantId\":\"t1\",\"tokenEndpointAuthMethod\":\"private_key_jwt\","
      + "\"tokenEndpointAuthSigningAlg\":\"RS512\",\"useNonce\":true,\"unknownField\":1}}";

  private static void assertFullApp(OutboundApp app) {
    assertEquals("aid", app.getId());
    assertEquals("n", app.getName());
    assertEquals("d", app.getDescription());
    assertEquals("cid", app.getClientId());
    assertEquals("cs", app.getClientSecret());
    assertEquals("l", app.getLogo());
    assertEquals("du", app.getDiscoveryUrl());
    assertEquals("au", app.getAuthorizationUrl());
    assertEquals("k", app.getAuthorizationUrlParams().get(0).getKey());
    assertEquals("v", app.getAuthorizationUrlParams().get(0).getValue());
    assertEquals("tu", app.getTokenUrl());
    assertEquals("k2", app.getTokenUrlParams().get(0).getKey());
    assertEquals("ru", app.getRevocationUrl());
    assertEquals(Arrays.asList("s"), app.getDefaultScopes());
    assertEquals("dr", app.getDefaultRedirectUrl());
    assertEquals("cd", app.getCallbackDomain());
    assertEquals(true, app.getPkce());
    assertEquals(OutboundAppConstants.ACCESS_TYPE_OFFLINE, app.getAccessType());
    assertEquals(Arrays.asList(OutboundAppConstants.PROMPT_CONSENT), app.getPrompt());
    assertEquals(OutboundAppConstants.APP_TYPE_OAUTH, app.getAppType());
    assertEquals(true, app.getUseDcr());
    assertEquals("dcr", app.getDcrUrl());
    assertEquals("t1", app.getTenantId());
    assertEquals(OutboundAppConstants.TOKEN_ENDPOINT_AUTH_METHOD_PRIVATE_KEY_JWT, app.getTokenEndpointAuthMethod());
    assertEquals(OutboundAppConstants.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_RS512, app.getTokenEndpointAuthSigningAlg());
    assertEquals(true, app.getUseNonce());
  }

  @Test
  void testCreateApplicationWithAllFields() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    AtomicReference<OutboundAppCreateResponse> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(APP_JSON, () -> res.set(service.createApplication(
        OutboundAppRequest.builder()
            .id("aid")
            .name("n")
            .description("d")
            .clientId("cid")
            .clientSecret("cs")
            .logo("l")
            .discoveryUrl("du")
            .authorizationUrl("au")
            .authorizationUrlParams(Arrays.asList(OutboundAppURLParam.builder().key("k").value("v").build()))
            .tokenUrl("tu")
            .tokenUrlParams(Arrays.asList(OutboundAppURLParam.builder().key("k2").value("v2").build()))
            .revocationUrl("ru")
            .defaultScopes(Arrays.asList("s"))
            .defaultRedirectUrl("dr")
            .callbackDomain("cd")
            .pkce(true)
            .accessType(OutboundAppConstants.ACCESS_TYPE_OFFLINE)
            .prompt(Arrays.asList(OutboundAppConstants.PROMPT_CONSENT))
            .appType(OutboundAppConstants.APP_TYPE_OAUTH)
            .useDcr(true)
            .dcrUrl("dcr")
            .tenantId("t1")
            .tokenEndpointAuthMethod(OutboundAppConstants.TOKEN_ENDPOINT_AUTH_METHOD_PRIVATE_KEY_JWT)
            .tokenEndpointAuthSigningAlg(OutboundAppConstants.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_RS512)
            .useNonce(true)
            .build())));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/create"));
    Map<?, ?> body = requestBody(request);
    assertEquals(25, body.size());
    assertEquals("n", body.get("name"));
    assertEquals("cs", body.get("clientSecret"));
    assertEquals(Arrays.asList(mapOf("key", "k", "value", "v")), body.get("authorizationUrlParams"));
    assertEquals(Arrays.asList("consent"), body.get("prompt"));
    assertEquals("oauth", body.get("appType"));
    assertEquals("private_key_jwt", body.get("tokenEndpointAuthMethod"));
    assertEquals("RS512", body.get("tokenEndpointAuthSigningAlg"));
    assertEquals(true, body.get("useNonce"));
    assertEquals(true, body.get("pkce"));
    assertFullApp(res.get().getApp());
  }

  @Test
  void testCreateApplicationOmitsUnsetFields() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    ClassicHttpRequest request = captureRequest(APP_JSON,
        () -> service.createApplication(OutboundAppRequest.builder().name("n").build()));
    assertEquals(mapOf("name", "n"), requestBody(request));
  }

  @Test
  void testCreateApplicationByTemplate() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.createApplicationByTemplate(null));
    assertInvalid("request.templateId",
        () -> service.createApplicationByTemplate(CreateOutboundAppByTemplateRequest.builder().build()));
    AtomicReference<OutboundApp> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(APP_JSON, () -> res.set(service.createApplicationByTemplate(
        CreateOutboundAppByTemplateRequest.builder()
            .templateId("tmpl")
            .id("aid")
            .clientId("cid")
            .clientSecret("cs")
            .tenantId("t1")
            .overrides(OutboundAppTemplateOverrides.builder().name("n").pkce(false)
                .accessType(OutboundAppConstants.ACCESS_TYPE_ONLINE).build())
            .build())));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/create/bytemplate"));
    Map<?, ?> body = requestBody(request);
    assertEquals(6, body.size());
    assertEquals("tmpl", body.get("templateId"));
    assertEquals("aid", body.get("id"));
    assertEquals("cid", body.get("clientId"));
    assertEquals("cs", body.get("clientSecret"));
    assertEquals("t1", body.get("tenantId"));
    assertEquals(mapOf("name", "n", "pkce", false, "accessType", "online"), body.get("overrides"));
    assertFullApp(res.get());
  }

  @Test
  void testListAppsWithUserToken() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("userId", () -> service.listAppsWithUserToken("", "t1"));
    AtomicReference<List<String>> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest("{\"appIds\":[\"a1\",\"a2\"]}",
        () -> res.set(service.listAppsWithUserToken("u 1", "t1")));
    assertEquals("GET", request.getMethod());
    URI uri = new URI(request.getRequestUri());
    assertEquals("/v1/mgmt/outbound/apps-with-user-token", uri.getPath());
    assertEquals(mapOf("userId", Arrays.asList("u 1"), "tenantId", Arrays.asList("t1")),
        UriUtils.splitQuery(request.getUri().toURL()));
    assertEquals(Arrays.asList("a1", "a2"), res.get());

    request = captureRequest("{}", () -> res.set(service.listAppsWithUserToken("u1", null)));
    assertEquals(mapOf("userId", Arrays.asList("u1")), UriUtils.splitQuery(request.getUri().toURL()));
    assertTrue(res.get().isEmpty());
  }

  @Test
  void testUploadUserAPIKey() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.uploadUserAPIKey(null));
    assertInvalid("appId", () -> service.uploadUserAPIKey(
        UploadOutboundAppUserAPIKeyRequest.builder().userId("u1").apiKey("k").build()));
    assertInvalid("userId", () -> service.uploadUserAPIKey(
        UploadOutboundAppUserAPIKeyRequest.builder().appId("a1").apiKey("k").build()));
    assertInvalid("apiKey", () -> service.uploadUserAPIKey(
        UploadOutboundAppUserAPIKeyRequest.builder().appId("a1").userId("u1").build()));
    ClassicHttpRequest request = captureRequest("{}", () -> service.uploadUserAPIKey(
        UploadOutboundAppUserAPIKeyRequest.builder().appId("a1").userId("u1").apiKey("k").tenantId("t1")
            .externalIdentifier("ext").build()));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/user/apikey/upload"));
    assertEquals(mapOf("appId", "a1", "userId", "u1", "apiKey", "k", "tenantId", "t1", "externalIdentifier", "ext"),
        requestBody(request));
  }

  @Test
  void testUploadTenantAPIKey() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.uploadTenantAPIKey(null));
    assertInvalid("appId", () -> service.uploadTenantAPIKey(
        UploadOutboundAppTenantAPIKeyRequest.builder().tenantId("t1").apiKey("k").build()));
    assertInvalid("tenantId", () -> service.uploadTenantAPIKey(
        UploadOutboundAppTenantAPIKeyRequest.builder().appId("a1").apiKey("k").build()));
    assertInvalid("apiKey", () -> service.uploadTenantAPIKey(
        UploadOutboundAppTenantAPIKeyRequest.builder().appId("a1").tenantId("t1").build()));
    ClassicHttpRequest request = captureRequest("{}", () -> service.uploadTenantAPIKey(
        UploadOutboundAppTenantAPIKeyRequest.builder().appId("a1").tenantId("t1").apiKey("k")
            .externalIdentifier("ext").build()));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/tenant/apikey/upload"));
    assertEquals(mapOf("appId", "a1", "tenantId", "t1", "apiKey", "k", "externalIdentifier", "ext"),
        requestBody(request));
  }

  @Test
  void testUploadUserToken() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.uploadUserToken(null));
    assertInvalid("appId", () -> service.uploadUserToken(
        UploadOutboundAppUserTokenRequest.builder().userId("u1").build()));
    assertInvalid("userId", () -> service.uploadUserToken(
        UploadOutboundAppUserTokenRequest.builder().appId("a1").build()));
    ClassicHttpRequest request = captureRequest("{}", () -> service.uploadUserToken(
        UploadOutboundAppUserTokenRequest.builder().appId("a1").userId("u1").tenantId("t1").refreshToken("rt")
            .accessToken("at").accessTokenExpiry(100).accessTokenType("Bearer").scopes(Arrays.asList("s"))
            .externalIdentifier("ext").idToken("idt").verifyRefresh(true).grantedBy("g")
            .refreshTokenExpiry(200).build()));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/user/oauthtoken/upload"));
    Map<?, ?> body = requestBody(request);
    assertEquals(13, body.size());
    assertEquals("u1", body.get("userId"));
    assertEquals(100, body.get("accessTokenExpiry"));
    assertEquals(200, body.get("refreshTokenExpiry"));
    assertEquals(true, body.get("verifyRefresh"));
    assertEquals(Arrays.asList("s"), body.get("scopes"));
  }

  @Test
  void testUploadTenantToken() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.uploadTenantToken(null));
    assertInvalid("appId", () -> service.uploadTenantToken(
        UploadOutboundAppTenantTokenRequest.builder().tenantId("t1").build()));
    assertInvalid("tenantId", () -> service.uploadTenantToken(
        UploadOutboundAppTenantTokenRequest.builder().appId("a1").build()));
    ClassicHttpRequest request = captureRequest("{}", () -> service.uploadTenantToken(
        UploadOutboundAppTenantTokenRequest.builder().appId("a1").tenantId("t1").accessToken("at").build()));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/tenant/oauthtoken/upload"));
    assertEquals(mapOf("appId", "a1", "tenantId", "t1", "accessToken", "at"), requestBody(request));
  }

  private static final String FAILURES_JSON = "{\"failures\":[{\"appId\":\"a1\",\"userId\":\"u1\","
      + "\"tenantId\":\"t1\",\"errorCode\":\"E1\",\"reason\":\"bad\"}]}";

  private static void assertFailure(List<OutboundAppTokenUploadFailure> failures) {
    assertEquals(1, failures.size());
    assertEquals("a1", failures.get(0).getAppId());
    assertEquals("u1", failures.get(0).getUserId());
    assertEquals("t1", failures.get(0).getTenantId());
    assertEquals("E1", failures.get(0).getErrorCode());
    assertEquals("bad", failures.get(0).getReason());
  }

  @Test
  void testBatchUploadUserTokens() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("tokens", () -> service.batchUploadUserTokens(null));
    assertInvalid("tokens", () -> service.batchUploadUserTokens(Collections.emptyList()));
    AtomicReference<List<OutboundAppTokenUploadFailure>> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(FAILURES_JSON, () -> res.set(service.batchUploadUserTokens(
        Arrays.asList(OutboundAppUserTokenUploadItem.builder().appId("a1").userId("u1").refreshToken("rt")
            .accessTokenExpiry(100).build()))));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/user/oauthtoken/batch/upload"));
    assertEquals(mapOf("tokens", Arrays.asList(
        mapOf("appId", "a1", "userId", "u1", "refreshToken", "rt", "accessTokenExpiry", 100))),
        requestBody(request));
    assertFailure(res.get());
    captureRequest("{}", () -> res.set(service.batchUploadUserTokens(
        Arrays.asList(OutboundAppUserTokenUploadItem.builder().appId("a1").userId("u1").build()))));
    assertTrue(res.get().isEmpty());
  }

  @Test
  void testBatchUploadTenantTokens() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("tokens", () -> service.batchUploadTenantTokens(null));
    assertInvalid("tokens", () -> service.batchUploadTenantTokens(Collections.emptyList()));
    AtomicReference<List<OutboundAppTokenUploadFailure>> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(FAILURES_JSON, () -> res.set(service.batchUploadTenantTokens(
        Arrays.asList(OutboundAppTenantTokenUploadItem.builder().appId("a1").tenantId("t1").accessToken("at")
            .build()))));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/app/tenant/oauthtoken/batch/upload"));
    assertEquals(mapOf("tokens", Arrays.asList(mapOf("appId", "a1", "tenantId", "t1", "accessToken", "at"))),
        requestBody(request));
    assertFailure(res.get());
  }

  private static final String SCIM_JSON = "{\"configuration\":{\"appId\":\"a1\",\"configuration\":{\"k\":\"v\"},"
      + "\"enabled\":true,\"lastExportTime\":10,\"lastProcessingTime\":20,\"failures\":3,\"version\":\"7\"}}";

  private static void assertScim(OutboundSCIMConfiguration cfg) {
    assertEquals("a1", cfg.getAppId());
    assertEquals(mapOf("k", "v"), cfg.getConfiguration());
    assertEquals(true, cfg.getEnabled());
    assertEquals(10, cfg.getLastExportTime());
    assertEquals(20, cfg.getLastProcessingTime());
    assertEquals(3, cfg.getFailures());
    assertEquals(7L, cfg.getVersion());
  }

  @Test
  void testCreateSCIMConfiguration() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.createSCIMConfiguration(null));
    assertInvalid("appId", () -> service.createSCIMConfiguration(
        CreateOutboundSCIMConfigurationRequest.builder().configuration(mapOf("k", "v")).build()));
    assertInvalid("configuration", () -> service.createSCIMConfiguration(
        CreateOutboundSCIMConfigurationRequest.builder().appId("a1").build()));
    AtomicReference<OutboundSCIMConfiguration> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(SCIM_JSON, () -> res.set(service.createSCIMConfiguration(
        CreateOutboundSCIMConfigurationRequest.builder().appId("a1").configuration(mapOf("k", "v")).build())));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/scim/create"));
    assertEquals(mapOf("appId", "a1", "configuration", mapOf("k", "v")), requestBody(request));
    assertScim(res.get());
  }

  @Test
  void testUpdateSCIMConfiguration() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("request", () -> service.updateSCIMConfiguration(null));
    assertInvalid("appId", () -> service.updateSCIMConfiguration(
        UpdateOutboundSCIMConfigurationRequest.builder().build()));
    AtomicReference<OutboundSCIMConfiguration> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(SCIM_JSON, () -> res.set(service.updateSCIMConfiguration(
        UpdateOutboundSCIMConfigurationRequest.builder().appId("a1").configuration(mapOf("k", "v")).version(7L)
            .build())));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/scim/update"));
    assertEquals(mapOf("appId", "a1", "configuration", mapOf("k", "v"), "version", "7"), requestBody(request));
    assertScim(res.get());
    // version may also arrive as a number
    captureRequest(SCIM_JSON.replace("\"7\"", "7"), () -> res.set(service.updateSCIMConfiguration(
        UpdateOutboundSCIMConfigurationRequest.builder().appId("a1").build())));
    assertScim(res.get());
  }

  @Test
  void testSetSCIMEnabled() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("appId", () -> service.setSCIMEnabled("", true));
    AtomicReference<OutboundSCIMConfiguration> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(SCIM_JSON, () -> res.set(service.setSCIMEnabled("a1", false)));
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/scim/enabled/set"));
    assertEquals(mapOf("appId", "a1", "enabled", false), requestBody(request));
    assertScim(res.get());
  }

  @Test
  void testLoadSCIMConfiguration() throws Exception {
    OutboundAppsService service = mgmtKeyService();
    assertInvalid("appId", () -> service.loadSCIMConfiguration(" "));
    AtomicReference<OutboundSCIMConfiguration> res = new AtomicReference<>();
    ClassicHttpRequest request = captureRequest(SCIM_JSON, () -> res.set(service.loadSCIMConfiguration("a/1")));
    assertEquals("GET", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v1/mgmt/outbound/scim/a%2F1"));
    assertScim(res.get());
  }
}
