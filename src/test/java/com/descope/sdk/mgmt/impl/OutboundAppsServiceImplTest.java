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
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
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

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.outboundAppsService =
        ManagementServiceBuilder.buildServices(client).getOutboundAppsService();
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
}
