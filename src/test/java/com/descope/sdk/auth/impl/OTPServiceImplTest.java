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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.response.Masked;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
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

  private static final String AUTH_MANAGEMENT_KEY = "some-auth-management-key";
  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  /** What a single auth call sent, and what it returned. */
  private static final class Sent<T> {
    private String authorization;
    private URI uri;
    private Map<String, Object> body;
    private T result;
  }

  @SuppressWarnings("unchecked")
  private static <T> Sent<T> send(Object response, Function<OTPService, T> call) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .authManagementKey(AUTH_MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    OTPService service = AuthenticationServiceBuilder.buildServices(client).getOtpService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    Sent<T> sent = new Sent<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.authorization = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      sent.result = call.apply(service);
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), any());
    sent.uri = uri.getValue();
    sent.body = MAPPER.convertValue(body.getValue(), Map.class);
    return sent;
  }

  private static Map<String, Object> json(String json) throws Exception {
    return MAPPER.readValue(json.replace('\'', '"'), Map.class);
  }

  private static LoginOptions fullLoginOptions() {
    return LoginOptions.builder()
        .stepup(true)
        .customClaims(mapOf("claim", "value"))
        .templateOptions(mapOf("option", "value"))
        .revokeOtherSessions(true)
        .revokeOtherSessionsTypes(new String[] {"web"})
        .jwt("not-sent")
        .locale("en")
        .templateId("login-template")
        .ssoAppId("sso-app")
        .pkceChallenge("Y2hhbGxlbmdl")
        .tenantId("tenant")
        .build();
  }

  private static OTPSignInOptions fullSignInOptions() {
    return OTPSignInOptions.builder().providerId("provider").templateId("template").ssoAppId("sso-app")
        .useRecovery(true).build();
  }

  private static OTPUpdateOptions fullUpdateOptions() {
    return OTPUpdateOptions.builder().providerId("provider").templateId("template").locale("en")
        .failOnConflict(true).mfa(true).build();
  }

  private static final String SIGN_IN_BODY = "{'loginId':'%s','loginOptions':{'stepup':true,'mfa':false,"
      + "'customClaims':{'claim':'value'},'templateOptions':{'option':'value'},'revokeOtherSessions':true,"
      + "'revokeOtherSessionsTypes':['web'],'locale':'en','templateId':'login-template','ssoAppId':'sso-app',"
      + "'pkceChallenge':'Y2hhbGxlbmdl','tenantId':'tenant'},'providerId':'provider','templateId':'template',"
      + "'ssoAppId':'sso-app','useRecovery':true}";

  @Test
  void testSignInWithOptions() throws Exception {
    Sent<Masked> sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL, 2),
        s -> s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, fullLoginOptions(), MOCK_REFRESH_TOKEN, fullSignInOptions()));
    assertEquals("/v1/auth/otp/signin/email", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json(String.format(SIGN_IN_BODY, MOCK_EMAIL)), sent.body);
    assertEquals(MOCK_MASKED_EMAIL, sent.result.getMasked());
    assertEquals(2, sent.result.getVerificationAttempts());
  }

  @Test
  void testSignInWithOptionsOverIm() throws Exception {
    Sent<Masked> sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE, 1),
        s -> s.signIn(DeliveryMethod.IM, MOCK_PHONE, null, null, null));
    assertEquals("/v1/auth/otp/signin/im", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json("{'loginId':'" + MOCK_PHONE + "'}"), sent.body);
    assertEquals(MOCK_MASKED_PHONE, sent.result.getMasked());
    assertEquals(1, sent.result.getVerificationAttempts());
  }

  @Test
  void testSignInWithOptionsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.SMS, "", null, null, null)).getMessage());
    assertEquals("The DeliveryMethod argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signIn(null, MOCK_PHONE, null, null, null)).getMessage());
    assertEquals("The refreshToken argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.SMS, MOCK_PHONE, fullLoginOptions(), "", null)).getMessage());
  }

  @Test
  void testSignUpOrInWithOptions() throws Exception {
    Sent<Masked> sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE, 3),
        s -> s.signUpOrIn(DeliveryMethod.WHATSAPP, MOCK_PHONE, fullLoginOptions(), MOCK_REFRESH_TOKEN,
            fullSignInOptions()));
    assertEquals("/v1/auth/otp/signup-in/whatsapp", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json(String.format(SIGN_IN_BODY, MOCK_PHONE)), sent.body);
    assertEquals(MOCK_MASKED_PHONE, sent.result.getMasked());
    assertEquals(3, sent.result.getVerificationAttempts());
  }

  @Test
  void testSignUpOrInWithOptionsWithoutLoginOptions() throws Exception {
    Sent<Masked> sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL, 1),
        s -> s.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, null, null, null));
    assertEquals("/v1/auth/otp/signup-in/email", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json("{'loginId':'" + MOCK_EMAIL + "'}"), sent.body);
  }

  @Test
  void testSignUpOrInWithOptionsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.SMS, "", null, null, null)).getMessage());
    assertEquals("The DeliveryMethod argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(null, MOCK_PHONE, null, null, null)).getMessage());
    assertEquals("The refreshToken argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.SMS, MOCK_PHONE, fullLoginOptions(), null, null)).getMessage());
  }

  private static User fullUser(String loginId) {
    return User.builder().name("name").email(MOCK_EMAIL).phone(MOCK_PHONE).givenName("given").middleName("middle")
        .familyName("family").loginId(loginId).recoveryEmail("recovery@domain.com").recoveryPhone("+15555550000")
        .build();
  }

  private static SignUpOptions fullSignUpOptions() {
    return SignUpOptions.builder().customClaims(mapOf("claim", "value")).templateOptions(mapOf("option", "value"))
        .locale("en").pkceChallenge("Y2hhbGxlbmdl").templateId("signup-template").tenantId("tenant").build();
  }

  private static final String SIGN_UP_BODY = "{%s,'loginId':'%s','user':{'name':'name','email':'" + MOCK_EMAIL
      + "','phone':'" + MOCK_PHONE + "','givenName':'given','middleName':'middle','familyName':'family',"
      + "'loginId':'%s','recoveryEmail':'recovery@domain.com','recoveryPhone':'+15555550000'},"
      + "'providerId':'provider','templateId':'template','loginOptions':{'customClaims':{'claim':'value'},"
      + "'templateOptions':{'option':'value'},'locale':'en','pkceChallenge':'Y2hhbGxlbmdl',"
      + "'templateId':'signup-template','tenantId':'tenant'}}";

  @Test
  void testSignUpWithOptionsEmail() throws Exception {
    Sent<Masked> sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL, 1),
        s -> s.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, fullUser(MOCK_EMAIL), fullSignUpOptions(),
            OTPSignUpOptions.builder().providerId("provider").templateId("template").build()));
    assertEquals("/v1/auth/otp/signup/email", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json(String.format(SIGN_UP_BODY, "'email':'" + MOCK_EMAIL + "'", MOCK_EMAIL, MOCK_EMAIL)),
        sent.body);
    assertEquals(MOCK_MASKED_EMAIL, sent.result.getMasked());
    assertEquals(1, sent.result.getVerificationAttempts());
  }

  @Test
  void testSignUpWithOptionsWhatsappSendsPhone() throws Exception {
    Sent<Masked> sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE, 4),
        s -> s.signUp(DeliveryMethod.WHATSAPP, "someLoginId", fullUser("someLoginId"), fullSignUpOptions(),
            OTPSignUpOptions.builder().providerId("provider").templateId("template").build()));
    assertEquals("/v1/auth/otp/signup/whatsapp", sent.uri.getPath());
    assertEquals(json(String.format(SIGN_UP_BODY, "'phone':'" + MOCK_PHONE + "'", "someLoginId", "someLoginId")),
        sent.body);
    assertEquals(MOCK_MASKED_PHONE, sent.result.getMasked());
    assertEquals(4, sent.result.getVerificationAttempts());
  }

  @Test
  void testSignUpWithOptionsImUsesLoginIdAsPhone() throws Exception {
    Sent<Masked> sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE, 1),
        s -> s.signUp(DeliveryMethod.IM, MOCK_PHONE, null, null, null));
    assertEquals("/v1/auth/otp/signup/im", sent.uri.getPath());
    assertEquals(json("{'phone':'" + MOCK_PHONE + "','loginId':'" + MOCK_PHONE + "','user':{}}"), sent.body);
  }

  @Test
  void testSignUpWithOptionsInvalidArguments() {
    assertEquals("The DeliveryMethod argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUp(null, MOCK_EMAIL, null, null, null)).getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.EMAIL, "", null, null, null)).getMessage());
    assertEquals("The user.email argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.EMAIL, "not-an-email", null, null, null)).getMessage());
    assertEquals("The user.phone argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.IM, "not-a-phone", null, null, null)).getMessage());
  }

  @Test
  void testUpdateUserEmailWithOptions() throws Exception {
    Sent<Masked> sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL, 2),
        s -> s.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_REFRESH_TOKEN, new UpdateOptions(true, false),
            mapOf("option", "value"), fullUpdateOptions()));
    assertEquals("/v1/auth/otp/update/email", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json("{'email':'" + UPDATE_MOCK_EMAIL + "','loginId':'" + MOCK_EMAIL + "','addToLoginIDs':true,"
        + "'onMergeUseExisting':false,'templateOptions':{'option':'value'},'providerId':'provider',"
        + "'templateId':'template','locale':'en','failOnConflict':true,'mfa':true}"), sent.body);
    assertEquals(MOCK_MASKED_EMAIL, sent.result.getMasked());
    assertEquals(2, sent.result.getVerificationAttempts());
  }

  @Test
  void testUpdateUserEmailWithoutOptionsSendsSameBody() throws Exception {
    Sent<String> sent = send(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        s -> s.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_REFRESH_TOKEN, null));
    assertEquals(json("{'email':'" + UPDATE_MOCK_EMAIL + "','loginId':'" + MOCK_EMAIL + "','addToLoginIDs':false,"
        + "'onMergeUseExisting':false}"), sent.body);
    assertEquals(MOCK_MASKED_EMAIL, sent.result);
  }

  @Test
  void testUpdateUserEmailWithOptionsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail("", MOCK_EMAIL, MOCK_REFRESH_TOKEN, null, null, null)).getMessage());
    assertEquals("The Email argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null, null, null)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, "", null, null, null)).getMessage());
  }

  @Test
  void testUpdateUserPhoneWithOptions() throws Exception {
    Sent<Masked> sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE, 2),
        s -> s.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN,
            new UpdateOptions(false, true), mapOf("option", "value"), fullUpdateOptions()));
    assertEquals("/v1/auth/otp/update/phone/im", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json("{'phone':'" + MOCK_PHONE + "','loginId':'" + MOCK_EMAIL + "','addToLoginIDs':false,"
        + "'onMergeUseExisting':true,'templateOptions':{'option':'value'},'providerId':'provider',"
        + "'templateId':'template','locale':'en','failOnConflict':true,'mfa':true}"), sent.body);
    assertEquals(MOCK_MASKED_PHONE, sent.result.getMasked());
    assertEquals(2, sent.result.getVerificationAttempts());
  }

  @Test
  void testUpdateUserPhoneWithOptionsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null, null, null))
        .getMessage());
    assertEquals("The Phone argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null, null, null))
        .getMessage());
    assertEquals("The Method argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null, null,
            null)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, "", null, null, null))
        .getMessage());
  }

  @Test
  void testSignUpOverImSendsPhone() throws Exception {
    Sent<String> sent = send(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        s -> s.signUp(DeliveryMethod.IM, MOCK_PHONE, User.builder().phone(MOCK_PHONE).build()));
    assertEquals("/v1/auth/otp/signup/im", sent.uri.getPath());
    assertEquals(MOCK_PHONE, sent.body.get("phone"));
    assertThat(sent.body).doesNotContainKeys("email", "whatsApp");
    assertEquals(MOCK_MASKED_PHONE, sent.result);
  }

  @Test
  void testVerifyCodeOverImReadsFullResponse() throws Exception {
    JWTResponse response = MAPPER.readValue(("{'sessionJwt':'s','refreshJwt':'r','cookieDomain':'d','cookiePath':'/',"
        + "'cookieMaxAge':1,'cookieExpiration':2,'firstSeen':true,'sessionExpiration':3,'externalToken':'e',"
        + "'claims':{'c':'v'},'tenantSSOID':'t','trustedDeviceJwt':'td','nextRefreshSeconds':4,'cookieName':'cn',"
        + "'sessionCookieName':'scn','sessionCookieDomain':'scd','unsavedSSO':true,'knownDeviceJwt':'kd',"
        + "'ssoAuthenticationOnly':true,'userCreationDeferred':false,'idpResponse':{'samlResponse':'sr',"
        + "'samlGeneratedUser':'sgu','samlGeneratedRoles':'sgr','oidcResponse':'or','oidcGeneratedUser':'ogu',"
        + "'oidcGeneratedRoles':'ogr'},'user':{'userId':'u','TOTP':true,'SAML':true,'OAuth':{'google':true},"
        + "'SCIM':true,'OIDC':false,'externalIds':['x'],'webauthn':true,'password':false,'editable':true,"
        + "'push':false,'permissions':['p'],'consentExpiration':5,'recoveryEmail':'re','verifiedRecoveryEmail':true,"
        + "'recoveryPhone':'rp','verifiedRecoveryPhone':false,'modifiedTime':6,'roleIds':['ri'],"
        + "'recoveryCodes':true,'dependent':false,'userFamilies':[{'familyId':'f','roleNames':['fr'],"
        + "'permissions':['fp'],'familyScopedAttributes':{'a':'b'}}],'scimTenantId':'st','samlTenantId':'sat',"
        + "'oidcTenantId':'ot','consentTenantId':'ct','lastLoginTime':7,"
        + "'userTenants':[{'tenantId':'t1','permissions':['tp'],'roleIds':['tr']}]}}").replace('\'', '"'),
        JWTResponse.class);
    Sent<AuthenticationInfo> sent;
    try (MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
      jwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      sent = send(response, s -> s.verifyCode(DeliveryMethod.IM, MOCK_PHONE, "123456"));
    }
    assertEquals("/v1/auth/otp/verify/im", sent.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY, sent.authorization);
    assertEquals(json("{'loginId':'" + MOCK_PHONE + "','code':'123456'}"), sent.body);
    AuthenticationInfo info = sent.result;
    assertThat(info).hasNoNullFieldsOrProperties();
    assertEquals("d", info.getCookieDomain());
    assertEquals(2, info.getCookieExpiration());
    assertEquals("t", info.getTenantSSOID());
    assertEquals(4, info.getNextRefreshSeconds());
    assertEquals(false, info.getUserCreationDeferred());
    assertThat(info.getIdpResponse()).hasNoNullFieldsOrPropertiesExcept("idpGroups", "idpSAMLAttributes",
        "idpOIDCClaims");
    UserResponse user = info.getUser();
    assertEquals(true, user.getTOTP());
    assertEquals(true, user.getSAML());
    assertEquals(mapOf("google", true), user.getOAuth());
    assertEquals(true, user.getSCIM());
    assertEquals(false, user.getOIDC());
    assertEquals(7L, user.getLastLoginTime());
    assertEquals(Arrays.asList("ri"), user.getRoleIds());
    assertEquals("f", user.getUserFamilies().get(0).getFamilyId());
    assertEquals(Arrays.asList("tp"), user.getUserTenants().get(0).getPermissions());
    assertEquals(Arrays.asList("tr"), user.getUserTenants().get(0).getRoleIds());
  }

  @Test
  void testAssociatedTenantResponseFieldsAreNotSerialized() {
    AssociatedTenant tenant = new AssociatedTenant("t1", null, null, Arrays.asList("p"), Arrays.asList("r"));
    assertEquals(mapOf("tenantId", "t1"), MAPPER.convertValue(tenant, Map.class));
  }
}
