package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.OAUTH_PROVIDER_GOOGLE;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.enums.OAuthPrompt;
import com.descope.exception.ClientFunctionalException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.OAuthService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class OAuthServiceImplTest {

  private OAuthService oauthService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.oauthService = AuthenticationServiceBuilder.buildServices(client).getOauthService();
  }

  @Test
  void testStart() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String start = oauthService.start("provider", "returnurl", new LoginOptions());
      Assertions.assertThat(start).isNotBlank().contains(MOCK_URL);
    }
  }

  @Test
  void testStartWithAuthParams() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL + "?q=t%40")).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Map<String, String> params = mapOf("aa", "val1@kiki");
      params.put("bb", "val2%xxx");

      String start = oauthService.start("provider", "returnurl", new LoginOptions(), params);
      Assertions.assertThat(start).isNotBlank().contains(MOCK_URL + "?q=t%40");
      Assertions.assertThat(start).isNotBlank().contains("val1%40kiki");
      Assertions.assertThat(start).isNotBlank().contains("val2%25xxx");
      Assertions.assertThat(start).isNotBlank().contains("aa=");
      Assertions.assertThat(start).isNotBlank().contains("bb=");
    }
  }

  @Test
  void testStartStepUpAndMfaSendRefreshJwtAsBearer() {
    String refreshJwt = "some-refresh-jwt";
    for (LoginOptions options : Arrays.asList(LoginOptions.builder().stepup(true).jwt(refreshJwt).build(),
        LoginOptions.builder().mfa(true).jwt(refreshJwt).build())) {
      String expected = "Bearer " + PROJECT_ID + ":" + refreshJwt;
      Assertions.assertThat(startAuthHeader(null, s -> s.start("google", null, options))).isEqualTo(expected);
      Assertions.assertThat(startAuthHeader(null, s -> s.start("google", null, options, null))).isEqualTo(expected);
      Assertions.assertThat(startAuthHeader(null, s -> s.startSignIn("google", null, options))).isEqualTo(expected);
      Assertions.assertThat(startAuthHeader(null, s -> s.startSignIn("google", null, options, null)))
          .isEqualTo(expected);
      Assertions.assertThat(startAuthHeader(null, s -> s.startSignUp("google", null, options))).isEqualTo(expected);
      Assertions.assertThat(startAuthHeader(null, s -> s.startSignUp("google", null, options, null)))
          .isEqualTo(expected);
      Assertions.assertThat(startAuthHeader("some-auth-management-key", s -> s.start("google", null, options)))
          .isEqualTo(expected + ":some-auth-management-key");
    }
  }

  @Test
  void testStartWithoutStepUpOrMfaSendsProjectIdOnly() {
    LoginOptions options = LoginOptions.builder().jwt("some-refresh-jwt").build();
    Assertions.assertThat(startAuthHeader(null, s -> s.start("google", null, options)))
        .isEqualTo("Bearer " + PROJECT_ID);
    Assertions.assertThat(startAuthHeader(null, s -> s.start("google", null, null)))
        .isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testStartStepUpWithoutRefreshJwtFails() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThatThrownBy(() -> oauthService.start("google", null, LoginOptions.builder().stepup(true).build()))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("refreshToken");
      assertThatThrownBy(() -> oauthService.startSignIn("google", null, LoginOptions.builder().mfa(true).build()))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("refreshToken");
      assertThatThrownBy(() -> oauthService.startSignUp("google", null, LoginOptions.builder().stepup(true).build()))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("refreshToken");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testStartWithPromptLoginHintAndInitiatedEmail() throws Exception {
    LoginOptions options = LoginOptions.builder().tenantId("t1").ssoAppId("app1")
        .pkceChallenge("challenge".getBytes(StandardCharsets.UTF_8)).build();
    List<OAuthPrompt> prompt = Arrays.asList(OAuthPrompt.LOGIN, OAuthPrompt.SELECT_ACCOUNT);
    Map<String, Function<OAuthService, String>> calls = mapOf(
        "/v1/auth/oauth/authorize",
        s -> s.start("google", "https://my-app.com/cb", options, null, prompt, "hint@x.com", "init@x.com"),
        "/v1/auth/oauth/authorize/signin",
        s -> s.startSignIn("google", "https://my-app.com/cb", options, null, prompt, "hint@x.com", "init@x.com"));
    calls.put("/v1/auth/oauth/authorize/signup",
        s -> s.startSignUp("google", "https://my-app.com/cb", options, null, prompt, "hint@x.com", "init@x.com"));
    for (Map.Entry<String, Function<OAuthService, String>> e : calls.entrySet()) {
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      ApiProxy apiProxy = mock(ApiProxy.class);
      doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(uri.capture(), body.capture(), any());
      try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
        mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
        Assertions.assertThat(e.getValue().apply(oauthService)).isEqualTo(MOCK_URL);
      }
      Assertions.assertThat(uri.getValue().getPath()).isEqualTo(e.getKey());
      Assertions.assertThat(uri.getValue().getRawQuery().split("&")).containsExactlyInAnyOrder(
          "provider=google", "redirectURL=https%3A%2F%2Fmy-app.com%2Fcb", "loginHint=hint%40x.com",
          "initiatedEmail=init%40x.com", "prompt=login", "prompt=select_account");
      Assertions.assertThat(toJson(body.getValue())).contains("\"tenantId\":\"t1\"", "\"ssoAppId\":\"app1\"",
          "\"pkceChallenge\":\"Y2hhbGxlbmdl\"");
      Assertions.assertThat(startAuthHeader(null, e.getValue())).isEqualTo("Bearer " + PROJECT_ID);
    }
  }

  @Test
  void testStartWithoutNewFieldsOmitsThem() throws Exception {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(uri.capture(), body.capture(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      oauthService.start("google", null, new LoginOptions(), null, null, null, null);
    }
    Assertions.assertThat(uri.getValue().getRawQuery()).isEqualTo("provider=google");
    Assertions.assertThat(toJson(body.getValue())).doesNotContain("tenantId", "ssoAppId", "pkceChallenge");
  }

  @Test
  void testStartWithNullPromptFails() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThatThrownBy(() -> oauthService.start("google", null, null, null,
          Collections.singletonList(null), null, null))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("prompt");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testUpdateUser() throws Exception {
    LoginOptions options = LoginOptions.builder().tenantId("t1").build();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(uri.capture(), body.capture(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String url = oauthService.updateUser("github", "https://my-app.com/cb", "some-refresh-jwt", options, true,
          Arrays.asList(OAuthPrompt.CONSENT), "hint@x.com", "init@x.com");
      Assertions.assertThat(url).isEqualTo(MOCK_URL);
    }
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/oauth/authorize/update");
    Assertions.assertThat(uri.getValue().getRawQuery().split("&")).containsExactlyInAnyOrder(
        "provider=github", "redirectUrl=https%3A%2F%2Fmy-app.com%2Fcb", "allowAllMerge=true",
        "loginHint=hint%40x.com", "initiatedEmail=init%40x.com", "prompt=consent");
    Assertions.assertThat(body.getValue()).isSameAs(options);
    Assertions.assertThat(toJson(body.getValue())).contains("\"tenantId\":\"t1\"");
  }

  @Test
  void testUpdateUserSendsRefreshJwtAsBearer() {
    String expected = "Bearer " + PROJECT_ID + ":some-refresh-jwt";
    Assertions.assertThat(startAuthHeader(null, s -> s.updateUser("github", null, "some-refresh-jwt", null, false)))
        .isEqualTo(expected);
    Assertions.assertThat(startAuthHeader("some-auth-management-key",
        s -> s.updateUser("github", null, "some-refresh-jwt", null, false)))
        .isEqualTo(expected + ":some-auth-management-key");
  }

  @Test
  void testUpdateUserMinimalRequest() throws Exception {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(uri.capture(), body.capture(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      oauthService.updateUser("github", null, "some-refresh-jwt", null, false);
    }
    Assertions.assertThat(uri.getValue().getRawQuery()).isEqualTo("provider=github");
    Assertions.assertThat(body.getValue()).isInstanceOf(LoginOptions.class);
  }

  @Test
  void testUpdateUserInvalidArgumentsFail() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThatThrownBy(() -> oauthService.updateUser("github", null, "", null, false))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("refreshToken");
      assertThatThrownBy(() -> oauthService.updateUser("", null, "some-refresh-jwt", null, false))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("provider");
      assertThatThrownBy(() -> oauthService.updateUser("github", null, "some-refresh-jwt", null, false,
          Collections.singletonList(null), null, null))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("prompt");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testExchangeTokenNewResponseFields() throws Exception {
    JWTResponse jwtResponse = parseJWTResponse("{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\","
        + "\"sessionExpiration\":11,\"externalToken\":\"ext\",\"claims\":{\"a\":\"b\"},"
        + "\"tenantSSOID\":\"sso1\",\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":22,"
        + "\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\",\"sessionCookieDomain\":\"x.com\","
        + "\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\",\"userCreationDeferred\":true,"
        + "\"externalPasskey\":true,\"unknownField\":1}");

    AuthenticationInfo info = exchangeTokenReturning(jwtResponse);

    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(11);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("ext");
    Assertions.assertThat(info.getClaims()).containsEntry("a", "b");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("sso1");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(22);
    Assertions.assertThat(info.getCookieName()).isEqualTo("DSR");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("DS");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("x.com");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();
    Assertions.assertThat(info.getExternalPasskey()).isTrue();
  }

  // Serialized the way the API proxy does it
  private static String toJson(Object body) throws Exception {
    return new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(body);
  }

  /** Runs the given start call and returns the Authorization header it was sent with. */
  @SuppressWarnings("unchecked")
  private static String startAuthHeader(String authManagementKey, Function<OAuthService, String> call) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    OAuthService service = AuthenticationServiceBuilder.buildServices(client).getOauthService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> captured = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      Assertions.assertThat(call.apply(service)).contains(MOCK_URL);
    }
    return captured.get();
  }

  @Test
  void testExchangeToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY))).when(apiProxy).get(any(),
        eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = oauthService.exchangeToken("somecode");
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();

    Token sessionToken = authenticationInfo.getToken();
    Assertions.assertThat(sessionToken).isNotNull();
    Assertions.assertThat(sessionToken.getJwt()).isNotBlank();
    Assertions.assertThat(sessionToken.getClaims()).isNotEmpty();
    Assertions.assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);

    Token refreshToken = authenticationInfo.getRefreshToken();
    Assertions.assertThat(refreshToken).isNotNull();
    Assertions.assertThat(refreshToken.getJwt()).isNotBlank();
    Assertions.assertThat(refreshToken.getClaims()).isNotEmpty();
    Assertions.assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);

    UserResponse user = authenticationInfo.getUser();
    Assertions.assertThat(user).isNotNull();
    Assertions.assertThat(user.getUserId()).isNotBlank();
    Assertions.assertThat(user.getLoginIds()).isNotEmpty();
  }

  @Test
  void testExchangeTokenWithoutIDPResponse() {
    JWTResponse jwtResponseNoIdp = new JWTResponse(
        "someSessionJwt", "someRefreshJwt", "", "/", 1234567, 1234567890,
        MOCK_JWT_RESPONSE.getUser(), true);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponseNoIdp).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY))).when(apiProxy).get(any(),
        eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = oauthService.exchangeToken("somecode");
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();
    Assertions.assertThat(authenticationInfo.getUser()).isNotNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse()).isNull();
  }

  @Test
  void testExchangeTokenWithIDPResponse() {
    IDPResponse idpResponse = new IDPResponse(
        Arrays.asList("users"),
        null,
        mapOf("email_verified", true, "locale", "en-US"));
    JWTResponse jwtResponseWithIdp = new JWTResponse(
        "someSessionJwt", "someRefreshJwt", "", "/", 1234567, 1234567890,
        MOCK_JWT_RESPONSE.getUser(), true, idpResponse);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponseWithIdp).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY))).when(apiProxy).get(any(),
        eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = oauthService.exchangeToken("somecode");
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse()).isNotNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse().getIdpGroups())
        .isEqualTo(Arrays.asList("users"));
    Assertions.assertThat(authenticationInfo.getIdpResponse().getIdpSAMLAttributes()).isNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse().getIdpOIDCClaims())
        .containsEntry("email_verified", true)
        .containsEntry("locale", "en-US");
  }

  // An authentication-only login: the exchange returns the user without a session instead of throwing
  @Test
  void testExchangeTokenAuthenticationOnlyWithoutSession() throws Exception {
    // The backend's JSON for such a login: empty JWTs, the user, and ssoAuthenticationOnly
    JWTResponse jwtResponse = parseJWTResponse("{\"sessionJwt\":\"\",\"refreshJwt\":\"\","
        + "\"user\":{\"userId\":\"U-auth-only\",\"loginIds\":[\"someone@example.com\"]},"
        + "\"firstSeen\":false,\"ssoAuthenticationOnly\":true}");

    AuthenticationInfo authenticationInfo = exchangeTokenReturning(jwtResponse);

    // No tokens, but the user and the flag reach the caller
    Assertions.assertThat(authenticationInfo).isNotNull();
    Assertions.assertThat(authenticationInfo.getToken()).isNull();
    Assertions.assertThat(authenticationInfo.getRefreshToken()).isNull();
    Assertions.assertThat(authenticationInfo.getUser().getUserId()).isEqualTo("U-auth-only");
    Assertions.assertThat(authenticationInfo.getSsoAuthenticationOnly()).isTrue();
  }

  // Without the authentication-only flag, a missing session is still an error, as before
  @Test
  void testExchangeTokenWithoutSessionThrows() throws Exception {
    JWTResponse jwtResponse = parseJWTResponse("{\"sessionJwt\":\"\",\"refreshJwt\":\"\"}");

    assertThrows(ClientFunctionalException.class, () -> exchangeTokenReturning(jwtResponse));
  }

  private AuthenticationInfo exchangeTokenReturning(JWTResponse jwtResponse) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        return oauthService.exchangeToken("somecode");
      }
    }
  }

  // Deserialized the way the API proxy does it
  private static JWTResponse parseJWTResponse(String json) throws Exception {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
  }

  void testExampleRequireBrowser() throws Exception {
    System.out.println(oauthService.start(OAUTH_PROVIDER_GOOGLE, "https://localhost/kuku", null));
    String encodedCode = "";
    String code = URLDecoder.decode(encodedCode, "UTF-8");
    AuthenticationInfo authInfo = oauthService.exchangeToken(code);
    UserResponse user = authInfo.getUser();
    assertNotNull(user);
  }

}
