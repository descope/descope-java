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
import static org.mockito.Mockito.verifyNoInteractions;

import com.descope.enums.PromptType;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.ExchangeTokenRequest;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.SAMLLoginOptions;
import com.descope.model.auth.SAMLResponse;
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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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

  // SAML shares the code exchange: an authentication-only login returns without a session
  @Test
  void testExchangeTokenAuthenticationOnlyWithoutSession() {
    // No session or refresh JWT, a user, and flagged ssoAuthenticationOnly (last argument)
    JWTResponse jwtResponse = new JWTResponse("", "", null, null, null, null,
        MOCK_JWT_RESPONSE.getUser(), false, null, true);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authenticationInfo = samlService.exchangeToken("somecode");
    }

    // No tokens, but the user and the flag reach the caller
    Assertions.assertThat(authenticationInfo.getToken()).isNull();
    Assertions.assertThat(authenticationInfo.getRefreshToken()).isNull();
    Assertions.assertThat(authenticationInfo.getUser()).isNotNull();
    Assertions.assertThat(authenticationInfo.getSsoAuthenticationOnly()).isTrue();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStartWithAuthorizeOptions() throws Exception {
    SAMLLoginOptions options = new SAMLLoginOptions();
    options.setStepup(true);
    options.setSsoAppId("sso-app-id");
    options.setPkceChallenge("Y2hhbGxlbmdl");
    options.setTenantId("tenant-id");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SAMLResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> header = ArgumentCaptor.forClass(Supplier.class);
    String url;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(header.capture(), any())).thenReturn(apiProxy);
      url = samlService.start("tenant", "https://app.example/cb", options, "refresh-jwt",
          Arrays.asList(PromptType.LOGIN, PromptType.SELECT_ACCOUNT), true, "user@example.com",
          "init@example.com");
    }
    Assertions.assertThat(url).isEqualTo(MOCK_URL);
    Assertions.assertThat(header.getValue().get()).isEqualTo("Bearer " + PROJECT_ID + ":refresh-jwt");
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(SAMLResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/saml/authorize");
    Assertions.assertThat(uri.getValue().getRawQuery()).isEqualTo("tenant=tenant"
        + "&redirectUrl=https%3A%2F%2Fapp.example%2Fcb&forceAuthn=true&loginHint=user%40example.com"
        + "&initiatedEmail=init%40example.com&prompt=login&prompt=select_account");
    String json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
        .writeValueAsString(body.getValue());
    Assertions.assertThat(json).contains("\"stepup\":true", "\"ssoAppId\":\"sso-app-id\"",
        "\"pkceChallenge\":\"Y2hhbGxlbmdl\"", "\"tenantId\":\"tenant-id\"").doesNotContain("jwtrequired");
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStartWithAuthorizeOptionsOmitsUnset() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new SAMLResponse(MOCK_URL)).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> header = ArgumentCaptor.forClass(Supplier.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(header.capture(), any())).thenReturn(apiProxy);
      samlService.start("tenant", null, null, null, null, false, " ", null);
    }
    Assertions.assertThat(header.getValue().get()).isEqualTo("Bearer " + PROJECT_ID);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(null), eq(SAMLResponse.class));
    Assertions.assertThat(uri.getValue().getRawQuery()).isEqualTo("tenant=tenant");
  }

  @Test
  void testStartWithAuthorizeOptionsStepupRequiresRefreshToken() {
    LoginOptions options = LoginOptions.builder().mfa(true).build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Assertions.assertThatThrownBy(
          () -> samlService.start("tenant", null, options, null, null, false, null, null))
          .isInstanceOf(ServerCommonException.class)
          .hasMessageContaining("refreshToken");
    }
    verifyNoInteractions(apiProxy);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testExchangeTokenReturnsResponseFields() throws Exception {
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue("{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"sessionExpiration\":100,"
            + "\"externalToken\":\"ext\",\"claims\":{\"k\":\"v\"},\"tenantSSOID\":\"sso-id\","
            + "\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":30,\"cookieName\":\"DSR\","
            + "\"sessionCookieName\":\"DS\",\"sessionCookieDomain\":\"example.com\",\"unsavedSSO\":true,"
            + "\"knownDeviceJwt\":\"kdj\",\"userCreationDeferred\":true,\"externalPasskey\":true,"
            + "\"newField\":1}", JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> header = ArgumentCaptor.forClass(Supplier.class);
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(header.capture(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = samlService.exchangeToken("somecode");
      }
    }
    Assertions.assertThat(header.getValue().get()).isEqualTo("Bearer " + PROJECT_ID);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<ExchangeTokenRequest> body = ArgumentCaptor.forClass(ExchangeTokenRequest.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JWTResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/saml/exchange");
    Assertions.assertThat(body.getValue().getCode()).isEqualTo("somecode");
    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(100);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("ext");
    Assertions.assertThat(info.getClaims()).containsEntry("k", "v");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("sso-id");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(30);
    Assertions.assertThat(info.getCookieName()).isEqualTo("DSR");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("DS");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("example.com");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();
    Assertions.assertThat(info.getExternalPasskey()).isTrue();
  }
}
