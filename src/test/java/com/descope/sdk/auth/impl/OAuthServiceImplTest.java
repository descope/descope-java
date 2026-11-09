package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.OAUTH_PROVIDER_GOOGLE;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
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

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.auth.OAuthStartOptions;
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

  private static final class Captured {
    private URI uri;
    private Object body;
    private String authorization;
  }

  @SuppressWarnings("unchecked")
  private static Captured captureStart(Supplier<OAuthResponse> call, OAuthResponse response) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authorization = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authorization.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      OAuthResponse res = call.get();
      Assertions.assertThat(res.getUrl()).startsWith(response.getUrl());
      Assertions.assertThat(res.getErrorId()).isEqualTo(response.getErrorId());
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(OAuthResponse.class));
    Captured captured = new Captured();
    captured.uri = uri.getValue();
    captured.body = body.getValue();
    captured.authorization = authorization.get();
    return captured;
  }

  private static OAuthStartOptions fullOptions(Boolean rawResponse) {
    return OAuthStartOptions.builder()
        .prompt(Arrays.asList("login", "consent"))
        .rawResponse(rawResponse)
        .loginHint("user@example.com")
        .initiatedEmail("init@example.com")
        .authParams(mapOf("aa", "v@1"))
        .build();
  }

  private static LoginOptions fullLoginOptions() {
    return LoginOptions.builder().mfa(true).locale("en").ssoAppId("app1").tenantId("t1").build();
  }

  @Test
  void testStartWithOptions() {
    Captured c = captureStart(() -> oauthService.startWithOptions("google", "https://my.app/cb", fullLoginOptions(),
        fullOptions(true)), new OAuthResponse(MOCK_URL, "err1"));
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize");
    Assertions.assertThat(c.uri.getRawQuery()).isEqualTo("provider=google&redirectUrl=https%3A%2F%2Fmy.app%2Fcb"
        + "&prompt=login&prompt=consent&rawResponse=true&loginHint=user%40example.com"
        + "&initiatedEmail=init%40example.com");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = (Map<String, Object>) c.body;
    Assertions.assertThat(body).containsEntry("mfa", true).containsEntry("stepup", false)
        .containsEntry("locale", "en").containsEntry("ssoAppId", "app1").containsEntry("tenantId", "t1")
        .doesNotContainKey("jwtrequired").doesNotContainKey("jwt");
  }

  @Test
  void testStartWithOptionsAppendsAuthParams() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new OAuthResponse(MOCK_URL + "?q=1", null)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      OAuthResponse res = oauthService.startWithOptions("google", null, null, fullOptions(null));
      Assertions.assertThat(res.getUrl()).isEqualTo(MOCK_URL + "?q=1&aa=v%401");
      Assertions.assertThat(res.getErrorId()).isNull();
    }
  }

  @Test
  void testStartWithOptionsMinimal() {
    Captured c = captureStart(() -> oauthService.startWithOptions("google", null, null, null),
        new OAuthResponse(MOCK_URL, null));
    Assertions.assertThat(c.uri.getRawQuery()).isEqualTo("provider=google");
    Assertions.assertThat((Map<String, Object>) c.body).isEmpty();
  }

  @Test
  void testStartSignInWithOptions() {
    Captured c = captureStart(() -> oauthService.startSignInWithOptions("google", null, fullLoginOptions(),
        OAuthStartOptions.builder().loginHint("hint").build()), new OAuthResponse(MOCK_URL, null));
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/signin");
    Assertions.assertThat(c.uri.getRawQuery()).isEqualTo("provider=google&loginHint=hint");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testStartSignUpWithOptions() {
    Captured c = captureStart(() -> oauthService.startSignUpWithOptions("google", null, fullLoginOptions(),
        OAuthStartOptions.builder().prompt(Arrays.asList("none")).build()), new OAuthResponse(MOCK_URL, null));
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/signup");
    Assertions.assertThat(c.uri.getRawQuery()).isEqualTo("provider=google&prompt=none");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testUpdateUser() {
    Captured c = captureStart(() -> oauthService.updateUser("google", "https://my.app/cb", "refreshJwt", true,
        fullLoginOptions(), fullOptions(null)), new OAuthResponse(MOCK_URL, "err1"));
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/oauth/authorize/update");
    Assertions.assertThat(c.uri.getRawQuery()).isEqualTo("provider=google&redirectUrl=https%3A%2F%2Fmy.app%2Fcb"
        + "&allowAllMerge=true&prompt=login&prompt=consent&loginHint=user%40example.com"
        + "&initiatedEmail=init%40example.com");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID + ":refreshJwt");
    Assertions.assertThat((Map<String, Object>) c.body).containsEntry("tenantId", "t1");
  }

  @Test
  void testUpdateUserInvalidArguments() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThrows(ServerCommonException.class,
          () -> oauthService.updateUser("google", null, " ", null, null, null));
      assertThrows(ServerCommonException.class,
          () -> oauthService.updateUser("google", null, "refreshJwt", null, null,
              OAuthStartOptions.builder().rawResponse(false).build()));
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testExchangeTokenFullResponse() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"sessionExpiration\":10,\"externalToken\":\"et\","
        + "\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"ts\",\"trustedDeviceJwt\":\"td\",\"nextRefreshSeconds\":20,"
        + "\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"kd\",\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,\"unknown\":1,"
        + "\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,\"editable\":true,"
        + "\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,\"consentExpiration\":\"30\","
        + "\"recoveryEmail\":\"re@example.com\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+1555\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":40,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,"
        + "\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"rn\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sa\","
        + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":50,\"idpResponse\":null}}";
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
    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(10);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("et");
    Assertions.assertThat(info.getClaims()).containsEntry("c", "v");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("ts");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("td");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(20);
    Assertions.assertThat(info.getCookieName()).isEqualTo("cn");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("scn");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("kd");
    Assertions.assertThat(info.getSsoAuthenticationOnly()).isTrue();
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();

    UserResponse user = info.getUser();
    Assertions.assertThat(user.getExternalIds()).containsExactly("e1");
    Assertions.assertThat(user.getWebauthn()).isTrue();
    Assertions.assertThat(user.getPassword()).isTrue();
    Assertions.assertThat(user.getEditable()).isTrue();
    Assertions.assertThat(user.getSCIM()).isTrue();
    Assertions.assertThat(user.getPush()).isTrue();
    Assertions.assertThat(user.getPermissions()).containsExactly("p1");
    Assertions.assertThat(user.getOIDC()).isTrue();
    Assertions.assertThat(user.getConsentExpiration()).isEqualTo(30L);
    Assertions.assertThat(user.getRecoveryEmail()).isEqualTo("re@example.com");
    Assertions.assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    Assertions.assertThat(user.getRecoveryPhone()).isEqualTo("+1555");
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
    Assertions.assertThat(user.getSamlTenantId()).isEqualTo("sa");
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
