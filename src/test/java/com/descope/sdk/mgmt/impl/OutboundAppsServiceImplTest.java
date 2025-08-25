package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.model.outbound.LoadAllOutboundApplicationsResponse;
import com.descope.model.outbound.OutboundApp;
import com.descope.model.outbound.OutboundAppCreateResponse;
import com.descope.model.outbound.OutboundAppRequest;
import com.descope.model.outbound.OutboundAppToken;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.OutboundAppsService;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}
