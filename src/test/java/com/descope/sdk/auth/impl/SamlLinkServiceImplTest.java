package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.SAMLResponse;
import com.descope.model.auth.SAMLStartOptions;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.SAMLService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class SamlLinkServiceImplTest {

  private SAMLService samlService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.samlService = AuthenticationServiceBuilder.buildServices(client).getSamlService();
  }

  @Test
  void testStart() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SAMLResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String start = samlService.start("tenant", "returnurl", new LoginOptions());
      Assertions.assertThat(start).isNotBlank().contains(MOCK_URL);
    }
  }

  @Test
  void testExchangeToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = samlService.exchangeToken("somecode");
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
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = samlService.exchangeToken("somecode");
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();
    Assertions.assertThat(authenticationInfo.getUser()).isNotNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse()).isNull();
  }

  @Test
  void testExchangeTokenWithIDPResponse() {
    IDPResponse idpResponse = new IDPResponse(
        Arrays.asList("engineering", "devops"),
        mapOf("department", "engineering", "title", "Staff Engineer"),
        null);
    JWTResponse jwtResponseWithIdp = new JWTResponse(
        "someSessionJwt", "someRefreshJwt", "", "/", 1234567, 1234567890,
        MOCK_JWT_RESPONSE.getUser(), true, idpResponse);

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponseWithIdp).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = samlService.exchangeToken("somecode");
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse()).isNotNull();
    Assertions.assertThat(authenticationInfo.getIdpResponse().getIdpGroups())
        .isEqualTo(Arrays.asList("engineering", "devops"));
    Assertions.assertThat(authenticationInfo.getIdpResponse().getIdpSAMLAttributes())
        .containsEntry("department", "engineering")
        .containsEntry("title", "Staff Engineer");
    Assertions.assertThat(authenticationInfo.getIdpResponse().getIdpOIDCClaims()).isNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStartWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SAMLResponse(MOCK_URL, "someErrorId")).when(apiProxy).post(any(), any(), any());
    LoginOptions loginOptions = LoginOptions.builder().tenantId("someTenant").ssoAppId("someApp")
        .pkceChallenge("Y2hhbGxlbmdl").build();
    SAMLStartOptions options = SAMLStartOptions.builder().prompt(Arrays.asList("login", "select account"))
        .forceAuthn(true).loginHint("hint@descope.com").initiatedEmail("init@descope.com").build();
    SAMLResponse res;
    ArgumentCaptor<Supplier<String>> auth = ArgumentCaptor.forClass(Supplier.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      res = samlService.start("tenant", "returnurl", loginOptions, options);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(auth.capture(), any()));
    }
    Assertions.assertThat(res.getUrl()).isEqualTo(MOCK_URL);
    Assertions.assertThat(res.getErrorId()).isEqualTo("someErrorId");
    Assertions.assertThat(auth.getValue().get()).isEqualTo("Bearer " + PROJECT_ID);

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(loginOptions), eq(SAMLResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/saml/authorize");
    Assertions.assertThat(uri.getValue().getRawQuery().split("&")).containsExactlyInAnyOrder(
        "tenant=tenant", "redirectURL=returnurl", "forceAuthn=true", "loginHint=hint%40descope.com",
        "initiatedEmail=init%40descope.com", "prompt=login", "prompt=select+account");
  }

  @Test
  void testStartWithoutOptionsSendsOnlyTenantAndRedirect() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SAMLResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SAMLResponse res = samlService.start("tenant", null, null, new SAMLStartOptions());
      Assertions.assertThat(res.getUrl()).isEqualTo(MOCK_URL);
      Assertions.assertThat(res.getErrorId()).isNull();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), any(), eq(SAMLResponse.class));
    Assertions.assertThat(uri.getValue().getRawQuery()).isEqualTo("tenant=tenant");
  }

  @Test
  void testLoginOptionsNewFieldsSerialization() throws Exception {
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(
        com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    Assertions.assertThat(mapper.writeValueAsString(new LoginOptions())).doesNotContain("tenantId", "ssoAppId",
        "pkceChallenge");
    String json = mapper.writeValueAsString(LoginOptions.builder().tenantId("t1").ssoAppId("a1")
        .pkceChallenge("Y2g=").build());
    Assertions.assertThat(json).contains("\"tenantId\":\"t1\"", "\"ssoAppId\":\"a1\"",
        "\"pkceChallenge\":\"Y2g=\"");
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
        + "\"OIDC\":true,\"consentExpiration\":30,\"recoveryEmail\":\"re@descope.com\","
        + "\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,"
        + "\"modifiedTime\":40,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"k\":\"v\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\","
        + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":50,\"tempLockExpiration\":null}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = samlService.exchangeToken("somecode");
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), any(), eq(JWTResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/saml/exchange");

    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(10);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("ext");
    Assertions.assertThat(info.getClaims()).containsEntry("c", "v");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("sso1");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(20);
    Assertions.assertThat(info.getCookieName()).isEqualTo("cn");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("scn");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    Assertions.assertThat(info.getSsoAuthenticationOnly()).isTrue();
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();

    UserResponse user = info.getUser();
    Assertions.assertThat(user.getExternalIds()).containsExactly("e1");
    Assertions.assertThat(user.getWebauthn()).isTrue();
    Assertions.assertThat(user.getPassword()).isTrue();
    Assertions.assertThat(user.getEditable()).isTrue();
    Assertions.assertThat(user.getScim()).isTrue();
    Assertions.assertThat(user.getPush()).isTrue();
    Assertions.assertThat(user.getPermissions()).containsExactly("p1");
    Assertions.assertThat(user.getOidc()).isTrue();
    Assertions.assertThat(user.getConsentExpiration()).isEqualTo(30L);
    Assertions.assertThat(user.getRecoveryEmail()).isEqualTo("re@descope.com");
    Assertions.assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    Assertions.assertThat(user.getRecoveryPhone()).isEqualTo("+15555555555");
    Assertions.assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    Assertions.assertThat(user.getModifiedTime()).isEqualTo(40L);
    Assertions.assertThat(user.getRoleIds()).containsExactly("r1");
    Assertions.assertThat(user.getRecoveryCodes()).isTrue();
    Assertions.assertThat(user.getDependent()).isTrue();
    Assertions.assertThat(user.getUserFamilies()).hasSize(1);
    Assertions.assertThat(user.getUserFamilies().get(0).getFamilyId()).isEqualTo("f1");
    Assertions.assertThat(user.getUserFamilies().get(0).getRoleNames()).containsExactly("fr");
    Assertions.assertThat(user.getUserFamilies().get(0).getPermissions()).containsExactly("fp");
    Assertions.assertThat(user.getUserFamilies().get(0).getFamilyScopedAttributes()).containsEntry("k", "v");
    Assertions.assertThat(user.getScimTenantId()).isEqualTo("st");
    Assertions.assertThat(user.getSamlTenantId()).isEqualTo("sat");
    Assertions.assertThat(user.getOidcTenantId()).isEqualTo("ot");
    Assertions.assertThat(user.getConsentTenantId()).isEqualTo("ct");
    Assertions.assertThat(user.getLastLoginTime()).isEqualTo(50L);
    Assertions.assertThat(user.getTempLockExpiration()).isNull();
  }
}
