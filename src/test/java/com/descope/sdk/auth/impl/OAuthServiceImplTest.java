package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.OAUTH_PROVIDER_GOOGLE;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.enums.OAuthPrompt;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.OAuthAuthorizeOptions;
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
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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

  private static final String REFRESH_TOKEN = "some-refresh-token";
  private static final OAuthAuthorizeOptions AUTHORIZE_OPTIONS = OAuthAuthorizeOptions.builder()
      .prompt(Arrays.asList(OAuthPrompt.LOGIN, OAuthPrompt.SELECT_ACCOUNT))
      .loginHint("user+1@example.com")
      .initiatedEmail("user@example.com")
      .build();

  @Test
  void testStartWithAuthorizeOptions() throws Exception {
    LoginOptions loginOptions = LoginOptions.builder().ssoAppId("app-id").pkceChallenge("Y2hhbGxlbmdl")
        .tenantId("tenant-id").build();
    Captured c = capture(() -> oauthService.start("google", "https://app.example/cb", loginOptions, null,
        AUTHORIZE_OPTIONS));
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize");
    assertThat(c.uri.getRawQuery().split("&")).containsExactlyInAnyOrder("provider=google",
        "redirectUrl=https%3A%2F%2Fapp.example%2Fcb", "loginHint=user%2B1%40example.com",
        "initiatedEmail=user%40example.com", "prompt=login", "prompt=select_account");
    assertThat(c.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(c.body).isEqualTo("{\"stepup\":false,\"mfa\":false,\"revokeOtherSessions\":false,"
        + "\"ssoAppId\":\"app-id\",\"pkceChallenge\":\"Y2hhbGxlbmdl\",\"tenantId\":\"tenant-id\"}");
    assertThat(c.result).isEqualTo(MOCK_URL);
  }

  @Test
  void testStartWithAuthorizeOptionsStepUp() throws Exception {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).jwt(REFRESH_TOKEN).build();
    Captured c = capture(() -> oauthService.start("google", null, loginOptions, null, AUTHORIZE_OPTIONS));
    assertThat(c.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN);
    assertThat(c.body).isEqualTo("{\"stepup\":true,\"mfa\":false,\"revokeOtherSessions\":false}");
  }

  @Test
  void testStartWithAuthorizeOptionsStepUpNoJwt() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      LoginOptions loginOptions = LoginOptions.builder().mfa(true).build();
      assertThatThrownBy(() -> oauthService.startSignIn("google", null, loginOptions, null, null))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("JWT");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testStartSignInAndSignUpWithAuthorizeOptions() throws Exception {
    Captured c = capture(() -> oauthService.startSignIn("google", null, null, mapOf("aa", "b"), AUTHORIZE_OPTIONS));
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/signin");
    assertThat(c.uri.getRawQuery().split("&")).containsExactlyInAnyOrder("provider=google",
        "loginHint=user%2B1%40example.com", "initiatedEmail=user%40example.com", "prompt=login",
        "prompt=select_account");
    assertThat(c.body).isEqualTo("{\"stepup\":false,\"mfa\":false,\"revokeOtherSessions\":false}");
    assertThat(c.result).isEqualTo(MOCK_URL + "?aa=b");

    c = capture(() -> oauthService.startSignUp("google", null, new LoginOptions(), null,
        new OAuthAuthorizeOptions()));
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/signup");
    assertThat(c.uri.getRawQuery()).isEqualTo("provider=google");
    assertThat(c.auth).isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testUpdateUser() throws Exception {
    LoginOptions loginOptions = LoginOptions.builder().tenantId("tenant-id").build();
    Captured c = capture(() -> oauthService.updateUser("google", "https://app.example/cb", REFRESH_TOKEN, true,
        loginOptions, AUTHORIZE_OPTIONS));
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/update");
    assertThat(c.uri.getRawQuery().split("&")).containsExactlyInAnyOrder("provider=google",
        "redirectUrl=https%3A%2F%2Fapp.example%2Fcb", "allowAllMerge=true", "loginHint=user%2B1%40example.com",
        "initiatedEmail=user%40example.com", "prompt=login", "prompt=select_account");
    assertThat(c.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN);
    assertThat(c.body).isEqualTo("{\"stepup\":false,\"mfa\":false,\"revokeOtherSessions\":false,"
        + "\"tenantId\":\"tenant-id\"}");
    assertThat(c.result).isEqualTo(MOCK_URL);

    c = capture(() -> oauthService.updateUser("google", null, REFRESH_TOKEN, false, null));
    assertThat(c.uri.getRawQuery()).isEqualTo("provider=google");
    assertThat(c.body).isEqualTo("{\"stepup\":false,\"mfa\":false,\"revokeOtherSessions\":false}");
  }

  @Test
  void testUpdateUserNoRefreshToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThatThrownBy(() -> oauthService.updateUser("google", null, " ", false, null))
          .isInstanceOf(ServerCommonException.class).hasMessageContaining("Refresh Token");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testExchangeTokenFullResponse() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"d\",\"cookiePath\":\"/\","
        + "\"cookieMaxAge\":1,\"cookieExpiration\":2,\"firstSeen\":true,\"sessionExpiration\":3,"
        + "\"externalToken\":\"et\",\"claims\":{\"a\":\"b\"},\"tenantSSOID\":\"tsso\",\"trustedDeviceJwt\":\"tdj\","
        + "\"nextRefreshSeconds\":4,\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\","
        + "\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,\"unknownField\":1,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\"},"
        + "\"user\":{\"userId\":\"u1\",\"userTenants\":[{\"tenantId\":\"t1\",\"permissions\":[\"p1\"],"
        + "\"roleIds\":[\"ri1\"]}],\"externalIds\":[\"e1\"],\"TOTP\":true,\"SAML\":true,"
        + "\"OAuth\":{\"google\":true},\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,"
        + "\"push\":true,\"permissions\":[\"p2\"],\"OIDC\":true,\"consentExpiration\":5,"
        + "\"recoveryEmail\":\"re\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"rp\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":\"6\",\"roleIds\":[\"ri2\"],\"recoveryCodes\":true,"
        + "\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"k\":\"v\"}}],\"scimTenantId\":\"st\","
        + "\"samlTenantId\":\"sat\",\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":7}}";
    JWTResponse jwtResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> auth = new AtomicReference<>();
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = oauthService.exchangeToken("somecode");
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/oauth/exchange");
    assertThat(new ObjectMapper().writeValueAsString(body.getValue())).isEqualTo("{\"code\":\"somecode\"}");
    assertThat(auth.get()).isEqualTo("Bearer " + PROJECT_ID);

    assertThat(info.getCookieDomain()).isEqualTo("d");
    assertThat(info.getCookiePath()).isEqualTo("/");
    assertThat(info.getCookieMaxAge()).isEqualTo(1);
    assertThat(info.getCookieExpiration()).isEqualTo(2);
    assertThat(info.getSessionExpiration()).isEqualTo(3);
    assertThat(info.getExternalToken()).isEqualTo("et");
    assertThat(info.getClaims()).containsEntry("a", "b");
    assertThat(info.getTenantSSOID()).isEqualTo("tsso");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(4);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();

    IDPResponse idp = info.getIdpResponse();
    assertThat(idp.getSamlResponse()).isEqualTo("sr");
    assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(idp.getOidcResponse()).isEqualTo("or");
    assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = info.getUser();
    AssociatedTenant tenant = user.getUserTenants().get(0);
    assertThat(tenant.getPermissions()).containsExactly("p1");
    assertThat(tenant.getRoleIds()).containsExactly("ri1");
    assertThat(user.getExternalIds()).containsExactly("e1");
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getSCIM()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("p2");
    assertThat(user.getOIDC()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(5L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("rp");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(6L);
    assertThat(user.getRoleIds()).containsExactly("ri2");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    assertThat(user.getUserFamilies().get(0).getFamilyId()).isEqualTo("f1");
    assertThat(user.getUserFamilies().get(0).getRoleNames()).containsExactly("fr");
    assertThat(user.getUserFamilies().get(0).getPermissions()).containsExactly("fp");
    assertThat(user.getUserFamilies().get(0).getFamilyScopedAttributes()).containsEntry("k", "v");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(7L);
  }

  @Test
  void testResponseOnlyTenantFieldsAreNotSerialized() throws Exception {
    AssociatedTenant tenant = new AssociatedTenant("t1", null, Arrays.asList("r1"));
    tenant.setPermissions(Arrays.asList("p1"));
    tenant.setRoleIds(Arrays.asList("ri1"));
    assertThat(new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(tenant))
        .isEqualTo("{\"tenantId\":\"t1\",\"roleNames\":[\"r1\"]}");
  }

  private static class Captured {
    URI uri;
    String body;
    String auth;
    String result;
  }

  @SuppressWarnings("unchecked")
  private Captured capture(Supplier<String> call) throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    Captured c = new Captured();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        c.auth = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      c.result = call.get();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(OAuthResponse.class));
    c.uri = uri.getValue();
    c.body = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(body.getValue());
    return c;
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
