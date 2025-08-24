package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN_LATEST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN_LATEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OutboundAppsByTokenServiceImplTest {

  private Client client;

  @BeforeEach
  void setup() {
    client = mock(Client.class);
  when(client.getProjectId()).thenReturn("proj");
  when(client.getManagementKey()).thenReturn("key");
  when(client.getUri()).thenReturn("https://api");
    when(client.getSdkInfo()).thenReturn(null);
  }

  @Test
  void validationErrors() {
    OutboundAppsByTokenServiceImpl svc = new OutboundAppsByTokenServiceImpl(client);
    assertThrows(ServerCommonException.class, () -> svc.fetchLatestOutboundAppUserToken(null, null));
    assertThrows(ServerCommonException.class, () -> svc.fetchOutboundAppTenantTokenByScopes(" ", null));

    assertThrows(ServerCommonException.class,
        () -> svc.fetchOutboundAppUserTokenByScopes("t", new FetchOutboundAppUserTokenRequest()));

    FetchOutboundAppUserTokenRequest u = new FetchOutboundAppUserTokenRequest();
    u.setAppId("app");
    assertThrows(ServerCommonException.class, () -> svc.fetchOutboundAppUserTokenByScopes("t", u));
    u.setUserId("u");
    assertThrows(ServerCommonException.class, () -> svc.fetchOutboundAppUserTokenByScopes("t", u));

    FetchOutboundAppTenantTokenRequest tr = new FetchOutboundAppTenantTokenRequest();
    tr.setAppId("app");
    assertThrows(ServerCommonException.class, () -> svc.fetchOutboundAppTenantTokenByScopes("t", tr));
    tr.setTenantId("ten");
    assertThrows(ServerCommonException.class, () -> svc.fetchOutboundAppTenantTokenByScopes("t", tr));
  }

  @Test
  void fetchUserTokenByScopes() throws DescopeException {
    try (MockedStatic<ApiProxyBuilder> builder = org.mockito.Mockito.mockStatic(ApiProxyBuilder.class)) {
      ApiProxy proxy = mock(ApiProxy.class);
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(proxy);

      FetchOutboundAppUserTokenResponse resp = new FetchOutboundAppUserTokenResponse();
  when(proxy.post(any(), any(), eq(FetchOutboundAppUserTokenResponse.class)))
          .thenReturn(resp);

      OutboundAppsByTokenServiceImpl svc = new OutboundAppsByTokenServiceImpl(client);
      FetchOutboundAppUserTokenRequest req = new FetchOutboundAppUserTokenRequest();
      req.setAppId("app");
      req.setUserId("u");
      req.setScopes(Arrays.asList("s1"));

      FetchOutboundAppUserTokenResponse out = svc.fetchOutboundAppUserTokenByScopes("token", req);
      assertNotNull(out);
      assertEquals(resp, out);
    }
  }

  @Test
  void fetchUserTokenLatest() throws DescopeException {
    try (MockedStatic<ApiProxyBuilder> builder = org.mockito.Mockito.mockStatic(ApiProxyBuilder.class)) {
      ApiProxy proxy = mock(ApiProxy.class);
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(proxy);

      FetchOutboundAppUserTokenResponse resp = new FetchOutboundAppUserTokenResponse();
  when(proxy.post(any(), any(), eq(FetchOutboundAppUserTokenResponse.class)))
          .thenReturn(resp);

      OutboundAppsByTokenServiceImpl svc = new OutboundAppsByTokenServiceImpl(client);
      FetchLatestOutboundAppUserTokenRequest req = new FetchLatestOutboundAppUserTokenRequest();
      req.setAppId("app");
      req.setUserId("u");

      FetchOutboundAppUserTokenResponse out = svc.fetchLatestOutboundAppUserToken("token", req);
      assertNotNull(out);
      assertEquals(resp, out);
    }
  }

  @Test
  void fetchTenantTokenByScopes() throws DescopeException {
    try (MockedStatic<ApiProxyBuilder> builder = org.mockito.Mockito.mockStatic(ApiProxyBuilder.class)) {
      ApiProxy proxy = mock(ApiProxy.class);
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(proxy);

      FetchOutboundAppTenantTokenResponse resp = new FetchOutboundAppTenantTokenResponse();
  when(proxy.post(any(), any(), eq(FetchOutboundAppTenantTokenResponse.class)))
          .thenReturn(resp);

      OutboundAppsByTokenServiceImpl svc = new OutboundAppsByTokenServiceImpl(client);
      FetchOutboundAppTenantTokenRequest req = new FetchOutboundAppTenantTokenRequest();
      req.setAppId("app");
      req.setTenantId("t");
      req.setScopes(Arrays.asList("s1"));

      FetchOutboundAppTenantTokenResponse out = svc.fetchOutboundAppTenantTokenByScopes("token", req);
      assertNotNull(out);
      assertEquals(resp, out);
    }
  }

  @Test
  void fetchTenantTokenLatest() throws DescopeException {
    try (MockedStatic<ApiProxyBuilder> builder = org.mockito.Mockito.mockStatic(ApiProxyBuilder.class)) {
      ApiProxy proxy = mock(ApiProxy.class);
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(proxy);

      FetchOutboundAppTenantTokenResponse resp = new FetchOutboundAppTenantTokenResponse();
  when(proxy.post(any(), any(), eq(FetchOutboundAppTenantTokenResponse.class)))
          .thenReturn(resp);

      OutboundAppsByTokenServiceImpl svc = new OutboundAppsByTokenServiceImpl(client);
      FetchOutboundAppTenantTokenRequest req = new FetchOutboundAppTenantTokenRequest();
      req.setAppId("app");
      req.setTenantId("t");

      FetchOutboundAppTenantTokenResponse out = svc.fetchLatestOutboundAppTenantToken("token", req);
      assertNotNull(out);
      assertEquals(resp, out);
    }
  }
}
