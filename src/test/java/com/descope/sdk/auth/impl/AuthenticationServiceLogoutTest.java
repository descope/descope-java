package com.descope.sdk.auth.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.IDPResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.AuthenticationService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AuthenticationServiceLogoutTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String REFRESH_TOKEN = "some-refresh-token";

  private final AuthenticationService authService = AuthenticationServiceBuilder.buildServices(Client.builder()
      .uri("https://api.descope.com")
      .projectId(PROJECT_ID)
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build()).getAuthService();

  @Test
  @SuppressWarnings("unchecked")
  void testLogoutWithLogoutType() {
    JWTResponse response = new JWTResponse("session", "refresh", "domain", "/path", 1, 2,
        new UserResponse(), true, new IDPResponse(), 3, "external", mapOf("k", "v"), "sso-id", "trusted", 4,
        "cookie", "session-cookie", "session-domain", false, "known", true, false);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      assertThat(authService.logout(REFRESH_TOKEN, Arrays.asList("t1", "t2"))).isEqualTo(response);
    }
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(mapOf("logoutType", Arrays.asList("t1", "t2"))),
        eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/logout");
  }

  @Test
  void testLogoutWithoutLogoutTypeSendsEmptyBody() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new JWTResponse()).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThat(authService.logout(REFRESH_TOKEN, null)).isNotNull();
    }
    verify(apiProxy).post(any(URI.class), eq(Collections.emptyMap()), eq(JWTResponse.class));
  }

  @Test
  void testLogoutWithLogoutTypeMissingRefreshToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> authService.logout("", Arrays.asList("t1")));
      assertThat(thrown.getMessage()).contains("refresh token");
    }
    verifyNoInteractions(apiProxy);
  }

  @Test
  void testLogoutWithoutLogoutTypeKeepsSendingNoBody() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authService.logout(REFRESH_TOKEN);
    }
    verify(apiProxy).post(any(URI.class), eq(null), eq(JWTResponse.class));
  }

  @Test
  void testLogoutResponseParsing() throws Exception {
    String json = "{\"sessionJwt\":\"session\",\"refreshJwt\":\"refresh\",\"cookieDomain\":\"domain\","
        + "\"cookiePath\":\"/path\",\"cookieMaxAge\":1,\"cookieExpiration\":2,\"user\":{\"userId\":\"u1\"},"
        + "\"firstSeen\":true,\"idpResponse\":{\"idpGroups\":[\"g\"]},\"sessionExpiration\":3,"
        + "\"externalToken\":\"external\",\"claims\":{\"k\":\"v\"},\"tenantSSOID\":\"sso-id\","
        + "\"trustedDeviceJwt\":\"trusted\",\"nextRefreshSeconds\":4,\"cookieName\":\"cookie\","
        + "\"sessionCookieName\":\"session-cookie\",\"sessionCookieDomain\":\"session-domain\","
        + "\"unsavedSSO\":false,\"knownDeviceJwt\":\"known\",\"ssoAuthenticationOnly\":true,"
        + "\"userCreationDeferred\":false,\"unknownField\":null}";
    JWTResponse res = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    assertThat(res.getSessionJwt()).isEqualTo("session");
    assertThat(res.getRefreshJwt()).isEqualTo("refresh");
    assertThat(res.getCookieDomain()).isEqualTo("domain");
    assertThat(res.getCookiePath()).isEqualTo("/path");
    assertThat(res.getCookieMaxAge()).isEqualTo(1);
    assertThat(res.getCookieExpiration()).isEqualTo(2);
    assertThat(res.getUser().getUserId()).isEqualTo("u1");
    assertThat(res.getFirstSeen()).isTrue();
    assertThat(res.getIdpResponse().getIdpGroups()).containsExactly("g");
    assertThat(res.getSessionExpiration()).isEqualTo(3);
    assertThat(res.getExternalToken()).isEqualTo("external");
    assertThat(res.getClaims()).containsEntry("k", "v");
    assertThat(res.getTenantSSOID()).isEqualTo("sso-id");
    assertThat(res.getTrustedDeviceJwt()).isEqualTo("trusted");
    assertThat(res.getNextRefreshSeconds()).isEqualTo(4);
    assertThat(res.getCookieName()).isEqualTo("cookie");
    assertThat(res.getSessionCookieName()).isEqualTo("session-cookie");
    assertThat(res.getSessionCookieDomain()).isEqualTo("session-domain");
    assertThat(res.getUnsavedSSO()).isFalse();
    assertThat(res.getKnownDeviceJwt()).isEqualTo("known");
    assertThat(res.getSsoAuthenticationOnly()).isTrue();
    assertThat(res.getUserCreationDeferred()).isFalse();
  }
}
