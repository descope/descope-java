package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
import com.descope.model.otp.response.OTPResponse;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.OTPService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class OTPServiceImplOptionsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final OTPResponse PHONE_RESPONSE = new OTPResponse(null, "+1-555XXXXX55", 3);
  private static final OTPResponse EMAIL_RESPONSE = new OTPResponse("u*******@domain.com", null, 3);
  private static final OTPSignInOptions SIGN_IN_OPTIONS = new OTPSignInOptions("provider", "app", true);
  private static final OTPUpdateOptions UPDATE_OPTIONS =
      new OTPUpdateOptions("provider", "template", "en", true, true);

  private final OTPService otpService = AuthenticationServiceBuilder.buildServices(Client.builder()
      .uri("https://api.descope.com")
      .projectId(PROJECT_ID)
      .sdkInfo(SdkInfo.builder().name("java").build())
      .build()).getOtpService();

  /** One captured POST: path, JSON body and Authorization header. */
  private static class Call {
    private String path;
    private String body;
    private String auth;
    private Object response;
  }

  @SuppressWarnings("unchecked")
  private static Call capture(Object response, Class<?> responseClass, Supplier<Object> call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    Call result = new Call();
    AtomicReference<String> auth = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      result.response = call.get();
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(responseClass));
    result.path = uri.getValue().getPath();
    try {
      result.body = MAPPER.writeValueAsString(body.getValue());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    result.auth = auth.get();
    return result;
  }

  private static void assertJson(String expected, String actual) throws Exception {
    assertEquals(MAPPER.readTree(expected), MAPPER.readTree(actual));
  }

  @Test
  void testSignInWithOptions() throws Exception {
    Call call = capture(PHONE_RESPONSE, OTPResponse.class,
        () -> otpService.signIn(DeliveryMethod.SMS, MOCK_PHONE, null, null, SIGN_IN_OPTIONS));
    assertEquals("/v1/auth/otp/signin/sms", call.path);
    assertEquals("Bearer " + PROJECT_ID, call.auth);
    assertJson("{\"loginId\":\"" + MOCK_PHONE + "\",\"providerId\":\"provider\",\"ssoAppId\":\"app\","
        + "\"useRecovery\":true}", call.body);
    assertEquals(PHONE_RESPONSE, call.response);
  }

  @Test
  void testSignInWithOptionsStepUp() throws Exception {
    LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
    Call call = capture(EMAIL_RESPONSE, OTPResponse.class,
        () -> otpService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, loginOptions, MOCK_REFRESH_TOKEN, null));
    assertEquals("/v1/auth/otp/signin/email", call.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, call.auth);
    assertThat(call.body).doesNotContain("providerId", "ssoAppId", "useRecovery");
    assertEquals(EMAIL_RESPONSE, call.response);
  }

  @Test
  void testSignInWithOptionsInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.SMS, "", null, null, SIGN_IN_OPTIONS));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(null, MOCK_PHONE, null, null, SIGN_IN_OPTIONS));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signIn(DeliveryMethod.SMS, MOCK_PHONE, LoginOptions.builder().mfa(true).build(), null,
            SIGN_IN_OPTIONS));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInIm() throws Exception {
    Call call = capture(PHONE_RESPONSE, OTPResponse.class,
        () -> otpService.signIn(DeliveryMethod.IM, MOCK_PHONE, null, null, null));
    assertEquals("/v1/auth/otp/signin/im", call.path);
    assertJson("{\"loginId\":\"" + MOCK_PHONE + "\"}", call.body);

    call = capture(new MaskedPhoneRes("+1-555XXXXX55"), MaskedPhoneRes.class,
        () -> otpService.signIn(DeliveryMethod.IM, MOCK_PHONE, null));
    assertEquals("/v1/auth/otp/signin/im", call.path);
    assertEquals("+1-555XXXXX55", call.response);
  }

  @Test
  void testSignUpOrInWithOptions() throws Exception {
    Call call = capture(PHONE_RESPONSE, OTPResponse.class,
        () -> otpService.signUpOrIn(DeliveryMethod.IM, MOCK_PHONE, SIGN_IN_OPTIONS));
    assertEquals("/v1/auth/otp/signup-in/im", call.path);
    assertEquals("Bearer " + PROJECT_ID, call.auth);
    assertJson("{\"loginId\":\"" + MOCK_PHONE + "\",\"providerId\":\"provider\",\"ssoAppId\":\"app\","
        + "\"useRecovery\":true}", call.body);
    assertEquals(PHONE_RESPONSE, call.response);
  }

  @Test
  void testSignUpOrInWithOptionsInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(DeliveryMethod.SMS, " ", SIGN_IN_OPTIONS));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUpOrIn(null, MOCK_PHONE, SIGN_IN_OPTIONS));
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOptions() throws Exception {
    User user = User.builder().name("name").phone(MOCK_PHONE).build();
    SignUpOptions signUpOptions = SignUpOptions.builder().templateOptions(Collections.singletonMap("k", "v")).build();
    Call call = capture(PHONE_RESPONSE, OTPResponse.class,
        () -> otpService.signUp(DeliveryMethod.WHATSAPP, MOCK_PHONE, user, signUpOptions,
            new OTPSignUpOptions("provider", "template")));
    assertEquals("/v1/auth/otp/signup/whatsapp", call.path);
    assertEquals("Bearer " + PROJECT_ID, call.auth);
    assertJson("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"" + MOCK_PHONE + "\",\"user\":{\"name\":\"name\","
        + "\"phone\":\"" + MOCK_PHONE + "\"},\"loginOptions\":{\"templateOptions\":{\"k\":\"v\"}},"
        + "\"providerId\":\"provider\",\"templateId\":\"template\"}", call.body);
    assertEquals(PHONE_RESPONSE, call.response);

    call = capture(EMAIL_RESPONSE, OTPResponse.class,
        () -> otpService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, null, null, null));
    assertEquals("/v1/auth/otp/signup/email", call.path);
    assertJson("{\"email\":\"" + MOCK_EMAIL + "\",\"loginId\":\"" + MOCK_EMAIL + "\",\"user\":{\"email\":\""
        + MOCK_EMAIL + "\"}}", call.body);
    assertEquals(EMAIL_RESPONSE, call.response);
  }

  @Test
  void testSignUpIm() throws Exception {
    User user = User.builder().phone(MOCK_PHONE).build();
    Call call = capture(PHONE_RESPONSE, OTPResponse.class,
        () -> otpService.signUp(DeliveryMethod.IM, MOCK_PHONE, user, null, null));
    assertEquals("/v1/auth/otp/signup/im", call.path);
    assertJson("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"" + MOCK_PHONE + "\",\"user\":{\"phone\":\""
        + MOCK_PHONE + "\"}}", call.body);

    call = capture(new MaskedPhoneRes("+1-555XXXXX55"), MaskedPhoneRes.class,
        () -> otpService.signUp(DeliveryMethod.IM, MOCK_PHONE, user));
    assertEquals("/v1/auth/otp/signup/im", call.path);
    assertJson("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"" + MOCK_PHONE + "\",\"user\":{\"phone\":\""
        + MOCK_PHONE + "\"}}", call.body);
  }

  @Test
  void testSignUpWithOptionsInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(null, MOCK_PHONE, null, null, null));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.SMS, "", null, null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.signUp(DeliveryMethod.IM, "abc", null, null, null));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithOptions() throws Exception {
    Call call = capture(EMAIL_RESPONSE, OTPResponse.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_REFRESH_TOKEN,
            UpdateOptions.builder().addToLoginIds(true).build(), null, UPDATE_OPTIONS));
    assertEquals("/v1/auth/otp/update/email", call.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, call.auth);
    assertJson("{\"email\":\"" + MOCK_EMAIL + "\",\"loginId\":\"" + MOCK_EMAIL + "\",\"addToLoginIDs\":true,"
        + "\"onMergeUseExisting\":false,\"providerId\":\"provider\",\"templateId\":\"template\",\"locale\":\"en\","
        + "\"failOnConflict\":true,\"mfa\":true}", call.body);
    assertEquals(EMAIL_RESPONSE, call.response);
  }

  @Test
  void testUpdateUserEmailWithOptionsInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail("", MOCK_EMAIL, MOCK_REFRESH_TOKEN, null, null, UPDATE_OPTIONS));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null, null, UPDATE_OPTIONS));
    assertEquals("The Email argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, "", null, null, UPDATE_OPTIONS));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneWithOptions() throws Exception {
    Call call = capture(PHONE_RESPONSE, OTPResponse.class,
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null,
            Collections.singletonMap("k", "v"), UPDATE_OPTIONS));
    assertEquals("/v1/auth/otp/update/phone/im", call.path);
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, call.auth);
    assertJson("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"" + MOCK_EMAIL + "\",\"addToLoginIDs\":false,"
        + "\"onMergeUseExisting\":false,\"templateOptions\":{\"k\":\"v\"},\"providerId\":\"provider\","
        + "\"templateId\":\"template\",\"locale\":\"en\",\"failOnConflict\":true,\"mfa\":true}", call.body);
    assertEquals(PHONE_RESPONSE, call.response);

    call = capture(new MaskedPhoneRes("+1-555XXXXX55"), MaskedPhoneRes.class,
        () -> otpService.updateUserPhone(DeliveryMethod.IM, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null));
    assertEquals("/v1/auth/otp/update/phone/im", call.path);
    assertJson("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"" + MOCK_EMAIL + "\",\"addToLoginIDs\":false,"
        + "\"onMergeUseExisting\":false}", call.body);
  }

  @Test
  void testUpdateUserPhoneWithOptionsInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null, null,
            UPDATE_OPTIONS));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, "abc", MOCK_REFRESH_TOKEN, null, null,
            UPDATE_OPTIONS));
    assertEquals("The Phone argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_REFRESH_TOKEN, null,
            null, UPDATE_OPTIONS));
    assertEquals("The Method argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> otpService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, "", null, null,
            UPDATE_OPTIONS));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testVerifyCodeFullResponse() throws Exception {
    JWTResponse jwtResponse = MAPPER.readValue("{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\","
        + "\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,"
        + "\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
        + "\"consentExpiration\":11,\"recoveryEmail\":\"r@x.com\",\"verifiedRecoveryEmail\":true,"
        + "\"recoveryPhone\":\"+1555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":12,\"roleIds\":[\"r1\"],"
        + "\"recoveryCodes\":true,\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"t1\","
        + "\"samlTenantId\":\"t2\",\"oidcTenantId\":\"t3\",\"consentTenantId\":\"t4\",\"lastLoginTime\":13},"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"su\",\"samlGeneratedRoles\":\"sg\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ou\",\"oidcGeneratedRoles\":\"og\"},"
        + "\"sessionExpiration\":14,\"externalToken\":\"et\",\"tenantSSOID\":\"sso\",\"trustedDeviceJwt\":\"td\","
        + "\"nextRefreshSeconds\":15,\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\","
        + "\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kd\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,\"unknownField\":1}", JWTResponse.class);
    Call call;
    try (MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
      jwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      call = capture(jwtResponse, JWTResponse.class,
          () -> otpService.verifyCode(DeliveryMethod.IM, MOCK_PHONE, "123456"));
    }
    assertEquals("/v1/auth/otp/verify/im", call.path);
    assertEquals("Bearer " + PROJECT_ID, call.auth);
    assertJson("{\"loginId\":\"" + MOCK_PHONE + "\",\"code\":\"123456\"}", call.body);

    AuthenticationInfo info = (AuthenticationInfo) call.response;
    assertEquals(Long.valueOf(14), info.getSessionExpiration());
    assertEquals("et", info.getExternalToken());
    assertEquals("sso", info.getTenantSSOID());
    assertEquals("td", info.getTrustedDeviceJwt());
    assertEquals(Integer.valueOf(15), info.getNextRefreshSeconds());
    assertEquals("cn", info.getCookieName());
    assertEquals("scn", info.getSessionCookieName());
    assertEquals("scd", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("kd", info.getKnownDeviceJwt());
    assertEquals(true, info.getSsoAuthenticationOnly());
    assertEquals(true, info.getUserCreationDeferred());
    assertEquals("sr", info.getIdpResponse().getSamlResponse());
    assertEquals("su", info.getIdpResponse().getSamlGeneratedUser());
    assertEquals("sg", info.getIdpResponse().getSamlGeneratedRoles());
    assertEquals("or", info.getIdpResponse().getOidcResponse());
    assertEquals("ou", info.getIdpResponse().getOidcGeneratedUser());
    assertEquals("og", info.getIdpResponse().getOidcGeneratedRoles());

    UserResponse user = info.getUser();
    assertEquals(Collections.singletonList("e1"), user.getExternalIds());
    assertEquals(true, user.getWebauthn());
    assertEquals(true, user.getPassword());
    assertEquals(true, user.getEditable());
    assertEquals(true, user.getScim());
    assertEquals(true, user.getPush());
    assertEquals(Collections.singletonList("p1"), user.getPermissions());
    assertEquals(true, user.getOidc());
    assertEquals(Long.valueOf(11), user.getConsentExpiration());
    assertEquals("r@x.com", user.getRecoveryEmail());
    assertEquals(true, user.getVerifiedRecoveryEmail());
    assertEquals("+1555", user.getRecoveryPhone());
    assertEquals(true, user.getVerifiedRecoveryPhone());
    assertEquals(Long.valueOf(12), user.getModifiedTime());
    assertEquals(Collections.singletonList("r1"), user.getRoleIds());
    assertEquals(true, user.getRecoveryCodes());
    assertEquals(true, user.getDependent());
    assertEquals("f1", user.getUserFamilies().get(0).getFamilyId());
    assertEquals(Collections.singletonList("fr"), user.getUserFamilies().get(0).getRoleNames());
    assertEquals(Collections.singletonList("fp"), user.getUserFamilies().get(0).getPermissions());
    assertEquals(Collections.singletonMap("a", "b"), user.getUserFamilies().get(0).getFamilyScopedAttributes());
    assertEquals("t1", user.getScimTenantId());
    assertEquals("t2", user.getSamlTenantId());
    assertEquals("t3", user.getOidcTenantId());
    assertEquals("t4", user.getConsentTenantId());
    assertEquals(Long.valueOf(13), user.getLastLoginTime());
  }

  @Test
  void testResponsesParseLeniently() throws Exception {
    JWTResponse jwtResponse = MAPPER.readValue("{\"user\":null,\"idpResponse\":null}", JWTResponse.class);
    assertEquals(null, jwtResponse.getUser());
    OTPResponse response = MAPPER.readValue("{\"maskedPhone\":\"m\",\"other\":1}", OTPResponse.class);
    assertEquals("m", response.getMaskedPhone());
    assertEquals(null, response.getVerificationAttempts());
  }
}
