package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_MASKED_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_MASKED_PHONE;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
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

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class OTPServiceImplTest {

  private OTPService otpService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.otpService =
        AuthenticationServiceBuilder.buildServices(client).getOtpService();
    this.userService = ManagementServiceBuilder.buildServices(client).getUserService();
  }

  @Test
  void testSignUp() {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signUp = otpService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, user);
      assertThat(signUp).isNotBlank().contains("*");
    }
  }

  @Test
  void signIn() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signIn = otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, null);
      assertThat(signIn).isNotBlank().contains("*");
    }
  }

  @Test
  void testSignUpOrIn() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signUpOrIn = otpService.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL);
      assertThat(signUpOrIn).isNotBlank().contains("*");
    }
  }

  @Test
  void testUpdateUserEmailForEmptyLoginId() {

    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> otpService.updateUserEmail("", MOCK_EMAIL, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyEmail() {

    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> otpService.updateUserEmail(MOCK_EMAIL, "", MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForInvalidEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> otpService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String updateUserEmail = otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, null);
      assertThat(updateUserEmail).isNotBlank().contains("*");
    }
  }

  @Test
  void testUpdateUserPhoneForLoginID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> otpService.updateUserPhone(DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForEmptyPhone() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, "", MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForInvalidPhone() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, "1234E", MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForInvalidMethod() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> otpService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedPhoneRes maskedPhoneRes = new MaskedPhoneRes(MOCK_MASKED_PHONE);
    doReturn(maskedPhoneRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String updateUserPhone =
          otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null);
      assertThat(updateUserPhone).isNotBlank().contains("X");
    }
  }

  @Test
  void testVerifyCode() {
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
        authenticationInfo = otpService.verifyCode(DeliveryMethod.EMAIL, MOCK_EMAIL, "somecode");
      }
    }

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

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    OTPTestUserResponse response = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    assertThat(response.getCode()).isNotBlank();
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, response.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleVoice() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().phone("+15555555555").verifiedPhone(true).build());
    OTPTestUserResponse response = userService.generateOtpForTestUser(loginId, DeliveryMethod.VOICE);
    assertThat(response.getCode()).isNotBlank();
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.VOICE, loginId, response.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdateEmail() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    OTPTestUserResponse response = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    assertThat(response.getCode()).isNotBlank();
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, response.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    OTPTestUserResponse response2 = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    assertThat(response.getCode()).isNotBlank();
    otpService.updateUserEmail(loginId, loginId + "1@descope.com", authInfo.getRefreshToken().getJwt(), null);
    authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, response2.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdatePhone() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().phone(MOCK_PHONE).build());
    OTPTestUserResponse response = userService.generateOtpForTestUser(loginId, DeliveryMethod.SMS);
    assertThat(response.getCode()).isNotBlank();
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.SMS, loginId, response.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    OTPTestUserResponse response2 = userService.generateOtpForTestUser(loginId, DeliveryMethod.SMS);
    assertThat(response.getCode()).isNotBlank();
    otpService.updateUserPhone(
        DeliveryMethod.SMS, loginId, "+1-555-555-5556", authInfo.getRefreshToken().getJwt(), null);
    authInfo = otpService.verifyCode(DeliveryMethod.SMS, loginId, response2.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdatePhoneVoice() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().phone(MOCK_PHONE).build());
    OTPTestUserResponse response = userService.generateOtpForTestUser(loginId, DeliveryMethod.VOICE);
    assertThat(response.getCode()).isNotBlank();
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.VOICE, loginId, response.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    OTPTestUserResponse response2 = userService.generateOtpForTestUser(loginId, DeliveryMethod.VOICE);
    assertThat(response.getCode()).isNotBlank();
    otpService.updateUserPhone(
        DeliveryMethod.VOICE, loginId, "+1-555-555-5556", authInfo.getRefreshToken().getJwt(), null);
    authInfo = otpService.verifyCode(DeliveryMethod.VOICE, loginId, response2.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }
}
