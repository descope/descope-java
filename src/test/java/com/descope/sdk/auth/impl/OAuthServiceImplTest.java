package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.OAUTH_PROVIDER_GOOGLE;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.OAuthPrompt;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.OAuthService;
import com.descope.utils.AuthUtils;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class OAuthServiceImplTest {

  private OAuthService oauthService;
  private Client client;

  @BeforeEach
  void setUp() {
    client = TestUtils.getClient();
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

  private interface StartCall {
    String call(OAuthService service);
  }

  private void assertStartWithOptions(String path, StartCall call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String res = call.call(oauthService);
      Assertions.assertThat(res).isEqualTo(MOCK_URL + "?aa=bb");

      @SuppressWarnings("unchecked")
      ArgumentCaptor<Supplier<String>> authCaptor = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(authCaptor.capture(), any()));
      Assertions.assertThat(authCaptor.getValue().get())
          .isEqualTo(AuthUtils.getBearerHeader(client.getProjectId(), null, client.getAuthManagementKey()));
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<LoginOptions> bodyCaptor = ArgumentCaptor.forClass(LoginOptions.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(OAuthResponse.class));
    URI uri = uriCaptor.getValue();
    Assertions.assertThat(uri.getPath()).isEqualTo(path);
    Assertions.assertThat(uri.getRawQuery()).contains("provider=google", "redirectUrl=https%3A%2F%2Fapp.com",
        "loginHint=user%40example.com", "initiatedEmail=admin%40example.com",
        "prompt=login&prompt=select_account");
    LoginOptions sent = bodyCaptor.getValue();
    Assertions.assertThat(sent.getTenantId()).isEqualTo("T1");
    Assertions.assertThat(sent.getSsoAppId()).isEqualTo("app1");
    Assertions.assertThat(sent.getPkceChallenge()).isEqualTo("Y2hhbGxlbmdl");
  }

  private static LoginOptions newOAuthLoginOptions() {
    return LoginOptions.builder().tenantId("T1").ssoAppId("app1").pkceChallenge("Y2hhbGxlbmdl").build();
  }

  @Test
  void testStartWithOptions() {
    assertStartWithOptions("/v1/auth/oauth/authorize", s -> s.start("google", "https://app.com",
        newOAuthLoginOptions(), mapOf("aa", "bb"), Arrays.asList(OAuthPrompt.LOGIN, OAuthPrompt.SELECT_ACCOUNT),
        "user@example.com", "admin@example.com"));
  }

  @Test
  void testStartSignInWithOptions() {
    assertStartWithOptions("/v1/auth/oauth/authorize/signin", s -> s.startSignIn("google", "https://app.com",
        newOAuthLoginOptions(), mapOf("aa", "bb"), Arrays.asList(OAuthPrompt.LOGIN, OAuthPrompt.SELECT_ACCOUNT),
        "user@example.com", "admin@example.com"));
  }

  @Test
  void testStartSignUpWithOptions() {
    assertStartWithOptions("/v1/auth/oauth/authorize/signup", s -> s.startSignUp("google", "https://app.com",
        newOAuthLoginOptions(), mapOf("aa", "bb"), Arrays.asList(OAuthPrompt.LOGIN, OAuthPrompt.SELECT_ACCOUNT),
        "user@example.com", "admin@example.com"));
  }

  @Test
  void testStartWithOptionsOmitsUnset() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      oauthService.start("google", null, null, null, null, null, null);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<LoginOptions> bodyCaptor = ArgumentCaptor.forClass(LoginOptions.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(OAuthResponse.class));
    Assertions.assertThat(uriCaptor.getValue().getRawQuery()).isEqualTo("provider=google");
    Assertions.assertThat(bodyCaptor.getValue()).isEqualTo(new LoginOptions());
  }

  @Test
  void testStartWithNullPrompt() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> oauthService.start("google", null, null, null, Arrays.asList((OAuthPrompt) null), null, null));
    assertEquals("The prompt argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUser() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    String res;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      res = oauthService.updateUser("google", "https://app.com", MOCK_REFRESH_TOKEN, true,
          newOAuthLoginOptions(), Arrays.asList(OAuthPrompt.CONSENT, OAuthPrompt.NONE), "user@example.com",
          "admin@example.com");

      @SuppressWarnings("unchecked")
      ArgumentCaptor<Supplier<String>> authCaptor = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(authCaptor.capture(), any()));
      Assertions.assertThat(authCaptor.getValue().get()).isEqualTo(
          AuthUtils.getBearerHeader(client.getProjectId(), MOCK_REFRESH_TOKEN, client.getAuthManagementKey()));
    }
    Assertions.assertThat(res).isEqualTo(MOCK_URL);
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<LoginOptions> bodyCaptor = ArgumentCaptor.forClass(LoginOptions.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(OAuthResponse.class));
    URI uri = uriCaptor.getValue();
    Assertions.assertThat(uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/update");
    Assertions.assertThat(uri.getRawQuery()).contains("provider=google", "redirectUrl=https%3A%2F%2Fapp.com",
        "allowAllMerge=true", "loginHint=user%40example.com", "initiatedEmail=admin%40example.com",
        "prompt=consent&prompt=none");
    Assertions.assertThat(bodyCaptor.getValue().getTenantId()).isEqualTo("T1");
  }

  @Test
  void testUpdateUserOmitsUnset() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      oauthService.updateUser("google", null, MOCK_REFRESH_TOKEN, false, null);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<LoginOptions> bodyCaptor = ArgumentCaptor.forClass(LoginOptions.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(OAuthResponse.class));
    Assertions.assertThat(uriCaptor.getValue().getRawQuery()).isEqualTo("provider=google");
    Assertions.assertThat(bodyCaptor.getValue()).isEqualTo(new LoginOptions());
  }

  @Test
  void testUpdateUserEmptyProvider() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> oauthService.updateUser("", null, MOCK_REFRESH_TOKEN, false, null));
    assertEquals("The Provider argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> oauthService.updateUser("google", null, "", false, null));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testExchangeTokenFullResponse() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"sessionExpiration\":10,"
        + "\"externalToken\":\"ext\",\"tenantSSOID\":\"sso1\",\"trustedDeviceJwt\":\"tdj\","
        + "\"nextRefreshSeconds\":20,\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\","
        + "\"sessionCookieDomain\":\"example.com\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\","
        + "\"samlGeneratedRoles\":\"sgr\",\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\","
        + "\"oidcGeneratedRoles\":\"ogr\"},"
        + "\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"OAuth\":{\"google\":true},"
        + "\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,"
        + "\"permissions\":[\"p1\"],\"OIDC\":true,\"consentExpiration\":30,"
        + "\"recoveryEmail\":\"r@example.com\",\"verifiedRecoveryEmail\":true,"
        + "\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":40,"
        + "\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"rn\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\","
        + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":50}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = oauthService.exchangeToken("somecode");
      }
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uriCaptor.capture(), any(), eq(JWTResponse.class));
    Assertions.assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/oauth/exchange");

    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(10);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("ext");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("sso1");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(20);
    Assertions.assertThat(info.getCookieName()).isEqualTo("DSR");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("DS");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("example.com");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    Assertions.assertThat(info.getSsoAuthenticationOnly()).isTrue();
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();

    IDPResponse idp = info.getIdpResponse();
    Assertions.assertThat(idp.getSamlResponse()).isEqualTo("sr");
    Assertions.assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    Assertions.assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    Assertions.assertThat(idp.getOidcResponse()).isEqualTo("or");
    Assertions.assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    Assertions.assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = info.getUser();
    Assertions.assertThat(user.getExternalIds()).containsExactly("e1");
    Assertions.assertThat(user.getOAuth()).containsEntry("google", true);
    Assertions.assertThat(user.getWebauthn()).isTrue();
    Assertions.assertThat(user.getPassword()).isTrue();
    Assertions.assertThat(user.getEditable()).isTrue();
    Assertions.assertThat(user.getScim()).isTrue();
    Assertions.assertThat(user.getPush()).isTrue();
    Assertions.assertThat(user.getPermissions()).containsExactly("p1");
    Assertions.assertThat(user.getOidc()).isTrue();
    Assertions.assertThat(user.getConsentExpiration()).isEqualTo(30L);
    Assertions.assertThat(user.getRecoveryEmail()).isEqualTo("r@example.com");
    Assertions.assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    Assertions.assertThat(user.getRecoveryPhone()).isEqualTo("+15555555555");
    Assertions.assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    Assertions.assertThat(user.getModifiedTime()).isEqualTo(40L);
    Assertions.assertThat(user.getRoleIds()).containsExactly("r1");
    Assertions.assertThat(user.getRecoveryCodes()).isTrue();
    Assertions.assertThat(user.getDependent()).isTrue();
    Assertions.assertThat(user.getUserFamilies()).hasSize(1);
    Assertions.assertThat(user.getUserFamilies().get(0).getFamilyId()).isEqualTo("f1");
    Assertions.assertThat(user.getUserFamilies().get(0).getRoleNames()).containsExactly("rn");
    Assertions.assertThat(user.getUserFamilies().get(0).getPermissions()).containsExactly("fp");
    Assertions.assertThat(user.getUserFamilies().get(0).getFamilyScopedAttributes()).containsEntry("a", "b");
    Assertions.assertThat(user.getScimTenantId()).isEqualTo("st");
    Assertions.assertThat(user.getSamlTenantId()).isEqualTo("sat");
    Assertions.assertThat(user.getOidcTenantId()).isEqualTo("ot");
    Assertions.assertThat(user.getConsentTenantId()).isEqualTo("ct");
    Assertions.assertThat(user.getLastLoginTime()).isEqualTo(50L);
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
