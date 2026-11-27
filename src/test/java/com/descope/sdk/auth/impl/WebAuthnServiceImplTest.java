package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.model.webauthn.PasskeyAuthenticatorSelection;
import com.descope.model.webauthn.PasskeyOptions;
import com.descope.model.webauthn.WebAuthnFinishRequest;
import com.descope.model.webauthn.WebAuthnTransactionResponse;
import com.descope.model.webauthn.WebAuthnUpdateFinishResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.WebAuthnService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class WebAuthnServiceImplTest {

  private WebAuthnService webAuthnService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.webAuthnService =
        AuthenticationServiceBuilder.buildServices(client).getWebAuthnService();
  }

  @Test
  void testSignUpStartForSuccess() {
    User user = new User(MOCK_NAME, MOCK_EMAIL, MOCK_PHONE);
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse signUp = webAuthnService.signUpStart(MOCK_EMAIL, user, "kuku");
      assertThat(signUp).isNotNull();
      assertEquals("t1", signUp.getTransactionId());
    }
  }

  @Test
  void testSignUpStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpStart(null, null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpStart("x", null, null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        AuthenticationInfo authenticationInfo = webAuthnService.signUpFinish(new WebAuthnFinishRequest("t1", "r"));
        assertThat(authenticationInfo).isNotNull();
        Token sessionToken = authenticationInfo.getToken();
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getJwt()).isNotBlank();
        assertThat(sessionToken.getClaims()).isNotEmpty();
        assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);
        Token refreshToken = authenticationInfo.getRefreshToken();
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getJwt()).isNotBlank();
        assertThat(refreshToken.getClaims()).isNotEmpty();
        assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);
        UserResponse user = authenticationInfo.getUser();
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isNotBlank();
        assertThat(user.getLoginIds()).isNotEmpty();
      }
    }
  }

  @Test
  void testSignUpFinishEmptyRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpFinish(null));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpFinish(new WebAuthnFinishRequest(null, null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpFinish(new WebAuthnFinishRequest("kuku", null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse signUp = webAuthnService.signInStart(MOCK_EMAIL, "kuku", null, null);
      assertThat(signUp).isNotNull();
      assertEquals("t1", signUp.getTransactionId());
    }
  }

  @Test
  void testSignInStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInStart(null, null, null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInStart("x", null, null, null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartEmptyToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
            () -> webAuthnService.signInStart("x", "x", null, new LoginOptions(true, false, null, null)));
    assertNotNull(thrown);
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        AuthenticationInfo authenticationInfo = webAuthnService.signInFinish(new WebAuthnFinishRequest("t1", "r"));
        assertThat(authenticationInfo).isNotNull();
        Token sessionToken = authenticationInfo.getToken();
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getJwt()).isNotBlank();
        assertThat(sessionToken.getClaims()).isNotEmpty();
        assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);
        Token refreshToken = authenticationInfo.getRefreshToken();
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getJwt()).isNotBlank();
        assertThat(refreshToken.getClaims()).isNotEmpty();
        assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);
        UserResponse user = authenticationInfo.getUser();
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isNotBlank();
        assertThat(user.getLoginIds()).isNotEmpty();
      }
    }
  }

  @Test
  void testSignInFinishEmptyRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInFinish(null));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInFinish(new WebAuthnFinishRequest(null, null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInFinish(new WebAuthnFinishRequest("kuku", null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse signUp = webAuthnService.signUpOrInStart(MOCK_EMAIL, "kuku");
      assertThat(signUp).isNotNull();
      assertEquals("t1", signUp.getTransactionId());
    }
  }

  @Test
  void testSignUpOrInStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart(null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart("x", null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        WebAuthnTransactionResponse signUp = webAuthnService.updateUserDeviceStart(MOCK_EMAIL, "kuku", "kiki");
        assertThat(signUp).isNotNull();
        assertEquals("t1", signUp.getTransactionId());
      }
    }
  }

  @Test
  void testUpdateUserDeviceStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart(null, null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart("x", null, null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartEmptyToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart("x", "x", null));
    assertNotNull(thrown);
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      webAuthnService.updateUserDeviceFinish(new WebAuthnFinishRequest("t1", "r"));
    }
  }

  @Test
  void testUpdateUserDeviceFinishEmptyRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceFinish(null));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceFinish(new WebAuthnFinishRequest(null, null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceFinish(new WebAuthnFinishRequest("kuku", null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }

  private static final PasskeyOptions PASSKEY_OPTIONS = PasskeyOptions.builder()
      .authenticatorSelection(PasskeyAuthenticatorSelection.builder()
          .authenticatorAttachment(PasskeyAuthenticatorSelection.AUTHENTICATOR_ATTACHMENT_PLATFORM)
          .residentKey(PasskeyAuthenticatorSelection.RESIDENT_KEY_REQUIRED)
          .build())
      .attestation(PasskeyOptions.ATTESTATION_DIRECT)
      .userVerification(PasskeyOptions.USER_VERIFICATION_REQUIRED)
      .extensionsJSON("{}")
      .build();

  /** A start call's captured request: Authorization header, URI and body. */
  private static class SentRequest {
    String authHeader;
    URI uri;
    Map<String, Object> body;
  }

  @SuppressWarnings("unchecked")
  private static SentRequest sendStart(Function<WebAuthnService, WebAuthnTransactionResponse> call) {
    WebAuthnService service = AuthenticationServiceBuilder.buildServices(TestUtils.getClient()).getWebAuthnService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    SentRequest sent = new SentRequest();
    AtomicReference<String> header = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class, Mockito.CALLS_REAL_METHODS)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      WebAuthnTransactionResponse response = call.apply(service);
      assertEquals("t1", response.getTransactionId());
      assertEquals("o", response.getOptions());
      assertThat(response.isCreate()).isTrue();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(WebAuthnTransactionResponse.class));
    sent.authHeader = header.get();
    sent.uri = uri.getValue();
    sent.body = (Map<String, Object>) body.getValue();
    return sent;
  }

  @Test
  void testSignUpStartWithOptions() {
    User user = new User(MOCK_NAME, MOCK_EMAIL, MOCK_PHONE);
    SignUpOptions signUpOptions = SignUpOptions.builder().customClaims(Collections.singletonMap("k", "v")).build();
    SentRequest sent = sendStart(s -> s.signUpStart(MOCK_EMAIL, user, "https://example.com", signUpOptions,
        PASSKEY_OPTIONS, "My passkey"));
    assertEquals("Bearer " + PROJECT_ID, sent.authHeader);
    assertEquals("/v1/auth/webauthn/signup/start", sent.uri.getPath());
    assertEquals(MOCK_EMAIL, sent.body.get("loginId"));
    assertEquals(user, sent.body.get("user"));
    assertEquals("https://example.com", sent.body.get("origin"));
    assertEquals(signUpOptions, sent.body.get("loginOptions"));
    assertEquals(PASSKEY_OPTIONS, sent.body.get("passkeyOptions"));
    assertEquals("My passkey", sent.body.get("displayName"));
  }

  @Test
  void testSignUpStartWithoutOptionsSendsOnlyRequiredFields() {
    SentRequest sent = sendStart(s -> s.signUpStart(MOCK_EMAIL, null, "https://example.com", null, null, null));
    assertThat(sent.body).containsOnlyKeys("loginId", "user", "origin");
  }

  @Test
  void testSignInStartWithPasskeyOptions() {
    SentRequest sent = sendStart(s -> s.signInStart(MOCK_EMAIL, "https://example.com", null, null, PASSKEY_OPTIONS));
    assertEquals("Bearer " + PROJECT_ID, sent.authHeader);
    assertEquals("/v1/auth/webauthn/signin/start", sent.uri.getPath());
    assertThat(sent.body).containsOnlyKeys("loginId", "origin", "passkeyOptions");
    assertEquals(PASSKEY_OPTIONS, sent.body.get("passkeyOptions"));
  }

  @Test
  void testSignInStartMfaWithPasskeyOptionsSendsRefreshJwt() {
    LoginOptions loginOptions = LoginOptions.builder().mfa(true).build();
    SentRequest sent = sendStart(s -> s.signInStart(MOCK_EMAIL, "https://example.com", "refresh-jwt", loginOptions,
        PASSKEY_OPTIONS));
    assertEquals("Bearer " + PROJECT_ID + ":refresh-jwt", sent.authHeader);
    assertEquals(loginOptions, sent.body.get("loginOptions"));
  }

  @Test
  void testSignUpOrInStartWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().templateId("t").build();
    SentRequest sent = sendStart(s -> s.signUpOrInStart(MOCK_EMAIL, "https://example.com", null, loginOptions,
        "app1", PASSKEY_OPTIONS, "My passkey"));
    assertEquals("Bearer " + PROJECT_ID, sent.authHeader);
    assertEquals("/v1/auth/webauthn/signup-in/start", sent.uri.getPath());
    assertEquals(MOCK_EMAIL, sent.body.get("loginId"));
    assertEquals("https://example.com", sent.body.get("origin"));
    assertEquals(loginOptions, sent.body.get("loginOptions"));
    assertEquals("app1", sent.body.get("ssoAppId"));
    assertEquals(PASSKEY_OPTIONS, sent.body.get("passkeyOptions"));
    assertEquals("My passkey", sent.body.get("displayName"));
  }

  @Test
  void testSignUpOrInStartWithoutOptionsSendsOnlyRequiredFields() {
    SentRequest sent = sendStart(s -> s.signUpOrInStart(MOCK_EMAIL, "https://example.com", null, null, null, null,
        null));
    assertThat(sent.body).containsOnlyKeys("loginId", "origin");
  }

  @Test
  void testSignUpOrInStartStepUpSendsRefreshJwt() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    SentRequest sent = sendStart(s -> s.signUpOrInStart(MOCK_EMAIL, "https://example.com", "refresh-jwt",
        loginOptions, null, null, null));
    assertEquals("Bearer " + PROJECT_ID + ":refresh-jwt", sent.authHeader);
  }

  @Test
  void testSignUpOrInStartStepUpEmptyToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart(MOCK_EMAIL, "x", null, LoginOptions.builder().mfa(true).build(),
              null, null, null));
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInStartWithOptionsEmptyLoginIdAndOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart(null, "x", null, null, null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart("x", null, null, null, null, null, null));
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartWithOptions() {
    SentRequest sent = sendStart(s -> s.updateUserDeviceStart(MOCK_EMAIL, "https://example.com", "refresh-jwt", true,
        PASSKEY_OPTIONS, "My passkey"));
    assertEquals("Bearer " + PROJECT_ID + ":refresh-jwt", sent.authHeader);
    assertEquals("/v1/auth/webauthn/update/start", sent.uri.getPath());
    assertEquals(MOCK_EMAIL, sent.body.get("loginId"));
    assertEquals("https://example.com", sent.body.get("origin"));
    assertEquals(true, sent.body.get("mfa"));
    assertEquals(PASSKEY_OPTIONS, sent.body.get("passkeyOptions"));
    assertEquals("My passkey", sent.body.get("displayName"));
  }

  @Test
  void testUpdateUserDeviceStartWithoutOptionsSendsOnlyRequiredFields() {
    SentRequest sent = sendStart(s -> s.updateUserDeviceStart(MOCK_EMAIL, "https://example.com", "refresh-jwt",
        false, null, null));
    assertThat(sent.body).containsOnlyKeys("loginId", "origin");
  }

  @Test
  void testUpdateUserDeviceStartWithOptionsEmptyToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart("x", "x", null, true, null, null));
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testPasskeyOptionsSerialization() throws Exception {
    String json = new ObjectMapper().writeValueAsString(PASSKEY_OPTIONS);
    assertThat(json).contains("\"extensionsJSON\":\"{}\"", "\"attestation\":\"direct\"",
        "\"authenticatorAttachment\":\"platform\"", "\"residentKey\":\"required\"");
  }

  @Test
  void testSignInFinishReturnsAllResponseFields() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"sessionExpiration\":1,\"externalToken\":\"et\","
        + "\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"t1\",\"trustedDeviceJwt\":\"td\",\"nextRefreshSeconds\":2,"
        + "\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"kd\",\"userCreationDeferred\":true,\"externalPasskey\":true,\"unknown\":1}";
    JWTResponse jwtResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      for (AuthenticationInfo info : Arrays.asList(
          webAuthnService.signInFinish(new WebAuthnFinishRequest("t1", "r")),
          webAuthnService.signUpFinish(new WebAuthnFinishRequest("t1", "r")))) {
        assertEquals(1, info.getSessionExpiration());
        assertEquals("et", info.getExternalToken());
        assertEquals(Collections.singletonMap("c", "v"), info.getClaims());
        assertEquals("t1", info.getTenantSSOID());
        assertEquals("td", info.getTrustedDeviceJwt());
        assertEquals(2, info.getNextRefreshSeconds());
        assertEquals("cn", info.getCookieName());
        assertEquals("scn", info.getSessionCookieName());
        assertEquals("scd", info.getSessionCookieDomain());
        assertEquals(true, info.getUnsavedSSO());
        assertEquals("kd", info.getKnownDeviceJwt());
        assertEquals(true, info.getUserCreationDeferred());
        assertEquals(true, info.getExternalPasskey());
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testUpdateUserDeviceFinishWithSession() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnUpdateFinishResponse(MOCK_JWT_RESPONSE)).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      WebAuthnFinishRequest finishRequest = new WebAuthnFinishRequest("t1", "r");
      AuthenticationInfo info = webAuthnService.updateUserDeviceFinishWithSession(finishRequest);
      assertThat(info).isNotNull();
      assertEquals(MOCK_TOKEN, info.getToken());
      assertEquals(MOCK_TOKEN, info.getRefreshToken());
      assertEquals(MOCK_JWT_RESPONSE.getUser(), info.getUser());
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).post(uri.capture(), eq(finishRequest), eq(WebAuthnUpdateFinishResponse.class));
      assertEquals("/v1/auth/webauthn/update/finish", uri.getValue().getPath());
      assertEquals("Bearer " + PROJECT_ID, header.get());
    }
  }

  @Test
  void testUpdateUserDeviceFinishWithSessionNoSession() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      doReturn(null).when(apiProxy).post(any(), any(), any());
      assertThat(webAuthnService.updateUserDeviceFinishWithSession(new WebAuthnFinishRequest("t1", "r"))).isNull();
      doReturn(new WebAuthnUpdateFinishResponse()).when(apiProxy).post(any(), any(), any());
      assertThat(webAuthnService.updateUserDeviceFinishWithSession(new WebAuthnFinishRequest("t1", "r"))).isNull();
      doReturn(new WebAuthnUpdateFinishResponse(new JWTResponse())).when(apiProxy).post(any(), any(), any());
      assertThat(webAuthnService.updateUserDeviceFinishWithSession(new WebAuthnFinishRequest("t1", "r"))).isNull();
    }
  }

  @Test
  void testUpdateUserDeviceFinishWithSessionEmptyRequest() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      for (WebAuthnFinishRequest request : Arrays.asList(null, new WebAuthnFinishRequest(null, "r"),
          new WebAuthnFinishRequest("t1", null))) {
        ServerCommonException thrown =
            assertThrows(ServerCommonException.class,
              () -> webAuthnService.updateUserDeviceFinishWithSession(request));
        assertEquals("The Finish Request argument is invalid", thrown.getMessage());
      }
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }
}
