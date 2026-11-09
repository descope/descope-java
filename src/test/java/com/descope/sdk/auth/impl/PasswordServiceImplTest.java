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
import static com.descope.sdk.TestUtils.PROJECT_ID;
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
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.jwt.response.JWTResponse;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

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

  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static final String FULL_JWT_RESPONSE = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\","
      + "\"sessionExpiration\":100,\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"t1\","
      + "\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":30,\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\","
      + "\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\","
      + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,\"unknownField\":1,"
      + "\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,"
      + "\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
      + "\"consentExpiration\":5,\"recoveryEmail\":\"re@example.com\",\"verifiedRecoveryEmail\":true,"
      + "\"recoveryPhone\":\"+1\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":6,\"roleIds\":[\"r1\"],"
      + "\"recoveryCodes\":true,\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\"}],"
      + "\"scimTenantId\":\"st\",\"samlTenantId\":\"sa\",\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\","
      + "\"lastLoginTime\":7}}";

  private static final class Captured {
    private URI uri;
    private Map<String, Object> body;
    private String authorization;
    private Object result;
  }

  @SuppressWarnings("unchecked")
  private static Captured capturePost(Supplier<Object> call, Object response) throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authorization = new AtomicReference<>();
    Captured captured = new Captured();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authorization.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class,
          org.mockito.Mockito.CALLS_REAL_METHODS)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        captured.result = call.get();
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), any());
    captured.uri = uri.getValue();
    captured.body = MAPPER.convertValue(body.getValue(), new TypeReference<Map<String, Object>>() {});
    captured.authorization = authorization.get();
    return captured;
  }

  private static void assertFullAuthenticationInfo(AuthenticationInfo info) {
    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(100);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("ext");
    Assertions.assertThat(info.getClaims()).containsEntry("c", "v");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("t1");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(30);
    Assertions.assertThat(info.getCookieName()).isEqualTo("cn");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("scn");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    Assertions.assertThat(info.getSsoAuthenticationOnly()).isTrue();
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();
    UserResponse u = info.getUser();
    Assertions.assertThat(u.getUserId()).isEqualTo("u1");
    Assertions.assertThat(u.getExternalIds()).containsExactly("e1");
    Assertions.assertThat(u.getWebauthn()).isTrue();
    Assertions.assertThat(u.getPassword()).isTrue();
    Assertions.assertThat(u.getEditable()).isTrue();
    Assertions.assertThat(u.getSCIM()).isTrue();
    Assertions.assertThat(u.getPush()).isTrue();
    Assertions.assertThat(u.getPermissions()).containsExactly("p1");
    Assertions.assertThat(u.getOIDC()).isTrue();
    Assertions.assertThat(u.getConsentExpiration()).isEqualTo(5L);
    Assertions.assertThat(u.getRecoveryEmail()).isEqualTo("re@example.com");
    Assertions.assertThat(u.getVerifiedRecoveryEmail()).isTrue();
    Assertions.assertThat(u.getRecoveryPhone()).isEqualTo("+1");
    Assertions.assertThat(u.getVerifiedRecoveryPhone()).isTrue();
    Assertions.assertThat(u.getModifiedTime()).isEqualTo(6L);
    Assertions.assertThat(u.getRoleIds()).containsExactly("r1");
    Assertions.assertThat(u.getRecoveryCodes()).isTrue();
    Assertions.assertThat(u.getDependent()).isTrue();
    Assertions.assertThat(u.getUserFamilies().get(0).getFamilyId()).isEqualTo("f1");
    Assertions.assertThat(u.getScimTenantId()).isEqualTo("st");
    Assertions.assertThat(u.getSamlTenantId()).isEqualTo("sa");
    Assertions.assertThat(u.getOidcTenantId()).isEqualTo("ot");
    Assertions.assertThat(u.getConsentTenantId()).isEqualTo("ct");
    Assertions.assertThat(u.getLastLoginTime()).isEqualTo(7L);
  }

  @Test
  void testSignUpWithOptions() throws Exception {
    JWTResponse response = MAPPER.readValue(FULL_JWT_RESPONSE, JWTResponse.class);
    SignUpOptions options = SignUpOptions.builder().customClaims(Collections.singletonMap("k", "v"))
        .templateOptions(Collections.singletonMap("t", "o")).locale("en").tenantId("t1").build();
    Captured c = capturePost(() -> passwordService.signUp(MOCK_EMAIL, new User(), MOCK_PWD, options), response);
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/password/signup");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Assertions.assertThat(c.body).containsEntry("loginId", MOCK_EMAIL).containsEntry("password", MOCK_PWD);
    Assertions.assertThat(c.body.get("loginOptions")).isEqualTo(MAPPER.readValue(
        "{\"customClaims\":{\"k\":\"v\"},\"templateOptions\":{\"t\":\"o\"},\"locale\":\"en\",\"tenantId\":\"t1\"}",
        Map.class));
    assertFullAuthenticationInfo((AuthenticationInfo) c.result);
  }

  @Test
  void testSignUpWithoutOptionsOmitsLoginOptions() throws Exception {
    Captured c = capturePost(() -> passwordService.signUp(MOCK_EMAIL, new User(), MOCK_PWD, null),
        MOCK_JWT_RESPONSE);
    Assertions.assertThat(c.body).doesNotContainKey("loginOptions");
  }

  @Test
  void testSignUpWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.signUp("", new User(), MOCK_PWD, new SignUpOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithOptions() throws Exception {
    JWTResponse response = MAPPER.readValue(FULL_JWT_RESPONSE, JWTResponse.class);
    LoginOptions options = LoginOptions.builder().stepup(true).mfa(true)
        .customClaims(Collections.singletonMap("k", "v")).templateOptions(Collections.singletonMap("t", "o"))
        .revokeOtherSessions(true).revokeOtherSessionsTypes(new String[] {"web"}).locale("en")
        .ssoAppId("app1").tenantId("t1").jwt("ignored").templateId("ignored").build();
    Captured c = capturePost(() -> passwordService.signIn(MOCK_EMAIL, MOCK_PWD, "app2", options,
        MOCK_REFRESH_TOKEN), response);
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/password/signin");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    Assertions.assertThat(c.body).containsEntry("loginId", MOCK_EMAIL).containsEntry("password", MOCK_PWD)
        .containsEntry("ssoAppId", "app2");
    Assertions.assertThat(c.body.get("loginOptions")).isEqualTo(MAPPER.readValue(
        "{\"stepup\":true,\"mfa\":true,\"customClaims\":{\"k\":\"v\"},\"templateOptions\":{\"t\":\"o\"},"
            + "\"revokeOtherSessions\":true,\"revokeOtherSessionsTypes\":[\"web\"],\"locale\":\"en\","
            + "\"ssoAppId\":\"app1\",\"tenantId\":\"t1\"}", Map.class));
    assertFullAuthenticationInfo((AuthenticationInfo) c.result);
  }

  @Test
  void testSignInWithOptionsWithoutStepup() throws Exception {
    Captured c = capturePost(() -> passwordService.signIn(MOCK_EMAIL, MOCK_PWD, null, null, null),
        MOCK_JWT_RESPONSE);
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Assertions.assertThat(c.body).containsOnlyKeys("loginId", "password");
  }

  @Test
  void testSignInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.signIn("", MOCK_PWD, null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithStepupForEmptyRefreshToken() {
    LoginOptions options = LoginOptions.builder().stepup(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.signIn(MOCK_EMAIL, MOCK_PWD, null, options, ""));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSendPasswordResetWithOptions() throws Exception {
    PasswordResetResponse response = MAPPER.readValue("{\"resetMethod\":\"magiclink\",\"pendingRef\":\"pr\","
        + "\"linkId\":\"l1\",\"maskedEmail\":\"a***@example.com\",\"unknownField\":1}", PasswordResetResponse.class);
    PasswordResetOptions options = PasswordResetOptions.builder().providerId("prov").locale("en")
        .useRecovery(true).build();
    Captured c = capturePost(() -> passwordService.sendPasswordReset(MOCK_EMAIL, MOCK_URL,
        Collections.singletonMap("t", "o"), options), response);
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/password/reset");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Assertions.assertThat(c.body).isEqualTo(MAPPER.readValue("{\"loginId\":\"" + MOCK_EMAIL + "\","
        + "\"redirectUrl\":\"" + MOCK_URL + "\",\"templateOptions\":{\"t\":\"o\"},\"providerId\":\"prov\","
        + "\"locale\":\"en\",\"useRecovery\":true}", Map.class));
    PasswordResetResponse res = (PasswordResetResponse) c.result;
    Assertions.assertThat(res.getResetMethod()).isEqualTo("magiclink");
    Assertions.assertThat(res.getPendingRef()).isEqualTo("pr");
    Assertions.assertThat(res.getLinkId()).isEqualTo("l1");
    Assertions.assertThat(res.getMaskedEmail()).isEqualTo("a***@example.com");
  }

  @Test
  void testSendPasswordResetWithoutOptions() throws Exception {
    Captured c = capturePost(() -> passwordService.sendPasswordReset(MOCK_EMAIL, null, null, null),
        new PasswordResetResponse());
    Assertions.assertThat(c.body).containsOnlyKeys("loginId");
  }

  @Test
  void testSendPasswordResetWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.sendPasswordReset("", MOCK_URL, null, new PasswordResetOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testReplaceUserPasswordWithRevoke() throws Exception {
    JWTResponse response = MAPPER.readValue(FULL_JWT_RESPONSE, JWTResponse.class);
    Captured c = capturePost(() -> passwordService.replaceUserPassword(MOCK_EMAIL, "old", "new", true,
        new String[] {"web"}), response);
    Assertions.assertThat(c.uri.getPath()).isEqualTo("/v1/auth/password/replace");
    Assertions.assertThat(c.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Assertions.assertThat(c.body).isEqualTo(MAPPER.readValue("{\"loginId\":\"" + MOCK_EMAIL + "\","
        + "\"oldPassword\":\"old\",\"newPassword\":\"new\",\"revokeOtherSessions\":true,"
        + "\"revokeOtherSessionsTypes\":[\"web\"]}", Map.class));
    assertFullAuthenticationInfo((AuthenticationInfo) c.result);
  }

  @Test
  void testReplaceUserPasswordWithRevokeForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> passwordService.replaceUserPassword("", "old", "new", true, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetPasswordPolicyStrengthScore() throws Exception {
    PasswordPolicy policy = MAPPER.readValue("{\"minLength\":8,\"lowercase\":true,\"uppercase\":true,"
        + "\"number\":true,\"nonAlphanumeric\":true,\"strengthScoreEnabled\":true,\"strengthScore\":3}",
        PasswordPolicy.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(policy).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      PasswordPolicy response = passwordService.getPasswordPolicy();
      Assertions.assertThat(response.getStrengthScoreEnabled()).isTrue();
      Assertions.assertThat(response.getStrengthScore()).isEqualTo(3);
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), eq(PasswordPolicy.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/password/policy");
  }
}
