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
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.PasswordService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import java.security.Key;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class PasswordServiceImplTest {

  private PasswordService passwordService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getAuthParams();
    var client = TestUtils.getClient();
    this.passwordService =
        AuthenticationServiceBuilder.buildServices(client, authParams).getPasswordService();
    var mgmtParams = TestUtils.getManagementParams();
    this.userService = ManagementServiceBuilder.buildServices(client, mgmtParams).getUserService();
  }

  @Test
  void testSignupForSuccess() {
    User usr = new User("someUserName", MOCK_EMAIL, "+910000000000");

    var apiProxy = mock(ApiProxy.class);

    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(List.of(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    var provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));

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

    var apiProxy = mock(ApiProxy.class);

    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(List.of(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    var provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));

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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      passwordService.replaceUserPassword(MOCK_EMAIL, MOCK_PWD, MOCK_PWD);
      verify(apiProxy, times(1)).post(any(), any(), any());
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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
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
    var authInfo = passwordService.signUp(loginId, MOCK_USER, MOCK_PWD);
    var user = authInfo.getUser();
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
    passwordService.replaceUserPassword(loginId, MOCK_PWD, MOCK_PWD + "1");
    authInfo = passwordService.signIn(loginId, MOCK_PWD + "1");
    assertThat(authInfo.getRefreshToken().getJwt()).isNotBlank();
    passwordService.updateUserPassword(
        loginId, MOCK_PWD + "2", authInfo.getRefreshToken().getJwt());
    passwordService.signIn(loginId, MOCK_PWD + "2");
    userService.delete(loginId);
    var policy = passwordService.getPasswordPolicy();
    assertThat(policy.getMinLength()).isGreaterThan(7);
  }
}
