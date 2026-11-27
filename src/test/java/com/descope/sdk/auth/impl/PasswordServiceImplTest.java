package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_PWD;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.MOCK_USER;
import static com.descope.sdk.TestUtils.MOCK_USER_RESPONSE;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.password.PasswordResetOptions;
import com.descope.model.password.PasswordResetResponse;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.PasswordService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class PasswordServiceImplTest {

  private PasswordService passwordService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.passwordService =
        AuthenticationServiceBuilder.buildServices(client).getPasswordService();
    this.userService = ManagementServiceBuilder.buildServices(client).getUserService();
  }

  @Test
  void testSignupForSuccess() {
    User usr = new User("someUserName", MOCK_EMAIL, "+910000000000");

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
        authenticationInfo = passwordService.signUp(MOCK_EMAIL, usr, MOCK_PWD);
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
  void testSignupForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> passwordService.signUp("", new User(), MOCK_PWD));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInForSuccess() {

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
        authenticationInfo = passwordService.signIn(MOCK_EMAIL, MOCK_PWD);
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
  void testSignInForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> passwordService.signIn("", MOCK_PWD));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSendPasswordResetForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      passwordService.sendPasswordReset(MOCK_EMAIL, MOCK_URL);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testSendPasswordResetForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> passwordService.sendPasswordReset("", MOCK_URL));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testReplaceUserPasswordForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        AuthenticationInfo authenticationInfo = passwordService.replaceUserPassword(MOCK_EMAIL, MOCK_PWD, MOCK_PWD);
        Assertions.assertThat(authenticationInfo).isNotNull();
        verify(apiProxy, times(1)).post(any(), any(), any());
      }
    }
  }

  @Test
  void testReplaceUserPasswordForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> passwordService.replaceUserPassword("", MOCK_PWD, MOCK_PWD));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPasswordForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any()))
          .thenReturn(apiProxy);
      passwordService.updateUserPassword(MOCK_EMAIL, MOCK_PWD, MOCK_REFRESH_TOKEN);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateUserPasswordForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> passwordService.updateUserPassword("", MOCK_PWD, ""));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPasswordForEmptyRefreshToken() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> passwordService.updateUserPassword(MOCK_EMAIL, MOCK_PWD, ""));
    assertNotNull(thrown);
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetPasswordPolicy() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    PasswordPolicy passwordPolicy =
        PasswordPolicy.builder()
            .minLength(8)
            .lowercase(false)
            .nonAlphanumeric(false)
            .number(true)
            .build();
    doReturn(passwordPolicy).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      PasswordPolicy response = passwordService.getPasswordPolicy();
      Assertions.assertThat(response.getMinLength()).isEqualTo(8);
      Assertions.assertThat(response.isNumber()).isTrue();
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-");
    AuthenticationInfo authInfo = passwordService.signUp(loginId, MOCK_USER, MOCK_PWD);
    UserResponse user = authInfo.getUser();
    assertNotNull(user);
    assertEquals(MOCK_EMAIL, user.getEmail());
    assertEquals(MOCK_NAME, user.getName());
    assertEquals(MOCK_PHONE, user.getPhone());
    assertThat(user.getLoginIds()).containsExactly(loginId);
    assertThat(user.getUserId()).isNotBlank();
    assertEquals("enabled", user.getStatus());
    assertThat(user.getCreatedTime()).isGreaterThan(0);
    assertTrue(authInfo.getFirstSeen());
    assertThat(authInfo.getRefreshToken().getJwt()).isNotBlank();
    authInfo = passwordService.signIn(loginId, MOCK_PWD);
    user = authInfo.getUser();
    assertNotNull(user);
    assertFalse(authInfo.getFirstSeen());
    authInfo = passwordService.replaceUserPassword(loginId, MOCK_PWD, MOCK_PWD + "1");
    assertThat(authInfo.getRefreshToken().getJwt()).isNotBlank();
    authInfo = passwordService.signIn(loginId, MOCK_PWD + "1");
    assertThat(authInfo.getRefreshToken().getJwt()).isNotBlank();
    passwordService.updateUserPassword(
        loginId, MOCK_PWD + "2", authInfo.getRefreshToken().getJwt());
    passwordService.signIn(loginId, MOCK_PWD + "2");
    userService.delete(loginId);
    PasswordPolicy policy = passwordService.getPasswordPolicy();
    assertThat(policy.getMinLength()).isGreaterThan(7);
  }

  private static final String AUTH_MGMT_KEY = "authMgmtKey";

  private static JWTResponse fullJwtResponse() {
    JWTResponse jwtResponse = new JWTResponse("someSessionJwt", "someRefreshJwt", "domain", "/", 1234567, 1234567890,
        MOCK_USER_RESPONSE, true, null, false);
    jwtResponse.setSessionExpiration(3600);
    jwtResponse.setExternalToken("externalToken");
    jwtResponse.setClaims(mapOf("claim", "value"));
    jwtResponse.setTenantSSOID("tenantSSOID");
    jwtResponse.setTrustedDeviceJwt("trustedDeviceJwt");
    jwtResponse.setNextRefreshSeconds(60);
    jwtResponse.setCookieName("cookieName");
    jwtResponse.setSessionCookieName("sessionCookieName");
    jwtResponse.setSessionCookieDomain("sessionCookieDomain");
    jwtResponse.setUnsavedSSO(true);
    jwtResponse.setKnownDeviceJwt("knownDeviceJwt");
    jwtResponse.setUserCreationDeferred(true);
    jwtResponse.setExternalPasskey(true);
    return jwtResponse;
  }

  private static void assertFullAuthenticationInfo(AuthenticationInfo info) {
    assertNotNull(info.getToken());
    assertNotNull(info.getRefreshToken());
    assertEquals(MOCK_USER_RESPONSE, info.getUser());
    assertEquals(true, info.getFirstSeen());
    assertEquals(false, info.getSsoAuthenticationOnly());
    assertEquals(3600, info.getSessionExpiration());
    assertEquals("externalToken", info.getExternalToken());
    assertEquals(mapOf("claim", "value"), info.getClaims());
    assertEquals("tenantSSOID", info.getTenantSSOID());
    assertEquals("trustedDeviceJwt", info.getTrustedDeviceJwt());
    assertEquals(60, info.getNextRefreshSeconds());
    assertEquals("cookieName", info.getCookieName());
    assertEquals("sessionCookieName", info.getSessionCookieName());
    assertEquals("sessionCookieDomain", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("knownDeviceJwt", info.getKnownDeviceJwt());
    assertEquals(true, info.getUserCreationDeferred());
    assertEquals(true, info.getExternalPasskey());
  }

  /** Runs the call against a mocked proxy, capturing the Authorization header, URI and body. */
  @SuppressWarnings("unchecked")
  private static <T> T runCaptured(Object response, AtomicReference<String> authHeader,
      ArgumentCaptor<URI> uri, ArgumentCaptor<Object> body, Function<PasswordService, T> call) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .authManagementKey(AUTH_MGMT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    PasswordService service = AuthenticationServiceBuilder.buildServices(client).getPasswordService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class, Mockito.CALLS_REAL_METHODS)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      T result = call.apply(service);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), any());
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> toJson(Object body) {
    return new ObjectMapper().setSerializationInclusion(Include.NON_NULL).convertValue(body, Map.class);
  }

  @Test
  void testSignUpWithOptions() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    SignUpOptions options = SignUpOptions.builder()
        .customClaims(mapOf("k", "v"))
        .templateOptions(mapOf("t", "o"))
        .build();
    AuthenticationInfo info = runCaptured(fullJwtResponse(), authHeader, uri, body,
        s -> s.signUp(MOCK_EMAIL, MOCK_USER, MOCK_PWD, options));
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MGMT_KEY, authHeader.get());
    assertEquals("/v1/auth/password/signup", uri.getValue().getPath());
    Map<String, Object> json = toJson(body.getValue());
    assertEquals(MOCK_EMAIL, json.get("loginId"));
    assertEquals(MOCK_PWD, json.get("password"));
    assertNotNull(json.get("user"));
    assertEquals(mapOf("customClaims", mapOf("k", "v"),
        "templateOptions", mapOf("t", "o")), json.get("loginOptions"));
    assertFullAuthenticationInfo(info);
  }

  @Test
  void testSignUpWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.signUp("", MOCK_USER, MOCK_PWD, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSignInWithOptions() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    LoginOptions loginOptions = LoginOptions.builder()
        .customClaims(mapOf("k", "v"))
        .revokeOtherSessions(true)
        .build();
    AuthenticationInfo info = runCaptured(fullJwtResponse(), authHeader, uri, body,
        s -> s.signIn(MOCK_EMAIL, MOCK_PWD, "ssoAppId", loginOptions, null));
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MGMT_KEY, authHeader.get());
    assertEquals("/v1/auth/password/signin", uri.getValue().getPath());
    Map<String, Object> json = toJson(body.getValue());
    assertEquals(MOCK_EMAIL, json.get("loginId"));
    assertEquals(MOCK_PWD, json.get("password"));
    assertEquals("ssoAppId", json.get("ssoAppId"));
    Map<String, Object> sentOptions = (Map<String, Object>) json.get("loginOptions");
    assertEquals(mapOf("k", "v"), sentOptions.get("customClaims"));
    assertEquals(true, sentOptions.get("revokeOtherSessions"));
    assertFalse(sentOptions.containsKey("jwtrequired"));
    assertFullAuthenticationInfo(info);
  }

  @Test
  void testSignInWithStepupSendsRefreshToken() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    runCaptured(fullJwtResponse(), authHeader, uri, body,
        s -> s.signIn(MOCK_EMAIL, MOCK_PWD, null, loginOptions, MOCK_REFRESH_TOKEN));
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MGMT_KEY, authHeader.get());
  }

  @Test
  void testSignInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.signIn("", MOCK_PWD, null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithMfaForEmptyRefreshToken() {
    LoginOptions loginOptions = LoginOptions.builder().mfa(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.signIn(MOCK_EMAIL, MOCK_PWD, null, loginOptions, ""));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSendPasswordResetWithOptions() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    PasswordResetResponse expected = new PasswordResetResponse("email", "pendingRef", "linkId", "u***@domain.com");
    PasswordResetOptions options = PasswordResetOptions.builder()
        .providerId("providerId")
        .templateId("templateId")
        .locale("en")
        .useRecovery(true)
        .build();
    PasswordResetResponse response = runCaptured(expected, authHeader, uri, body,
        s -> s.sendPasswordReset(MOCK_EMAIL, MOCK_URL, mapOf("t", "o"), options));
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MGMT_KEY, authHeader.get());
    assertEquals("/v1/auth/password/reset", uri.getValue().getPath());
    Map<String, Object> json = toJson(body.getValue());
    assertEquals(MOCK_EMAIL, json.get("loginId"));
    assertEquals(MOCK_URL, json.get("redirectUrl"));
    assertEquals(mapOf("t", "o"), json.get("templateOptions"));
    assertEquals("providerId", json.get("providerId"));
    assertEquals("templateId", json.get("templateId"));
    assertEquals("en", json.get("locale"));
    assertEquals(true, json.get("useRecovery"));
    assertEquals(expected, response);
  }

  @Test
  void testSendPasswordResetWithoutOptions() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    runCaptured(new PasswordResetResponse(), authHeader, uri, body,
        s -> s.sendPasswordReset(MOCK_EMAIL, null, null, null));
    assertEquals(mapOf("loginId", MOCK_EMAIL), toJson(body.getValue()));
  }

  @Test
  void testSendPasswordResetWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.sendPasswordReset("", MOCK_URL, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testReplaceUserPasswordWithRevokeOtherSessions() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    AuthenticationInfo info = runCaptured(fullJwtResponse(), authHeader, uri, body,
        s -> s.replaceUserPassword(MOCK_EMAIL, MOCK_PWD, "newPassword1!", true, new String[] {"web"}));
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MGMT_KEY, authHeader.get());
    assertEquals("/v1/auth/password/replace", uri.getValue().getPath());
    Map<String, Object> json = toJson(body.getValue());
    assertEquals(MOCK_EMAIL, json.get("loginId"));
    assertEquals(MOCK_PWD, json.get("oldPassword"));
    assertEquals("newPassword1!", json.get("newPassword"));
    assertEquals(true, json.get("revokeOtherSessions"));
    assertEquals(Arrays.asList("web"), json.get("revokeOtherSessionsTypes"));
    assertFullAuthenticationInfo(info);
  }

  @Test
  void testReplaceUserPasswordWithRevokeOtherSessionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.replaceUserPassword("", MOCK_PWD, MOCK_PWD, true, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testParseNewResponseFields() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    PasswordPolicy policy = mapper.readValue(
        "{\"minLength\":8,\"strengthScoreEnabled\":true,\"strengthScore\":3}", PasswordPolicy.class);
    assertEquals(true, policy.getStrengthScoreEnabled());
    assertEquals(3, policy.getStrengthScore());
    PasswordResetResponse reset = mapper.readValue("{\"resetMethod\":\"email\",\"pendingRef\":\"p\","
        + "\"linkId\":\"l\",\"maskedEmail\":\"m\"}", PasswordResetResponse.class);
    assertEquals(new PasswordResetResponse("email", "p", "l", "m"), reset);
    JWTResponse jwt = mapper.readValue("{\"sessionExpiration\":1,\"externalToken\":\"e\",\"claims\":{\"a\":\"b\"},"
        + "\"tenantSSOID\":\"t\",\"trustedDeviceJwt\":\"td\",\"nextRefreshSeconds\":2,\"cookieName\":\"c\","
        + "\"sessionCookieName\":\"sc\",\"sessionCookieDomain\":\"sd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"k\",\"userCreationDeferred\":true,\"externalPasskey\":true}", JWTResponse.class);
    assertEquals(1, jwt.getSessionExpiration());
    assertEquals("e", jwt.getExternalToken());
    assertEquals(mapOf("a", "b"), jwt.getClaims());
    assertEquals("t", jwt.getTenantSSOID());
    assertEquals("td", jwt.getTrustedDeviceJwt());
    assertEquals(2, jwt.getNextRefreshSeconds());
    assertEquals("c", jwt.getCookieName());
    assertEquals("sc", jwt.getSessionCookieName());
    assertEquals("sd", jwt.getSessionCookieDomain());
    assertEquals(true, jwt.getUnsavedSSO());
    assertEquals("k", jwt.getKnownDeviceJwt());
    assertEquals(true, jwt.getUserCreationDeferred());
    assertEquals(true, jwt.getExternalPasskey());
  }
}
