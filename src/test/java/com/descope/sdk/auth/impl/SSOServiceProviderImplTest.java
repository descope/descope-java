package com.descope.sdk.auth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.ExchangeTokenRequest;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.SSOServiceProvider;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class SSOServiceProviderImplTest {

  private final SSOServiceProvider ssoService = AuthenticationServiceBuilder.buildServices(Client.builder()
      .uri("https://api.descope.com")
      .projectId("test-project")
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build()).getSsoServiceProvider();

  @Test
  void testStartPreservesExistingOverloads() {
    LoginOptions options = new LoginOptions();

    URI uri = captureStart(() -> ssoService.start("tenant-id", "https://app.example/callback", "login", options),
        options);
    assertThat(uri.getPath()).isEqualTo("/v1/auth/sso/authorize");
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "redirectURL=https%3A%2F%2Fapp.example%2Fcallback",
        "prompt=login").doesNotContain("ssoId=", "loginHint=");

    uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token"), options);
    assertThat(uri.getRawQuery()).isEqualTo("tenant=tenant-id");
  }

  @Test
  void testStartWithSsoIdAndLoginHint() {
    LoginOptions options = new LoginOptions();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "connection/id",
        "user+name@example.com"), options);

    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "ssoId=connection%2Fid",
        "loginHint=user%2Bname%40example.com");
    assertThat(uri.getRawQuery()).doesNotContain("redirectURL=", "prompt=");
  }

  @Test
  void testStartWithRefreshTokenAndOptionalParameters() {
    LoginOptions options = LoginOptions.builder().stepup(true).build();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token",
        "connection-id", "  "), options);
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "ssoId=connection-id")
        .doesNotContain("loginHint=");

    uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token",
        "  ", "user@example.com"), options);
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "loginHint=user%40example.com")
        .doesNotContain("ssoId=");
  }

  @Test
  void testStartWithForceAuthn() {
    LoginOptions options = new LoginOptions();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, "login", options, null, null, null, true),
        options);
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "prompt=login", "forceAuthn=true");

    uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, null, null, null, false), options);
    assertThat(uri.getRawQuery()).isEqualTo("tenant=tenant-id");
  }

  @Test
  void testStartWithForceAuthnAndRefreshToken() {
    LoginOptions options = LoginOptions.builder().stepup(true).build();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, "refresh-token",
        "connection-id", null, true), options);
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "ssoId=connection-id", "forceAuthn=true");
  }

  @Test
  void testStartWithInitiatedEmail() {
    LoginOptions options = new LoginOptions();
    URI uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, null, null, null, false,
        "user+name@example.com"), options);
    assertThat(uri.getPath()).isEqualTo("/v1/auth/sso/authorize");
    assertThat(uri.getRawQuery()).contains("tenant=tenant-id", "initiatedEmail=user%2Bname%40example.com")
        .doesNotContain("forceAuthn=");

    uri = captureStart(() -> ssoService.start("tenant-id", null, null, options, null, null, null, false, "  "),
        options);
    assertThat(uri.getRawQuery()).isEqualTo("tenant=tenant-id");
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStartSendsLoginOptionsFields() throws Exception {
    LoginOptions options = LoginOptions.builder().ssoAppId("app-id")
        .pkceChallenge("challenge".getBytes(StandardCharsets.UTF_8)).tenantId("tenant-id").build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse("https://idp.example/login")).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> auth = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      assertThat(ssoService.start("tenant-id", null, null, options, null, null, null, false, "user@example.com"))
          .isEqualTo("https://idp.example/login");
    }
    assertThat(auth.get()).isEqualTo("Bearer test-project");
    ArgumentCaptor<LoginOptions> body = ArgumentCaptor.forClass(LoginOptions.class);
    verify(apiProxy).post(any(), body.capture(), eq(OAuthResponse.class));
    // Same inclusion as the SDK's request serialization
    String json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(body.getValue());
    assertThat(json).contains("\"ssoAppId\":\"app-id\"", "\"pkceChallenge\":\"Y2hhbGxlbmdl\"",
        "\"tenantId\":\"tenant-id\"");

    json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(new LoginOptions());
    assertThat(json).doesNotContain("ssoAppId", "pkceChallenge", "tenantId");
  }

  @Test
  @SuppressWarnings("unchecked")
  void testExchangeTokenReadsResponseFields() throws Exception {
    String json = "{\"sessionJwt\":\"someSessionJwt\",\"refreshJwt\":\"someRefreshJwt\",\"firstSeen\":true,"
        + "\"sessionExpiration\":1700000000,\"externalToken\":\"external-token\",\"claims\":{\"a\":\"b\"},"
        + "\"tenantSSOID\":\"sso-config-id\",\"trustedDeviceJwt\":\"trusted-jwt\",\"nextRefreshSeconds\":60,"
        + "\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\",\"sessionCookieDomain\":\"example.com\","
        + "\"unsavedSSO\":true,\"knownDeviceJwt\":\"known-jwt\",\"userCreationDeferred\":true,"
        + "\"externalPasskey\":true,\"unknownField\":1}";
    JWTResponse response = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY))).when(apiProxy).get(any(),
        eq(SigningKeysResponse.class));
    AuthenticationInfo info;
    AtomicReference<String> auth = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth.compareAndSet(null, ((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      try (MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
        jwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = ssoService.exchangeToken("somecode");
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(new ExchangeTokenRequest("somecode")), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/sso/exchange");
    assertThat(auth.get()).isEqualTo("Bearer test-project");
    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getFirstSeen()).isTrue();
    assertThat(info.getSessionExpiration()).isEqualTo(1700000000);
    assertThat(info.getExternalToken()).isEqualTo("external-token");
    assertThat(info.getClaims()).isEqualTo(Collections.singletonMap("a", "b"));
    assertThat(info.getTenantSSOID()).isEqualTo("sso-config-id");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("trusted-jwt");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(60);
    assertThat(info.getCookieName()).isEqualTo("DSR");
    assertThat(info.getSessionCookieName()).isEqualTo("DS");
    assertThat(info.getSessionCookieDomain()).isEqualTo("example.com");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("known-jwt");
    assertThat(info.getUserCreationDeferred()).isTrue();
    assertThat(info.getExternalPasskey()).isTrue();
  }

  // SSO shares the code exchange: an authentication-only login returns without a session
  @Test
  void testExchangeTokenAuthenticationOnlyWithoutSession() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    // No session or refresh JWT, flagged ssoAuthenticationOnly (last argument)
    doReturn(new JWTResponse("", "", null, null, null, null, null, false, null, true))
        .when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      info = ssoService.exchangeToken("somecode");
    }
    // It called the SSO exchange route, and returned no tokens with the flag set
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), any(), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/sso/exchange");
    assertThat(info.getToken()).isNull();
    assertThat(info.getRefreshToken()).isNull();
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
  }

  private URI captureStart(Supplier<String> call, LoginOptions options) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse("https://idp.example/login")).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThat(call.get()).isEqualTo("https://idp.example/login");
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(options), eq(OAuthResponse.class));
    return uri.getValue();
  }
}
