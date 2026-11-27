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
import static com.descope.sdk.TestUtils.UPDATE_MOCK_EMAIL;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.otp.AuthenticationVerifyRequestBody;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
import com.descope.model.otp.SignInRequest;
import com.descope.model.otp.SignUpRequest;
import com.descope.model.otp.UpdateEmailRequestBody;
import com.descope.model.otp.UpdatePhoneRequestBody;
import com.descope.model.otp.response.OTPResponse;
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
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
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

  /** Runs the call against a mocked proxy returning the response, capturing header, URI and body. */
  @SuppressWarnings("unchecked")
  private static <R> R runCaptured(Object response, AtomicReference<String> header, ArgumentCaptor<URI> uri,
      ArgumentCaptor<Object> body, Supplier<R> call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(uri.capture(), body.capture(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class, CALLS_REAL_METHODS)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        return call.get();
      }
    }
  }

  private static OTPSignInOptions mockSignInOptions() {
    return OTPSignInOptions.builder()
        .providerId("someProvider")
        .templateId("someTemplate")
        .ssoAppId("someSsoApp")
        .useRecovery(true)
        .build();
  }

  private static OTPUpdateOptions mockOtpUpdateOptions() {
    return OTPUpdateOptions.builder()
        .providerId("someProvider")
        .templateId("someTemplate")
        .locale("en")
        .failOnConflict(true)
        .mfa(true)
        .build();
  }

  private static void assertSignInOptionsSent(SignInRequest req) {
    assertEquals("someProvider", req.getProviderId());
    assertEquals("someTemplate", req.getTemplateId());
    assertEquals("someSsoApp", req.getSsoAppId());
    assertEquals(true, req.getUseRecovery());
  }

  @Test
  void testSignInWithOTPOptions() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    OTPResponse res = runCaptured(new OTPResponse(null, MOCK_MASKED_PHONE, 3), header, uri, body,
        () -> otpService.signIn(DeliveryMethod.IM, MOCK_PHONE, null, null, mockSignInOptions()));
    assertEquals(MOCK_MASKED_PHONE, res.getMaskedPhone());
    assertEquals(3, res.getVerificationAttempts());
    assertEquals("/v1/auth/otp/signin/im", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID, header.get());
    SignInRequest req = (SignInRequest) body.getValue();
    assertEquals(MOCK_PHONE, req.getLoginId());
    assertSignInOptionsSent(req);
  }

  @Test
  void testSignInWithOTPOptionsMfaSendsRefreshToken() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    LoginOptions loginOptions = LoginOptions.builder().mfa(true).build();
    OTPResponse res = runCaptured(new OTPResponse(MOCK_MASKED_EMAIL, null, 3), header, uri, body,
        () -> otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, loginOptions, MOCK_REFRESH_TOKEN,
            mockSignInOptions()));
    assertEquals(MOCK_MASKED_EMAIL, res.getMaskedEmail());
    assertEquals("/v1/auth/otp/signin/email", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, header.get());
    assertEquals(loginOptions, ((SignInRequest) body.getValue()).getLoginOptions());
  }

  @Test
  void testSignInWithOTPOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.EMAIL, "", null, null, mockSignInOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithOTPOptionsMfaWithoutRefreshToken() {
    LoginOptions loginOptions = LoginOptions.builder().mfa(true).build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, loginOptions, null, mockSignInOptions()));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInByIM() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    String masked = runCaptured(new MaskedPhoneRes(MOCK_MASKED_PHONE), header, uri, body,
        () -> otpService.signIn(DeliveryMethod.IM, MOCK_PHONE, null));
    assertEquals(MOCK_MASKED_PHONE, masked);
    assertEquals("/v1/auth/otp/signin/im", uri.getValue().getPath());
    SignInRequest req = (SignInRequest) body.getValue();
    assertNull(req.getProviderId());
    assertNull(req.getUseRecovery());
  }

  @Test
  void testSignUpOrInWithOTPOptions() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    OTPResponse res = runCaptured(new OTPResponse(MOCK_MASKED_EMAIL, null, 2), header, uri, body,
        () -> otpService.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, mockSignInOptions()));
    assertEquals(MOCK_MASKED_EMAIL, res.getMaskedEmail());
    assertEquals(2, res.getVerificationAttempts());
    assertEquals("/v1/auth/otp/signup-in/email", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID, header.get());
    SignInRequest req = (SignInRequest) body.getValue();
    assertEquals(MOCK_EMAIL, req.getLoginId());
    assertNull(req.getLoginOptions());
    assertSignInOptionsSent(req);
  }

  @Test
  void testSignUpOrInWithOTPOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.EMAIL, "", mockSignInOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOTPOptions() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    User user = User.builder().name("someName").phone(MOCK_PHONE).build();
    SignUpOptions signUpOptions = SignUpOptions.builder().templateOptions(mapOf("key", "value")).build();
    OTPSignUpOptions otpSignUpOptions =
        OTPSignUpOptions.builder().providerId("someProvider").templateId("someTemplate").build();
    OTPResponse res = runCaptured(new OTPResponse(null, MOCK_MASKED_PHONE, 3), header, uri, body,
        () -> otpService.signUp(DeliveryMethod.IM, MOCK_PHONE, user, signUpOptions, otpSignUpOptions));
    assertEquals(MOCK_MASKED_PHONE, res.getMaskedPhone());
    assertEquals(3, res.getVerificationAttempts());
    assertEquals("/v1/auth/otp/signup/im", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID, header.get());
    SignUpRequest req = (SignUpRequest) body.getValue();
    assertEquals(MOCK_PHONE, req.getLoginId());
    assertEquals(MOCK_PHONE, req.getPhone());
    assertNull(req.getWhatsApp());
    assertEquals(user, req.getUser());
    assertEquals(signUpOptions, req.getLoginOptions());
    assertEquals("someProvider", req.getProviderId());
    assertEquals("someTemplate", req.getTemplateId());
  }

  @Test
  void testSignUpWithOTPOptionsForInvalidPhone() {
    User user = User.builder().phone("1234E").build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.IM, MOCK_PHONE, user, null, new OTPSignUpOptions()));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOTPOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.EMAIL, "", null, null, new OTPSignUpOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithOTPOptions() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    OTPResponse res = runCaptured(new OTPResponse(MOCK_MASKED_EMAIL, null, 3), header, uri, body,
        () -> otpService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_REFRESH_TOKEN,
            new UpdateOptions(true, true), mapOf("key", "value"), mockOtpUpdateOptions()));
    assertEquals(MOCK_MASKED_EMAIL, res.getMaskedEmail());
    assertEquals(3, res.getVerificationAttempts());
    assertEquals("/v1/auth/otp/update/email", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, header.get());
    UpdateEmailRequestBody req = (UpdateEmailRequestBody) body.getValue();
    assertEquals(MOCK_EMAIL, req.getLoginId());
    assertEquals(UPDATE_MOCK_EMAIL, req.getEmail());
    assertTrue(req.isAddToLoginIds());
    assertTrue(req.isOnMergeUseExisting());
    assertEquals(mapOf("key", "value"), req.getTemplateOptions());
    assertEquals("someProvider", req.getProviderId());
    assertEquals("someTemplate", req.getTemplateId());
    assertEquals("en", req.getLocale());
    assertEquals(true, req.getFailOnConflict());
    assertEquals(true, req.getMfa());
  }

  @Test
  void testUpdateUserEmailWithOTPOptionsForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail("", MOCK_EMAIL, MOCK_REFRESH_TOKEN, null, null, mockOtpUpdateOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, "", MOCK_REFRESH_TOKEN, null, null, mockOtpUpdateOptions()));
    assertEquals("The Email argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, "", null, null, mockOtpUpdateOptions()));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneWithOTPOptions() {
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    OTPResponse res = runCaptured(new OTPResponse(null, MOCK_MASKED_PHONE, 3), header, uri, body,
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN,
            new UpdateOptions(true, false), mapOf("key", "value"), mockOtpUpdateOptions()));
    assertEquals(MOCK_MASKED_PHONE, res.getMaskedPhone());
    assertEquals(3, res.getVerificationAttempts());
    assertEquals("/v1/auth/otp/update/phone/im", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, header.get());
    UpdatePhoneRequestBody req = (UpdatePhoneRequestBody) body.getValue();
    assertEquals(MOCK_EMAIL, req.getLoginId());
    assertEquals(MOCK_PHONE, req.getPhone());
    assertTrue(req.isAddToLoginIds());
    assertEquals(mapOf("key", "value"), req.getTemplateOptions());
    assertEquals("someProvider", req.getProviderId());
    assertEquals("someTemplate", req.getTemplateId());
    assertEquals("en", req.getLocale());
    assertEquals(true, req.getFailOnConflict());
    assertEquals(true, req.getMfa());
  }

  @Test
  void testUpdateUserPhoneWithOTPOptionsForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null,
            null, mockOtpUpdateOptions()));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.IM, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null,
            null, mockOtpUpdateOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, "1234E", MOCK_REFRESH_TOKEN, null,
            null, mockOtpUpdateOptions()));
    assertEquals("The Phone argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, "", null,
            null, mockOtpUpdateOptions()));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testVerifyCodeByIMReadsAllResponseFields() {
    JWTResponse jwtResponse = new JWTResponse(MOCK_JWT_RESPONSE.getSessionJwt(), MOCK_JWT_RESPONSE.getRefreshJwt(),
        MOCK_JWT_RESPONSE.getCookieDomain(), MOCK_JWT_RESPONSE.getCookiePath(), MOCK_JWT_RESPONSE.getCookieMaxAge(),
        MOCK_JWT_RESPONSE.getCookieExpiration(), MOCK_JWT_RESPONSE.getUser(), true);
    jwtResponse.setSessionExpiration(600);
    jwtResponse.setExternalToken("someExternalToken");
    jwtResponse.setClaims(Collections.singletonMap("claim", "value"));
    jwtResponse.setTenantSSOID("someTenant");
    jwtResponse.setTrustedDeviceJwt("someTrustedDeviceJwt");
    jwtResponse.setNextRefreshSeconds(60);
    jwtResponse.setCookieName("DSR");
    jwtResponse.setSessionCookieName("DS");
    jwtResponse.setSessionCookieDomain("example.com");
    jwtResponse.setUnsavedSSO(true);
    jwtResponse.setKnownDeviceJwt("someKnownDeviceJwt");
    jwtResponse.setUserCreationDeferred(true);
    jwtResponse.setExternalPasskey(true);
    AtomicReference<String> header = new AtomicReference<>();
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    AuthenticationInfo info = runCaptured(jwtResponse, header, uri, body,
        () -> otpService.verifyCode(DeliveryMethod.IM, MOCK_PHONE, "123456"));
    assertNotNull(info.getToken());
    assertEquals("/v1/auth/otp/verify/im", uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID, header.get());
    AuthenticationVerifyRequestBody req = (AuthenticationVerifyRequestBody) body.getValue();
    assertEquals(MOCK_PHONE, req.getLoginId());
    assertEquals("123456", req.getCode());
    assertNotNull(info.getRefreshToken());
    assertEquals(600, info.getSessionExpiration());
    assertEquals("someExternalToken", info.getExternalToken());
    assertEquals(Collections.singletonMap("claim", "value"), info.getClaims());
    assertEquals("someTenant", info.getTenantSSOID());
    assertEquals("someTrustedDeviceJwt", info.getTrustedDeviceJwt());
    assertEquals(60, info.getNextRefreshSeconds());
    assertEquals("DSR", info.getCookieName());
    assertEquals("DS", info.getSessionCookieName());
    assertEquals("example.com", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("someKnownDeviceJwt", info.getKnownDeviceJwt());
    assertEquals(true, info.getUserCreationDeferred());
    assertEquals(true, info.getExternalPasskey());
  }
}
