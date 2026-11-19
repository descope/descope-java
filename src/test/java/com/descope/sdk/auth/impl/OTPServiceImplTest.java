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
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.UserFamily;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
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

  /** Captures the Authorization header, URI and JSON body of the single request a call makes. */
  private static final class CapturedRequest {
    String authorization;
    String uri;
    JsonNode body;
    Class<?> responseClass;
  }

  @SuppressWarnings("unchecked")
  private static <T> CapturedRequest capture(Object response, Supplier<T> call, AtomicReference<T> result) {
    CapturedRequest captured = new CapturedRequest();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
         MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.authorization = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.isJWTRequired(any())).thenCallRealMethod();
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      result.set(call.get());
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), classCaptor.capture());
    captured.uri = uriCaptor.getValue().getPath();
    captured.body = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).valueToTree(bodyCaptor.getValue());
    captured.responseClass = classCaptor.getValue();
    return captured;
  }

  @Test
  void testSignInWithSignInOptions() {
    LoginOptions loginOptions = LoginOptions.builder()
        .ssoAppId("sso-app").pkceChallenge("Y2hhbGxlbmdl").tenantId("tenant-1").build();
    OTPSignInOptions signInOptions = OTPSignInOptions.builder()
        .providerId("provider-1").templateId("template-1").ssoAppId("sso-app").useRecovery(true).build();
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.signIn(DeliveryMethod.IM, MOCK_PHONE, loginOptions, null, signInOptions), result);

    assertThat(result.get()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(req.uri).isEqualTo("/v1/auth/otp/signin/im");
    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(req.responseClass).isEqualTo(MaskedPhoneRes.class);
    assertThat(req.body.get("loginId").asText()).isEqualTo(MOCK_PHONE);
    assertThat(req.body.get("providerId").asText()).isEqualTo("provider-1");
    assertThat(req.body.get("templateId").asText()).isEqualTo("template-1");
    assertThat(req.body.get("ssoAppId").asText()).isEqualTo("sso-app");
    assertThat(req.body.get("useRecovery").asBoolean()).isTrue();
    JsonNode options = req.body.get("loginOptions");
    assertThat(options.get("ssoAppId").asText()).isEqualTo("sso-app");
    assertThat(options.get("pkceChallenge").asText()).isEqualTo("Y2hhbGxlbmdl");
    assertThat(options.get("tenantId").asText()).isEqualTo("tenant-1");
  }

  @Test
  void testSignInWithoutSignInOptionsOmitsThem() {
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, null), result);

    assertThat(req.uri).isEqualTo("/v1/auth/otp/signin/email");
    assertThat(req.body.has("providerId")).isFalse();
    assertThat(req.body.has("templateId")).isFalse();
    assertThat(req.body.has("ssoAppId")).isFalse();
    assertThat(req.body.has("useRecovery")).isFalse();
    assertThat(req.body.has("loginOptions")).isFalse();
  }

  @Test
  void testSignInWithSignInOptionsStepUpSendsRefreshToken() {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, loginOptions, MOCK_REFRESH_TOKEN,
            OTPSignInOptions.builder().useRecovery(true).build()), result);

    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
  }

  @Test
  void testSignInWithSignInOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.IM, "", null, null, new OTPSignInOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithSignInOptionsStepUpWithoutRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.SMS, MOCK_PHONE, LoginOptions.builder().mfa(true).build(), null,
            new OTPSignInOptions()));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().tenantId("tenant-1").templateId("login-template").build();
    OTPSignInOptions signInOptions = OTPSignInOptions.builder()
        .providerId("provider-1").templateId("template-1").ssoAppId("sso-app").useRecovery(false).build();
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.signUpOrIn(DeliveryMethod.WHATSAPP, MOCK_PHONE, loginOptions, signInOptions), result);

    assertThat(result.get()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(req.uri).isEqualTo("/v1/auth/otp/signup-in/whatsapp");
    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(req.responseClass).isEqualTo(MaskedPhoneRes.class);
    assertThat(req.body.get("loginId").asText()).isEqualTo(MOCK_PHONE);
    assertThat(req.body.get("providerId").asText()).isEqualTo("provider-1");
    assertThat(req.body.get("templateId").asText()).isEqualTo("template-1");
    assertThat(req.body.get("ssoAppId").asText()).isEqualTo("sso-app");
    assertThat(req.body.get("useRecovery").asBoolean()).isFalse();
    assertThat(req.body.get("loginOptions").get("tenantId").asText()).isEqualTo("tenant-1");
    assertThat(req.body.get("loginOptions").get("templateId").asText()).isEqualTo("login-template");
  }

  @Test
  void testSignUpOrInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.IM, "", null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOptions() {
    User user = User.builder()
        .loginId("login-1").name("Some Name").phone(MOCK_PHONE)
        .recoveryEmail("recovery@domain.com").recoveryPhone("+1-5555555556").build();
    SignUpOptions signUpOptions = SignUpOptions.builder()
        .locale("en").pkceChallenge("Y2hhbGxlbmdl").templateId("login-template").tenantId("tenant-1").build();
    OTPSignUpOptions otpSignUpOptions = OTPSignUpOptions.builder()
        .providerId("provider-1").templateId("template-1").build();
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.signUp(DeliveryMethod.IM, MOCK_PHONE, user, signUpOptions, otpSignUpOptions), result);

    assertThat(result.get()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(req.uri).isEqualTo("/v1/auth/otp/signup/im");
    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(req.responseClass).isEqualTo(MaskedPhoneRes.class);
    assertThat(req.body.get("loginId").asText()).isEqualTo(MOCK_PHONE);
    assertThat(req.body.get("phone").asText()).isEqualTo(MOCK_PHONE);
    assertThat(req.body.has("whatsApp")).isFalse();
    assertThat(req.body.get("providerId").asText()).isEqualTo("provider-1");
    assertThat(req.body.get("templateId").asText()).isEqualTo("template-1");
    JsonNode userNode = req.body.get("user");
    assertThat(userNode.get("loginId").asText()).isEqualTo("login-1");
    assertThat(userNode.get("recoveryEmail").asText()).isEqualTo("recovery@domain.com");
    assertThat(userNode.get("recoveryPhone").asText()).isEqualTo("+1-5555555556");
    JsonNode options = req.body.get("loginOptions");
    assertThat(options.get("locale").asText()).isEqualTo("en");
    assertThat(options.get("pkceChallenge").asText()).isEqualTo("Y2hhbGxlbmdl");
    assertThat(options.get("templateId").asText()).isEqualTo("login-template");
    assertThat(options.get("tenantId").asText()).isEqualTo("tenant-1");
  }

  @Test
  void testSignUpWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.IM, "", new User(), null, new OTPSignUpOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOptionsForInvalidPhone() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.IM, "not-a-phone", new User(), null, new OTPSignUpOptions()));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  private static UpdateOptions fullUpdateOptions() {
    return UpdateOptions.builder()
        .providerId("provider-1").templateId("template-1").locale("en").failOnConflict(true).mfa(true).build();
  }

  private static void assertUpdateOptions(JsonNode body) {
    assertThat(body.get("providerId").asText()).isEqualTo("provider-1");
    assertThat(body.get("templateId").asText()).isEqualTo("template-1");
    assertThat(body.get("locale").asText()).isEqualTo("en");
    assertThat(body.get("failOnConflict").asBoolean()).isTrue();
    assertThat(body.get("mfa").asBoolean()).isTrue();
  }

  @Test
  void testUpdateUserEmailWithUpdateOptions() {
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, fullUpdateOptions()), result);

    assertThat(result.get()).isEqualTo(MOCK_MASKED_EMAIL);
    assertThat(req.uri).isEqualTo("/v1/auth/otp/update/email");
    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(req.body.get("email").asText()).isEqualTo(MOCK_EMAIL);
    assertUpdateOptions(req.body);
  }

  @Test
  void testUpdateUserEmailWithoutNewUpdateOptionsOmitsThem() {
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, null), result);

    for (String field : Arrays.asList("providerId", "templateId", "locale", "failOnConflict", "mfa")) {
      assertThat(req.body.has(field)).as(field).isFalse();
    }
  }

  @Test
  void testUpdateUserPhoneWithUpdateOptions() {
    AtomicReference<String> result = new AtomicReference<>();
    CapturedRequest req = capture(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN,
            fullUpdateOptions()), result);

    assertThat(result.get()).isEqualTo(MOCK_MASKED_PHONE);
    assertThat(req.uri).isEqualTo("/v1/auth/otp/update/phone/im");
    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(req.body.get("phone").asText()).isEqualTo(MOCK_PHONE);
    assertUpdateOptions(req.body);
  }

  @Test
  void testVerifyCodeWithIMAndFullResponse() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"d\",\"cookiePath\":\"/\","
        + "\"cookieMaxAge\":1,\"cookieExpiration\":2,\"firstSeen\":true,\"sessionExpiration\":3,"
        + "\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"t-sso\","
        + "\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":4,\"cookieName\":\"cn\","
        + "\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"kdj\",\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\"},"
        + "\"user\":{\"userId\":\"u1\",\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true},"
        + "\"SCIM\":true,\"OIDC\":true,\"externalIds\":[\"x\"],\"webauthn\":true,\"password\":true,"
        + "\"editable\":true,\"push\":true,\"permissions\":[\"p\"],\"consentExpiration\":5,"
        + "\"recoveryEmail\":\"re\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"rp\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":6,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,"
        + "\"dependent\":true,\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\",\"oidcTenantId\":\"ot\","
        + "\"consentTenantId\":\"ct\",\"lastLoginTime\":7,\"unknownField\":1,"
        + "\"userTenants\":[{\"tenantId\":\"t1\",\"permissions\":[\"tp\"],\"roleIds\":[\"tr\"]}],"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}]}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);
    AtomicReference<AuthenticationInfo> result = new AtomicReference<>();
    CapturedRequest req = capture(jwtResponse,
        () -> otpService.verifyCode(DeliveryMethod.IM, MOCK_PHONE, "123456"), result);

    assertThat(req.uri).isEqualTo("/v1/auth/otp/verify/im");
    assertThat(req.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(req.body.get("loginId").asText()).isEqualTo(MOCK_PHONE);
    assertThat(req.body.get("code").asText()).isEqualTo("123456");

    AuthenticationInfo info = result.get();
    assertThat(info.getCookieDomain()).isEqualTo("d");
    assertThat(info.getCookiePath()).isEqualTo("/");
    assertThat(info.getCookieMaxAge()).isEqualTo(1);
    assertThat(info.getCookieExpiration()).isEqualTo(2);
    assertThat(info.getSessionExpiration()).isEqualTo(3);
    assertThat(info.getExternalToken()).isEqualTo("ext");
    assertThat(info.getClaims()).containsEntry("c", "v");
    assertThat(info.getTenantSSOID()).isEqualTo("t-sso");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(4);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();

    IDPResponse idp = info.getIdpResponse();
    assertThat(idp.getSamlResponse()).isEqualTo("sr");
    assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(idp.getOidcResponse()).isEqualTo("or");
    assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = info.getUser();
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getScim()).isTrue();
    assertThat(user.getOidc()).isTrue();
    assertThat(user.getExternalIds()).containsExactly("x");
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("p");
    assertThat(user.getConsentExpiration()).isEqualTo(5L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("rp");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(6L);
    assertThat(user.getRoleIds()).containsExactly("r1");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(7L);
    AssociatedTenant tenant = user.getUserTenants().get(0);
    assertThat(tenant.getPermissions()).containsExactly("tp");
    assertThat(tenant.getRoleIds()).containsExactly("tr");
    UserFamily family = user.getUserFamilies().get(0);
    assertThat(family.getFamilyId()).isEqualTo("f1");
    assertThat(family.getRoleNames()).containsExactly("fr");
    assertThat(family.getPermissions()).containsExactly("fp");
    assertThat(family.getFamilyScopedAttributes()).containsEntry("a", "b");

    // Response-only tenant fields are never sent back in requests
    JsonNode tenantJson = new ObjectMapper().valueToTree(tenant);
    assertThat(tenantJson.has("permissions")).isFalse();
    assertThat(tenantJson.has("roleIds")).isFalse();
  }

  @Test
  void testUserResponseReadsLowercaseAuthMethodNames() throws Exception {
    UserResponse user = new ObjectMapper().readValue(
        "{\"totp\":true,\"saml\":true,\"oauth\":{\"google\":true},\"scim\":true,\"oidc\":true}",
        UserResponse.class);
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getScim()).isTrue();
    assertThat(user.getOidc()).isTrue();
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
