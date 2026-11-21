package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.OAUTH_PROVIDER_GOOGLE;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
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
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.ExchangeTokenRequest;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

  private static final String TEST_PROJECT_ID = "P123456789012345678901234567";
  private static final String REFRESH_TOKEN = "some-refresh-token";

  private final OAuthService testOAuthService = AuthenticationServiceBuilder.buildServices(Client.builder()
      .uri("https://api.descope.com")
      .projectId(TEST_PROJECT_ID)
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build()).getOauthService();

  @Test
  void testStartWithAllOptions() throws Exception {
    LoginOptions options = LoginOptions.builder().tenantId("tenant-id").ssoAppId("sso-app-id")
        .pkceChallenge("challenge".getBytes(StandardCharsets.UTF_8)).jwt("not-sent").build();
    Captured c = capture(() -> testOAuthService.start("google", "https://app.example/cb",
        Arrays.asList(OAuthPrompt.CONSENT, OAuthPrompt.SELECT_ACCOUNT), "user@example.com", "init@example.com",
        options, null, mapOf("aa", "b@c")));
    assertThat(c.result).isEqualTo("https://idp.example/login?aa=b%40c");
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize");
    assertThat(c.uri.getRawQuery()).contains("provider=google", "redirectUrl=https%3A%2F%2Fapp.example%2Fcb",
        "loginHint=user%40example.com", "initiatedEmail=init%40example.com", "prompt=consent&prompt=select_account")
        .doesNotContain("allowAllMerge", "redirectURL");
    assertThat(c.auth).isEqualTo("Bearer " + TEST_PROJECT_ID);
    JsonNode json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).valueToTree(c.body);
    assertThat(json.has("jwt")).isFalse();
    assertThat(json.has("jwtrequired")).isFalse();
    assertThat(json.get("stepup").asBoolean()).isFalse();
    assertThat(json.get("tenantId").asText()).isEqualTo("tenant-id");
    assertThat(json.get("ssoAppId").asText()).isEqualTo("sso-app-id");
    assertThat(json.get("pkceChallenge").asText()).isEqualTo("Y2hhbGxlbmdl");
  }

  @Test
  void testStartSignInWithStepUp() {
    LoginOptions options = LoginOptions.builder().stepup(true).build();
    Captured c = capture(() -> testOAuthService.startSignIn("google", null, null, null, null, options,
        REFRESH_TOKEN, null));
    assertThat(c.result).isEqualTo("https://idp.example/login");
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/signin");
    assertThat(c.uri.getRawQuery()).isEqualTo("provider=google");
    assertThat(c.auth).isEqualTo("Bearer " + TEST_PROJECT_ID + ":" + REFRESH_TOKEN);
    assertThat(c.body).isEqualTo(mapOf("stepup", true, "mfa", false, "revokeOtherSessions", false));
  }

  @Test
  void testStartSignUpWithoutLoginOptions() {
    Captured c = capture(() -> testOAuthService.startSignUp("google", null, null, "hint", null, null, null, null));
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/signup");
    assertThat(c.uri.getRawQuery()).contains("provider=google", "loginHint=hint").doesNotContain("prompt");
    assertThat(c.auth).isEqualTo("Bearer " + TEST_PROJECT_ID);
    assertThat(c.body).isEqualTo(mapOf("stepup", false, "mfa", false, "revokeOtherSessions", false));
  }

  @Test
  void testStartWithStepUpAndNoRefreshToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    LoginOptions options = LoginOptions.builder().mfa(true).build();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException e = assertThrows(ServerCommonException.class,
          () -> testOAuthService.start("google", null, null, null, null, options, null, null));
      assertThat(e.getMessage()).contains("refreshToken");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testStartWithoutProvider() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException e = assertThrows(ServerCommonException.class,
          () -> testOAuthService.startSignIn("", null, null, null, null, null, null, null));
      assertThat(e.getMessage()).contains("Provider");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testStartUpdateUser() {
    LoginOptions options = LoginOptions.builder().tenantId("tenant-id").build();
    Captured c = capture(() -> testOAuthService.startUpdateUser("github", "https://app.example/cb", true,
        Arrays.asList(OAuthPrompt.LOGIN, OAuthPrompt.NONE), "hint", "init@example.com", options, REFRESH_TOKEN,
        null));
    assertThat(c.result).isEqualTo("https://idp.example/login");
    assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/update");
    assertThat(c.uri.getRawQuery()).contains("provider=github", "redirectUrl=https%3A%2F%2Fapp.example%2Fcb",
        "allowAllMerge=true", "loginHint=hint", "initiatedEmail=init%40example.com", "prompt=login&prompt=none");
    assertThat(c.auth).isEqualTo("Bearer " + TEST_PROJECT_ID + ":" + REFRESH_TOKEN);
    assertThat(c.body).isEqualTo(mapOf("stepup", false, "mfa", false, "revokeOtherSessions", false,
        "tenantId", "tenant-id"));

    c = capture(() -> testOAuthService.startUpdateUser("github", null, false, null, REFRESH_TOKEN));
    assertThat(c.uri.getRawQuery()).isEqualTo("provider=github");
    assertThat(c.body).isEqualTo(mapOf("stepup", false, "mfa", false, "revokeOtherSessions", false));
  }

  @Test
  void testStartUpdateUserWithoutRefreshToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException e = assertThrows(ServerCommonException.class,
          () -> testOAuthService.startUpdateUser("github", null, false, null, " "));
      assertThat(e.getMessage()).contains("refreshToken");
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testExchangeTokenFullResponse() throws Exception {
    String body = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"example.com\",\"cookiePath\":\"/\","
        + "\"cookieMaxAge\":100,\"cookieExpiration\":200,\"firstSeen\":true,\"sessionExpiration\":300,"
        + "\"externalToken\":\"ext\",\"claims\":{\"k\":\"v\"},\"tenantSSOID\":\"sso-id\",\"trustedDeviceJwt\":\"tdj\","
        + "\"nextRefreshSeconds\":400,\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\","
        + "\"sessionCookieDomain\":\"s.example.com\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,\"unknownField\":1,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\","
        + "\"idpGroups\":[\"g\"]},"
        + "\"user\":{\"userId\":\"u1\",\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true},\"externalIds\":[\"e\"],"
        + "\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p\"],"
        + "\"OIDC\":true,\"consentExpiration\":10,\"recoveryEmail\":\"r@example.com\",\"verifiedRecoveryEmail\":true,"
        + "\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":20,\"roleIds\":[\"ri\"],"
        + "\"recoveryCodes\":true,\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f\",\"roleNames\":[\"fr\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\","
        + "\"samlTenantId\":\"sat\",\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":30,"
        + "\"userTenants\":[{\"tenantId\":\"t\",\"permissions\":[\"tp\"],\"roleIds\":[\"tr\"]}]}}";
    JWTResponse jwtResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(body, JWTResponse.class);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> auth = new AtomicReference<>();
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = testOAuthService.exchangeToken("somecode");
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> req = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), req.capture(), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/oauth/exchange");
    assertThat(((ExchangeTokenRequest) req.getValue()).getCode()).isEqualTo("somecode");
    assertThat(auth.get()).isEqualTo("Bearer " + TEST_PROJECT_ID);

    assertThat(info.getFirstSeen()).isTrue();
    assertThat(info.getCookieDomain()).isEqualTo("example.com");
    assertThat(info.getCookiePath()).isEqualTo("/");
    assertThat(info.getCookieMaxAge()).isEqualTo(100);
    assertThat(info.getCookieExpiration()).isEqualTo(200);
    assertThat(info.getSessionExpiration()).isEqualTo(300);
    assertThat(info.getExternalToken()).isEqualTo("ext");
    assertThat(info.getClaims()).containsEntry("k", "v");
    assertThat(info.getTenantSSOID()).isEqualTo("sso-id");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(400);
    assertThat(info.getCookieName()).isEqualTo("DSR");
    assertThat(info.getSessionCookieName()).isEqualTo("DS");
    assertThat(info.getSessionCookieDomain()).isEqualTo("s.example.com");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();

    IDPResponse idp = info.getIdpResponse();
    assertThat(idp.getIdpGroups()).containsExactly("g");
    assertThat(idp.getSamlResponse()).isEqualTo("sr");
    assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(idp.getOidcResponse()).isEqualTo("or");
    assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = info.getUser();
    assertThat(user.getUserId()).isEqualTo("u1");
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getExternalIds()).containsExactly("e");
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getSCIM()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("p");
    assertThat(user.getOIDC()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(10L);
    assertThat(user.getRecoveryEmail()).isEqualTo("r@example.com");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("+15555555555");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(20L);
    assertThat(user.getRoleIds()).containsExactly("ri");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    assertThat(user.getUserFamilies()).hasSize(1);
    assertThat(user.getUserFamilies().get(0).getFamilyId()).isEqualTo("f");
    assertThat(user.getUserFamilies().get(0).getRoleNames()).containsExactly("fr");
    assertThat(user.getUserFamilies().get(0).getPermissions()).containsExactly("fp");
    assertThat(user.getUserFamilies().get(0).getFamilyScopedAttributes()).containsEntry("a", "b");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(30L);
    assertThat(user.getUserTenants().get(0).getPermissions()).containsExactly("tp");
    assertThat(user.getUserTenants().get(0).getRoleIds()).containsExactly("tr");
  }

  @Test
  void testUserResponseKeepsDerivedNames() throws Exception {
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    UserResponse user = mapper.readValue("{\"totp\":true,\"saml\":true,\"oauth\":{\"google\":true}}",
        UserResponse.class);
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    JsonNode json = mapper.valueToTree(user);
    assertThat(json.has("totp")).isTrue();
    assertThat(json.has("TOTP")).isFalse();
  }

  private static class Captured {
    String result;
    URI uri;
    Object body;
    String auth;
  }

  @SuppressWarnings("unchecked")
  private Captured capture(Supplier<String> call) {
    Captured c = new Captured();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse("https://idp.example/login")).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        c.auth = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      c.result = call.get();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(OAuthResponse.class));
    c.uri = uri.getValue();
    c.body = body.getValue();
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
