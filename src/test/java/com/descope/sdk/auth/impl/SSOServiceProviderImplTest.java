package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.auth.SSOResponse;
import com.descope.model.auth.SSOStartOptions;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.SSOServiceProvider;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class SSOServiceProviderImplTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";

  private final SSOServiceProvider ssoService = AuthenticationServiceBuilder.buildServices(Client.builder()
      .uri("https://api.descope.com")
      .projectId(PROJECT_ID)
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
  @SuppressWarnings("unchecked")
  void testStartWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).tenantId("someTenant").ssoAppId("someApp")
        .pkceChallenge("Y2hhbGxlbmdl").build();
    SSOStartOptions options = SSOStartOptions.builder().initiatedEmail("init@example.com").build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SSOResponse("https://idp.example/login", "someErrorId")).when(apiProxy).post(any(), any(), any());
    SSOResponse res;
    ArgumentCaptor<Supplier<String>> auth = ArgumentCaptor.forClass(Supplier.class);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      res = ssoService.start("tenant-id", "https://app.example/callback", "login", loginOptions, "refresh-token",
          "connection-id", "hint@example.com", true, options);
      builder.verify(() -> ApiProxyBuilder.buildProxy(auth.capture(), any()));
    }
    assertThat(res.getUrl()).isEqualTo("https://idp.example/login");
    assertThat(res.getErrorId()).isEqualTo("someErrorId");
    assertThat(auth.getValue().get()).isEqualTo("Bearer " + PROJECT_ID + ":refresh-token");

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(loginOptions), eq(SSOResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/sso/authorize");
    assertThat(uri.getValue().getRawQuery().split("&")).containsExactlyInAnyOrder("tenant=tenant-id",
        "redirectURL=https%3A%2F%2Fapp.example%2Fcallback", "prompt=login", "ssoId=connection-id",
        "loginHint=hint%40example.com", "forceAuthn=true", "initiatedEmail=init%40example.com");
  }

  @Test
  void testStartWithEmptyOptionsSendsOnlyTenant() {
    LoginOptions loginOptions = new LoginOptions();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SSOResponse("https://idp.example/login", null)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SSOResponse res = ssoService.start("tenant-id", null, null, loginOptions, null, null, null, false,
          new SSOStartOptions());
      assertThat(res.getErrorId()).isNull();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(loginOptions), eq(SSOResponse.class));
    assertThat(uri.getValue().getRawQuery()).isEqualTo("tenant=tenant-id");
  }

  @Test
  void testStartWithOptionsForEmptyTenant() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> ssoService.start("", null, null, new LoginOptions(), null, null, null, false, null));
    assertEquals("The Tenant argument is invalid", thrown.getMessage());
  }

  @Test
  void testStartWithOptionsForMissingRefreshToken() {
    LoginOptions loginOptions = LoginOptions.builder().mfa(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> ssoService.start("tenant-id", null, null, loginOptions, null, null, null, false, null));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testExchangeTokenNewFields() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"sessionExpiration\":10,"
        + "\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"sso1\","
        + "\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":20,\"cookieName\":\"cn\","
        + "\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"kdj\",\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"unknownField\":1,\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"webauthn\":true,"
        + "\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],"
        + "\"OIDC\":true,\"consentExpiration\":30,\"recoveryEmail\":\"re@example.com\","
        + "\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,"
        + "\"modifiedTime\":40,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"k\":\"v\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\","
        + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":50}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> jwt = mockStatic(JwtUtils.class)) {
        jwt.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = ssoService.exchangeToken("someCode");
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/sso/exchange");
    assertThat(new ObjectMapper().writeValueAsString(body.getValue())).isEqualTo("{\"code\":\"someCode\"}");

    assertThat(info.getSessionExpiration()).isEqualTo(10);
    assertThat(info.getExternalToken()).isEqualTo("ext");
    assertThat(info.getClaims()).containsEntry("c", "v");
    assertThat(info.getTenantSSOID()).isEqualTo("sso1");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(20);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();

    UserResponse user = info.getUser();
    assertThat(user.getExternalIds()).containsExactly("e1");
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getScim()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("p1");
    assertThat(user.getOidc()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(30L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re@example.com");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("+15555555555");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(40L);
    assertThat(user.getRoleIds()).containsExactly("r1");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    assertThat(user.getUserFamilies()).hasSize(1);
    assertThat(user.getUserFamilies().get(0).getFamilyId()).isEqualTo("f1");
    assertThat(user.getUserFamilies().get(0).getRoleNames()).containsExactly("fr");
    assertThat(user.getUserFamilies().get(0).getPermissions()).containsExactly("fp");
    assertThat(user.getUserFamilies().get(0).getFamilyScopedAttributes()).containsEntry("k", "v");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(50L);
  }

  @Test
  void testExchangeTokenAuthenticationOnlyWithoutSession() {
    JWTResponse jwtResponse = new JWTResponse();
    jwtResponse.setSsoAuthenticationOnly(true);
    jwtResponse.setTenantSSOID("sso1");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      info = ssoService.exchangeToken("someCode");
    }
    assertThat(info.getToken()).isNull();
    assertThat(info.getRefreshToken()).isNull();
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getTenantSSOID()).isEqualTo("sso1");
  }

  @Test
  void testExchangeTokenWithoutSessionThrows() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new JWTResponse()).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThrows(DescopeException.class, () -> ssoService.exchangeToken("someCode"));
    }
  }

  @Test
  void testAuthenticationOnlyWithoutSessionThrowsOutsideExchange() {
    JWTResponse jwtResponse = new JWTResponse();
    jwtResponse.setSsoAuthenticationOnly(true);
    AuthenticationsBase base = (AuthenticationsBase) ssoService;
    assertThrows(DescopeException.class, () -> base.getAuthenticationInfo(jwtResponse));
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
