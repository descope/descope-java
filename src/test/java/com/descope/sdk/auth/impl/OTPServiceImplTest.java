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
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

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
import com.descope.model.otp.OTPResponse;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
import com.descope.model.otp.UpdateEmailRequestBody;
import com.descope.model.otp.UpdatePhoneRequestBody;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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

  @Test
  void testSignInWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().locale("en").build();
    OTPSignInOptions options = new OTPSignInOptions("provider", "app", true);
    Sent sent = send(new OTPResponse(MOCK_MASKED_EMAIL, null, 3), OTPResponse.class,
        s -> s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, loginOptions, null, options));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signin/email");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("loginId")).isEqualTo(MOCK_EMAIL);
    assertThat(body.get("loginOptions")).isEqualTo(specLoginOptions(loginOptions));
    assertThat(body.get("providerId")).isEqualTo("provider");
    assertThat(body.get("ssoAppId")).isEqualTo("app");
    assertThat(body.get("useRecovery")).isEqualTo(true);
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_EMAIL);
    assertThat(res.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testSignInWithOptionsStepupSendsRefreshToken() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.signIn(DeliveryMethod.VOICE, MOCK_PHONE, loginOptions, MOCK_REFRESH_TOKEN, null));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signin/voice");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("providerId")).isNull();
    assertThat(body.get("ssoAppId")).isNull();
    assertThat(body.get("useRecovery")).isNull();
  }

  @Test
  void testSignInWithOptionsFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.EMAIL, "", null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.WHATSAPP, MOCK_PHONE, null, null, null));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(null, MOCK_PHONE, null, null, null));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.SMS, MOCK_PHONE, LoginOptions.builder().mfa(true).build(), null,
            null));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInIM() {
    OTPSignInOptions options = new OTPSignInOptions("provider", "app", false);
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 2), OTPResponse.class,
        s -> s.signInIM(MOCK_PHONE, null, null, options));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signin/im");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("loginId")).isEqualTo(MOCK_PHONE);
    assertThat(body.get("providerId")).isEqualTo("provider");
    assertThat(body.get("ssoAppId")).isEqualTo("app");
    assertThat(body.get("useRecovery")).isEqualTo(false);
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(2);
  }

  @Test
  void testSignInIMFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signInIM("", null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOptions() {
    User user = new User("someUserName", MOCK_EMAIL, MOCK_PHONE);
    SignUpOptions signUpOptions = SignUpOptions.builder().templateOptions(mapOf("k", "v")).build();
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.signUp(DeliveryMethod.SMS, MOCK_PHONE, user, signUpOptions, new OTPSignUpOptions("provider")));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signup/sms");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("loginId")).isEqualTo(MOCK_PHONE);
    assertThat(body.get("phone")).isEqualTo(MOCK_PHONE);
    assertThat(body.get("email")).isNull();
    assertThat(body.get("whatsApp")).isNull();
    assertThat(body.get("user")).isEqualTo(mapOf("name", "someUserName", "email", MOCK_EMAIL));
    assertThat(body.get("loginOptions")).isEqualTo(mapOf("templateOptions", mapOf("k", "v")));
    assertThat(body.get("providerId")).isEqualTo("provider");
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testSignUpWithOptionsEmail() {
    Sent sent = send(new OTPResponse(MOCK_MASKED_EMAIL, null, 3), OTPResponse.class,
        s -> s.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, null, null, null));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signup/email");
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("email")).isEqualTo(MOCK_EMAIL);
    assertThat(body.get("phone")).isNull();
    assertThat(body.get("providerId")).isNull();
  }

  @Test
  void testSignUpWithOptionsFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.EMAIL, "", null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.WHATSAPP, MOCK_PHONE, null, null, null));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.SMS, "abc", null, null, null));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpIM() {
    User user = new User("someUserName", MOCK_EMAIL, MOCK_PHONE);
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.signUpIM("someLoginId", user, null, new OTPSignUpOptions("provider")));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signup/im");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("loginId")).isEqualTo("someLoginId");
    assertThat(body.get("phone")).isEqualTo(MOCK_PHONE);
    assertThat(body.get("whatsApp")).isNull();
    assertThat(body.get("providerId")).isEqualTo("provider");
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testSignUpIMFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpIM("", null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpIM(MOCK_EMAIL, null, null, null));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().templateOptions(mapOf("k", "v")).build();
    OTPSignInOptions options = new OTPSignInOptions("provider", "app", true);
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.signUpOrIn(DeliveryMethod.VOICE, MOCK_PHONE, loginOptions, options));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signup-in/voice");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("loginId")).isEqualTo(MOCK_PHONE);
    assertThat(body.get("loginOptions")).isEqualTo(specLoginOptions(loginOptions));
    assertThat(body.get("providerId")).isEqualTo("provider");
    assertThat(body.get("ssoAppId")).isEqualTo("app");
    assertThat(body.get("useRecovery")).isEqualTo(true);
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testSignUpOrInWithOptionsFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.EMAIL, "", null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.WHATSAPP, MOCK_PHONE, null, null));
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInIM() {
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.signUpOrInIM(MOCK_PHONE, null, new OTPSignInOptions("provider", "app", true)));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/signup-in/im");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = asMap(sent.body);
    assertThat(body.get("loginId")).isEqualTo(MOCK_PHONE);
    assertThat(body.get("loginOptions")).isNull();
    assertThat(body.get("providerId")).isEqualTo("provider");
    assertThat(body.get("ssoAppId")).isEqualTo("app");
    assertThat(body.get("useRecovery")).isEqualTo(true);
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testSignUpOrInIMFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrInIM("", null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testVerifyCodeIM() {
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
        authenticationInfo = otpService.verifyCodeIM(MOCK_PHONE, "123456");
      }
      ArgumentCaptor<Supplier<String>> headerCaptor = headerCaptor();
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(headerCaptor.capture(), any()), atLeastOnce());
      assertThat(headerCaptor.getAllValues().get(0).get()).isEqualTo("Bearer " + PROJECT_ID);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<AuthenticationVerifyRequestBody> bodyCaptor =
        ArgumentCaptor.forClass(AuthenticationVerifyRequestBody.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(JWTResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/otp/verify/im");
    assertThat(bodyCaptor.getValue().getLoginId()).isEqualTo(MOCK_PHONE);
    assertThat(bodyCaptor.getValue().getCode()).isEqualTo("123456");
    assertThat(bodyCaptor.getValue().getLoginOptions()).isNull();
    assertThat(authenticationInfo.getToken().getJwt()).isNotBlank();
    assertThat(authenticationInfo.getRefreshToken().getJwt()).isNotBlank();
    assertThat(authenticationInfo.getUser().getUserId()).isNotBlank();
  }

  @Test
  void testVerifyCodeIMFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.verifyCodeIM("", "123456"));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithOptions() {
    UpdateOptions updateOptions = new UpdateOptions(true, true);
    OTPUpdateOptions options = new OTPUpdateOptions("provider", "en", true, true);
    Sent sent = send(new OTPResponse(MOCK_MASKED_EMAIL, null, 3), OTPResponse.class,
        s -> s.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, updateOptions, mapOf("k", "v"),
            options));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/update/email");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    UpdateEmailRequestBody body = (UpdateEmailRequestBody) sent.body;
    assertThat(body.getEmail()).isEqualTo(MOCK_EMAIL);
    assertThat(body.getLoginId()).isEqualTo(MOCK_EMAIL);
    assertThat(body.isAddToLoginIds()).isTrue();
    assertThat(body.isOnMergeUseExisting()).isTrue();
    assertThat(body.getTemplateOptions()).containsEntry("k", "v");
    assertThat(body.getProviderId()).isEqualTo("provider");
    assertThat(body.getLocale()).isEqualTo("en");
    assertThat(body.getFailOnConflict()).isTrue();
    assertThat(body.getMfa()).isTrue();
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_EMAIL);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testUpdateUserEmailWithOptionsOmitsUnset() {
    Sent sent = send(new OTPResponse(MOCK_MASKED_EMAIL, null, 3), OTPResponse.class,
        s -> s.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, null, null, null));
    UpdateEmailRequestBody body = (UpdateEmailRequestBody) sent.body;
    assertThat(body.getProviderId()).isNull();
    assertThat(body.getLocale()).isNull();
    assertThat(body.getFailOnConflict()).isNull();
    assertThat(body.getMfa()).isNull();
  }

  @Test
  void testUpdateUserEmailWithOptionsFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail("", MOCK_EMAIL, MOCK_REFRESH_TOKEN, null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null, null, null));
    assertEquals("The Email argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, "", null, null, null));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneWithOptions() {
    OTPUpdateOptions options = new OTPUpdateOptions("provider", "en", false, true);
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.updateUserPhone(DeliveryMethod.VOICE, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null,
            mapOf("k", "v"), options));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/update/phone/voice");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    UpdatePhoneRequestBody body = (UpdatePhoneRequestBody) sent.body;
    assertThat(body.getPhone()).isEqualTo(MOCK_PHONE);
    assertThat(body.getLoginId()).isEqualTo(MOCK_EMAIL);
    assertThat(body.isAddToLoginIds()).isFalse();
    assertThat(body.getTemplateOptions()).containsEntry("k", "v");
    assertThat(body.getProviderId()).isEqualTo("provider");
    assertThat(body.getLocale()).isEqualTo("en");
    assertThat(body.getFailOnConflict()).isFalse();
    assertThat(body.getMfa()).isTrue();
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testUpdateUserPhoneWithOptionsFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.WHATSAPP, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null,
            null, null));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null, null,
            null));
    assertEquals("The Phone argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, "", null, null, null));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneIM() {
    OTPUpdateOptions options = new OTPUpdateOptions("provider", "en", true, false);
    Sent sent = send(new OTPResponse(null, MOCK_MASKED_PHONE, 3), OTPResponse.class,
        s -> s.updateUserPhoneIM(MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, new UpdateOptions(true, false),
            null, options));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/otp/update/phone/im");
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    UpdatePhoneRequestBody body = (UpdatePhoneRequestBody) sent.body;
    assertThat(body.getPhone()).isEqualTo(MOCK_PHONE);
    assertThat(body.getLoginId()).isEqualTo(MOCK_EMAIL);
    assertThat(body.isAddToLoginIds()).isTrue();
    assertThat(body.getProviderId()).isEqualTo("provider");
    assertThat(body.getLocale()).isEqualTo("en");
    assertThat(body.getFailOnConflict()).isTrue();
    assertThat(body.getMfa()).isFalse();
    OTPResponse res = (OTPResponse) sent.result;
    assertThat(res.getMasked()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(res.getVerificationAttempts()).isEqualTo(3);
  }

  @Test
  void testUpdateUserPhoneIMFailures() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhoneIM("", MOCK_PHONE, MOCK_REFRESH_TOKEN, null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhoneIM(MOCK_EMAIL, "", MOCK_REFRESH_TOKEN, null, null, null));
    assertEquals("The Phone argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhoneIM(MOCK_EMAIL, MOCK_PHONE, "", null, null, null));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  private static class Sent {
    URI uri;
    Object body;
    String authorization;
    Object result;
  }

  private Sent send(Object response, Class<?> responseClass, Function<OTPService, Object> call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    Sent sent = new Sent();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      sent.result = call.apply(otpService);
      ArgumentCaptor<Supplier<String>> headerCaptor = headerCaptor();
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(headerCaptor.capture(), any()));
      sent.authorization = headerCaptor.getValue().get();
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(responseClass));
    sent.uri = uriCaptor.getValue();
    sent.body = bodyCaptor.getValue();
    return sent;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object body) {
    return (Map<String, Object>) body;
  }

  private static Map<String, Object> specLoginOptions(LoginOptions o) {
    Map<String, Object> m = new HashMap<>();
    m.put("stepup", o.isStepup());
    m.put("mfa", o.isMfa());
    m.put("revokeOtherSessions", o.isRevokeOtherSessions());
    if (o.getTemplateOptions() != null) {
      m.put("templateOptions", o.getTemplateOptions());
    }
    if (o.getLocale() != null) {
      m.put("locale", o.getLocale());
    }
    return m;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static ArgumentCaptor<Supplier<String>> headerCaptor() {
    return ArgumentCaptor.forClass((Class) Supplier.class);
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
