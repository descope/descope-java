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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
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
import java.util.function.Supplier;
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

  @Test
  void testSignInWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().ssoAppId("app1").pkceChallenge("YWJj").tenantId("T1").build();
    Captured captured = capturePost(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.signIn(DeliveryMethod.IM, MOCK_PHONE, loginOptions, null, "provider1", "app2", true));
    assertEquals(MOCK_MASKED_PHONE, captured.result);
    assertEquals("/v1/auth/otp/signin/im", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID, captured.authorization);
    JsonNode body = captured.body;
    assertEquals(MOCK_PHONE, body.get("loginId").asText());
    assertEquals("provider1", body.get("providerId").asText());
    assertEquals("app2", body.get("ssoAppId").asText());
    assertTrue(body.get("useRecovery").asBoolean());
    assertEquals("app1", body.get("loginOptions").get("ssoAppId").asText());
    assertEquals("YWJj", body.get("loginOptions").get("pkceChallenge").asText());
    assertEquals("T1", body.get("loginOptions").get("tenantId").asText());
  }

  @Test
  void testSignInWithOptionsOmitsUnset() {
    Captured captured = capturePost(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, null));
    assertEquals("/v1/auth/otp/signin/email", captured.uri.getPath());
    assertFalse(captured.body.has("providerId"));
    assertFalse(captured.body.has("ssoAppId"));
    assertFalse(captured.body.has("useRecovery"));
  }

  @Test
  void testSignInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.IM, "", null, null, "provider1", "app2", true));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithProviderId() {
    SignUpOptions signUpOptions = SignUpOptions.builder().locale("en").pkceChallenge("YWJj").tenantId("T1").build();
    Captured captured = capturePost(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.signUp(DeliveryMethod.IM, MOCK_PHONE, new User(), signUpOptions, "provider1"));
    assertEquals(MOCK_MASKED_PHONE, captured.result);
    assertEquals("/v1/auth/otp/signup/im", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID, captured.authorization);
    JsonNode body = captured.body;
    assertEquals(MOCK_PHONE, body.get("loginId").asText());
    assertEquals("provider1", body.get("providerId").asText());
    assertEquals("en", body.get("loginOptions").get("locale").asText());
    assertEquals("YWJj", body.get("loginOptions").get("pkceChallenge").asText());
    assertEquals("T1", body.get("loginOptions").get("tenantId").asText());
  }

  @Test
  void testSignUpWithProviderIdForInvalidPhone() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.IM, "not-a-phone", new User(), null, "provider1"));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInWithOptions() {
    LoginOptions loginOptions = LoginOptions.builder().tenantId("T1").build();
    Captured captured = capturePost(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.signUpOrIn(DeliveryMethod.VOICE, MOCK_PHONE, loginOptions, "provider1", "app2", false));
    assertEquals(MOCK_MASKED_PHONE, captured.result);
    assertEquals("/v1/auth/otp/signup-in/voice", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID, captured.authorization);
    JsonNode body = captured.body;
    assertEquals(MOCK_PHONE, body.get("loginId").asText());
    assertEquals("provider1", body.get("providerId").asText());
    assertEquals("app2", body.get("ssoAppId").asText());
    assertFalse(body.get("useRecovery").asBoolean());
    assertEquals("T1", body.get("loginOptions").get("tenantId").asText());
  }

  @Test
  void testSignUpOrInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.VOICE, "", null, "provider1", null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testVerifyCodeResponseFields() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"d\",\"cookiePath\":\"/\","
        + "\"cookieMaxAge\":1,\"cookieExpiration\":2,\"sessionExpiration\":3,\"externalToken\":\"e\","
        + "\"claims\":{\"a\":\"b\"},\"tenantSSOID\":\"t\",\"trustedDeviceJwt\":\"td\",\"nextRefreshSeconds\":4,"
        + "\"cookieName\":\"c\",\"sessionCookieName\":\"sc\",\"sessionCookieDomain\":\"sd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"k\",\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"user\":{\"userId\":\"u\",\"externalIds\":[\"x\"],\"webauthn\":true,\"password\":true,"
        + "\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p\"],\"OIDC\":true,"
        + "\"consentExpiration\":5,\"recoveryEmail\":\"re\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"rp\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":6,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,"
        + "\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f\",\"roleNames\":[\"rn\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"k\":\"v\"}}],\"scimTenantId\":\"st\","
        + "\"samlTenantId\":\"sa\",\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":7},"
        + "\"idpResponse\":{\"samlResponse\":\"a\",\"samlGeneratedUser\":\"b\",\"samlGeneratedRoles\":\"c\","
        + "\"oidcResponse\":\"d\",\"oidcGeneratedUser\":\"e\",\"oidcGeneratedRoles\":\"f\"},\"unknown\":null}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    Captured captured;
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      captured = capturePost(jwtResponse, () -> otpService.verifyCode(DeliveryMethod.IM, MOCK_PHONE, "123456"));
    }
    assertEquals("/v1/auth/otp/verify/im", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID, captured.authorization);
    assertEquals(MOCK_PHONE, captured.body.get("loginId").asText());
    assertEquals("123456", captured.body.get("code").asText());
    AuthenticationInfo info = (AuthenticationInfo) captured.result;
    assertEquals("d", info.getCookieDomain());
    assertEquals("/", info.getCookiePath());
    assertEquals(1, info.getCookieMaxAge());
    assertEquals(2, info.getCookieExpiration());
    assertEquals(3, info.getSessionExpiration());
    assertEquals("e", info.getExternalToken());
    assertEquals("b", info.getClaims().get("a"));
    assertEquals("t", info.getTenantSSOID());
    assertEquals("td", info.getTrustedDeviceJwt());
    assertEquals(4, info.getNextRefreshSeconds());
    assertEquals("c", info.getCookieName());
    assertEquals("sc", info.getSessionCookieName());
    assertEquals("sd", info.getSessionCookieDomain());
    assertTrue(info.getUnsavedSSO());
    assertEquals("k", info.getKnownDeviceJwt());
    assertTrue(info.getSsoAuthenticationOnly());
    assertTrue(info.getUserCreationDeferred());
    UserResponse user = info.getUser();
    assertEquals(Arrays.asList("x"), user.getExternalIds());
    assertTrue(user.getWebauthn());
    assertTrue(user.getPassword());
    assertTrue(user.getEditable());
    assertTrue(user.getSCIM());
    assertTrue(user.getPush());
    assertEquals(Arrays.asList("p"), user.getPermissions());
    assertTrue(user.getOIDC());
    assertEquals(5L, user.getConsentExpiration());
    assertEquals("re", user.getRecoveryEmail());
    assertTrue(user.getVerifiedRecoveryEmail());
    assertEquals("rp", user.getRecoveryPhone());
    assertTrue(user.getVerifiedRecoveryPhone());
    assertEquals(6L, user.getModifiedTime());
    assertEquals(Arrays.asList("r1"), user.getRoleIds());
    assertTrue(user.getRecoveryCodes());
    assertTrue(user.getDependent());
    UserFamily family = user.getUserFamilies().get(0);
    assertEquals("f", family.getFamilyId());
    assertEquals(Arrays.asList("rn"), family.getRoleNames());
    assertEquals(Arrays.asList("fp"), family.getPermissions());
    assertEquals("v", family.getFamilyScopedAttributes().get("k"));
    assertEquals("st", user.getScimTenantId());
    assertEquals("sa", user.getSamlTenantId());
    assertEquals("ot", user.getOidcTenantId());
    assertEquals("ct", user.getConsentTenantId());
    assertEquals(7L, user.getLastLoginTime());
    IDPResponse idp = info.getIdpResponse();
    assertEquals("a", idp.getSamlResponse());
    assertEquals("b", idp.getSamlGeneratedUser());
    assertEquals("c", idp.getSamlGeneratedRoles());
    assertEquals("d", idp.getOidcResponse());
    assertEquals("e", idp.getOidcGeneratedUser());
    assertEquals("f", idp.getOidcGeneratedRoles());
  }

  @Test
  void testUpdateUserEmailWithUpdateOptions() {
    UpdateOptions updateOptions = UpdateOptions.builder()
        .providerId("provider1").locale("en").failOnConflict(true).mfa(true).build();
    Captured captured = capturePost(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, updateOptions));
    assertEquals(MOCK_MASKED_EMAIL, captured.result);
    assertEquals("/v1/auth/otp/update/email", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, captured.authorization);
    JsonNode body = captured.body;
    assertEquals(MOCK_EMAIL, body.get("email").asText());
    assertEquals("provider1", body.get("providerId").asText());
    assertEquals("en", body.get("locale").asText());
    assertTrue(body.get("failOnConflict").asBoolean());
    assertTrue(body.get("mfa").asBoolean());
  }

  @Test
  void testUpdateUserEmailOmitsUnsetUpdateOptions() {
    Captured captured = capturePost(new MaskedEmailRes(MOCK_MASKED_EMAIL),
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN, null));
    assertFalse(captured.body.has("providerId"));
    assertFalse(captured.body.has("locale"));
    assertFalse(captured.body.has("failOnConflict"));
    assertFalse(captured.body.has("mfa"));
  }

  @Test
  void testUpdateUserPhoneWithIM() {
    UpdateOptions updateOptions = UpdateOptions.builder()
        .providerId("provider1").locale("en").failOnConflict(false).mfa(true).build();
    Captured captured = capturePost(new MaskedPhoneRes(MOCK_MASKED_PHONE),
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, updateOptions));
    assertEquals(MOCK_MASKED_PHONE, captured.result);
    assertEquals("/v1/auth/otp/update/phone/im", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, captured.authorization);
    JsonNode body = captured.body;
    assertEquals(MOCK_PHONE, body.get("phone").asText());
    assertEquals("provider1", body.get("providerId").asText());
    assertEquals("en", body.get("locale").asText());
    assertFalse(body.get("failOnConflict").asBoolean());
    assertTrue(body.get("mfa").asBoolean());
  }

  private static class Captured {
    URI uri;
    JsonNode body;
    String authorization;
    Object result;
  }

  @SuppressWarnings("unchecked")
  private Captured capturePost(Object response, Supplier<Object> call) {
    Captured captured = new Captured();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doAnswer(inv -> {
      captured.uri = inv.getArgument(0);
      captured.body = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).valueToTree(inv.getArgument(1));
      return response;
    }).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.authorization = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      captured.result = call.get();
    }
    return captured;
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
