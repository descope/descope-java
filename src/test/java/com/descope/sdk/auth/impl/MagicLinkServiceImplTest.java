package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_DOMAIN;
import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_MASKED_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_MASKED_PHONE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.sdk.TestUtils.UPDATE_MOCK_EMAIL;
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
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.MagicLinkTestUserResponse;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.descope.utils.UriUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class MagicLinkServiceImplTest {

  private MagicLinkService magicLinkService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.magicLinkService =
        AuthenticationServiceBuilder.buildServices(client).getMagicLinkService();
    this.userService = ManagementServiceBuilder.buildServices(client).getUserService();
  }

  @Test
  void signUp() {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signUp = magicLinkService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, user);
      assertThat(signUp).isNotBlank().contains("*");
    }
  }

  @SneakyThrows
  @Test
  void testVerify() {
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
        authenticationInfo = magicLinkService.verify("SomeToken");
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
  void signIn() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signIn =
          magicLinkService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null, null);
      assertThat(signIn).isNotBlank().contains("*");
    }
  }

  @Test
  void testSignUpOrInForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String signUpOrIn =
          magicLinkService.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN);
      assertThat(signUpOrIn).isNotBlank().contains("*");
    }
  }

  @Test
  void testSignUpOrInForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.signUpOrIn(DeliveryMethod.EMAIL, "", MOCK_DOMAIN));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyLoginId() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail("", MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail(MOCK_EMAIL, "", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForInvalidEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyRefreshToken() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> magicLinkService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN, "", null));

    assertNotNull(thrown);
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedEmailRes maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String updateUserEmail =
          magicLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null);
      assertThat(updateUserEmail).isNotBlank().contains("*");
    }
  }

  @Test
  void testUpdateUserPhoneForLoginID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.SMS, "", MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForEmptyPhone() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.SMS, MOCK_EMAIL, "", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForInvalidPhone() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.SMS, MOCK_EMAIL, "1234E", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForInvalidMethod() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                magicLinkService.updateUserPhone(
                    DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));
    assertNotNull(thrown);
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    MaskedPhoneRes maskedEmailRes = new MaskedPhoneRes(MOCK_MASKED_PHONE);
    doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String updateUserPhone =
          magicLinkService.updateUserPhone(
            DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null);
      assertThat(updateUserPhone).isNotBlank().contains("X");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> captureRequest(ApiProxy apiProxy, ArgumentCaptor<URI> uriCaptor) {
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), any());
    return new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
        .convertValue(bodyCaptor.getValue(), Map.class);
  }

  @SuppressWarnings("unchecked")
  private String captureAuthHeader(MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder) {
    ArgumentCaptor<Supplier<String>> headerCaptor = ArgumentCaptor.forClass(Supplier.class);
    mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(headerCaptor.capture(), any()));
    return headerCaptor.getValue().get();
  }

  @Test
  void testSignInWithProviderSsoAppAndRecovery() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      LoginOptions loginOptions = LoginOptions.builder().locale("en").build();
      String masked = magicLinkService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, null, null, loginOptions,
          "provider1", "app1", true);
      assertEquals(MOCK_MASKED_EMAIL, masked);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/signin/email", uriCaptor.getValue().getPath());
      assertEquals(MOCK_EMAIL, body.get("loginId"));
      assertEquals("provider1", body.get("providerId"));
      assertEquals("app1", body.get("ssoAppId"));
      assertEquals(true, body.get("useRecovery"));
      assertEquals("en", ((Map<String, Object>) body.get("loginOptions")).get("locale"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testSignInWithProviderForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signIn(DeliveryMethod.EMAIL, "", null, null, null, "provider1", "app1", true));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInWithoutOptionsOmitsNewFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      magicLinkService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null, null);
      Map<String, Object> body = captureRequest(apiProxy, ArgumentCaptor.forClass(URI.class));
      assertThat(body).containsOnlyKeys("loginId", "URI");
    }
  }

  @Test
  void testSignUpSmsWithProviderId() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      User user = User.builder().name(MOCK_NAME).phone(MOCK_PHONE).build();
      String masked = magicLinkService.signUp(DeliveryMethod.SMS, "someLoginId", null, user, null, "provider1");
      assertEquals(MOCK_MASKED_PHONE, masked);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/signup/sms", uriCaptor.getValue().getPath());
      assertEquals(MOCK_PHONE, body.get("phone"));
      assertEquals("provider1", body.get("providerId"));
      assertEquals("someLoginId", body.get("loginId"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testSignUpEmailWithProviderId() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      magicLinkService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, null, null, null, "provider1");
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/signup/email", uriCaptor.getValue().getPath());
      assertEquals(MOCK_EMAIL, body.get("email"));
      assertEquals("provider1", body.get("providerId"));
      assertThat(body).doesNotContainKey("phone");
    }
  }

  @Test
  void testSignUpSmsWithProviderIdForInvalidPhone() {
    User user = User.builder().phone("1234E").build();
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signUp(DeliveryMethod.SMS, "someLoginId", null, user, null, "provider1"));
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpSmsWithoutProviderIdOmitsPhone() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      User user = User.builder().phone(MOCK_PHONE).build();
      magicLinkService.signUp(DeliveryMethod.SMS, MOCK_PHONE, null, user, null);
      Map<String, Object> body = captureRequest(apiProxy, ArgumentCaptor.forClass(URI.class));
      assertThat(body).doesNotContainKeys("phone", "providerId");
    }
  }

  @Test
  void testSignUpOrInWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      LoginOptions loginOptions = LoginOptions.builder().locale("en").build();
      String masked = magicLinkService.signUpOrIn(DeliveryMethod.SMS, MOCK_PHONE, null, loginOptions,
          "provider1", "app1", false);
      assertEquals(MOCK_MASKED_PHONE, masked);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/signup-in/sms", uriCaptor.getValue().getPath());
      assertEquals(MOCK_PHONE, body.get("loginId"));
      assertEquals("provider1", body.get("providerId"));
      assertEquals("app1", body.get("ssoAppId"));
      assertEquals(false, body.get("useRecovery"));
      assertEquals("en", ((Map<String, Object>) body.get("loginOptions")).get("locale"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testSignUpOrInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signUpOrIn(DeliveryMethod.SMS, "", null, null, "provider1", null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithProviderLocaleAndConflict() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String masked = magicLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, null, MOCK_REFRESH_TOKEN, null,
          null, "provider1", "en", true);
      assertEquals(MOCK_MASKED_EMAIL, masked);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/update/email", uriCaptor.getValue().getPath());
      assertEquals(UPDATE_MOCK_EMAIL, body.get("email"));
      assertEquals("provider1", body.get("providerId"));
      assertEquals("en", body.get("locale"));
      assertEquals(true, body.get("failOnConflict"));
      assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testUpdateUserEmailWithProviderForEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, null, "", null, null, "provider1",
            "en", true));
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithoutOptionsOmitsNewFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      magicLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, null, MOCK_REFRESH_TOKEN, null);
      Map<String, Object> body = captureRequest(apiProxy, ArgumentCaptor.forClass(URI.class));
      assertThat(body).containsOnlyKeys("email", "loginId", "crossDevice", "addToLoginIDs", "onMergeUseExisting");
    }
  }

  @Test
  void testUpdateUserPhoneWithProviderLocaleAndConflict() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String masked = magicLinkService.updateUserPhone(DeliveryMethod.SMS, MOCK_EMAIL, MOCK_PHONE, null,
          MOCK_REFRESH_TOKEN, null, null, "provider1", "en", false);
      assertEquals(MOCK_MASKED_PHONE, masked);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/update/phone/sms", uriCaptor.getValue().getPath());
      assertEquals(MOCK_PHONE, body.get("phone"));
      assertEquals("provider1", body.get("providerId"));
      assertEquals("en", body.get("locale"));
      assertEquals(false, body.get("failOnConflict"));
      assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testUpdateUserPhoneWithProviderForInvalidMethod() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, null,
            MOCK_REFRESH_TOKEN, null, null, "provider1", "en", false));
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @SneakyThrows
  @Test
  void testVerifyResponseUserFields() {
    String json = "{\"sessionJwt\":\"s\",\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"webauthn\":true,"
        + "\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
        + "\"consentExpiration\":11,\"recoveryEmail\":\"r@x.com\",\"verifiedRecoveryEmail\":true,"
        + "\"recoveryPhone\":\"+1555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":12,\"roleIds\":[\"r1\"],"
        + "\"recoveryCodes\":true,\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"rn\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"t1\","
        + "\"samlTenantId\":\"t2\",\"oidcTenantId\":\"t3\",\"consentTenantId\":\"t4\",\"lastLoginTime\":13,"
        + "\"unknownField\":1}}";
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class)).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = magicLinkService.verify("SomeToken");
      }
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/magiclink/verify", uriCaptor.getValue().getPath());
      assertEquals("SomeToken", body.get("token"));
    }
    UserResponse user = authenticationInfo.getUser();
    assertEquals(Arrays.asList("e1"), user.getExternalIds());
    assertEquals(true, user.getWebauthn());
    assertEquals(true, user.getPassword());
    assertEquals(true, user.getEditable());
    assertEquals(true, user.getScim());
    assertEquals(true, user.getPush());
    assertEquals(Arrays.asList("p1"), user.getPermissions());
    assertEquals(true, user.getOidc());
    assertEquals(11L, user.getConsentExpiration());
    assertEquals("r@x.com", user.getRecoveryEmail());
    assertEquals(true, user.getVerifiedRecoveryEmail());
    assertEquals("+1555", user.getRecoveryPhone());
    assertEquals(true, user.getVerifiedRecoveryPhone());
    assertEquals(12L, user.getModifiedTime());
    assertEquals(Arrays.asList("r1"), user.getRoleIds());
    assertEquals(true, user.getRecoveryCodes());
    assertEquals(true, user.getDependent());
    assertEquals("f1", user.getUserFamilies().get(0).getFamilyId());
    assertEquals(Arrays.asList("rn"), user.getUserFamilies().get(0).getRoleNames());
    assertEquals(Arrays.asList("fp"), user.getUserFamilies().get(0).getPermissions());
    assertEquals("b", user.getUserFamilies().get(0).getFamilyScopedAttributes().get("a"));
    assertEquals("t1", user.getScimTenantId());
    assertEquals("t2", user.getSamlTenantId());
    assertEquals("t3", user.getOidcTenantId());
    assertEquals("t4", user.getConsentTenantId());
    assertEquals(13L, user.getLastLoginTime());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    MagicLinkTestUserResponse response =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.EMAIL);
    assertThat(response.getLink()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    AuthenticationInfo authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdateEmail() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    MagicLinkTestUserResponse response =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.EMAIL);
    assertThat(response.getLink()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    AuthenticationInfo authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    MagicLinkTestUserResponse response2 =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.EMAIL);
    assertThat(response2.getLink()).isNotBlank();
    magicLinkService.updateUserEmail(
        loginId, loginId + "1@descope.com", MOCK_URL, authInfo.getRefreshToken().getJwt(), null);
    params = UriUtils.splitQuery("https://kuku.com" + response2.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdatePhone() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().phone(MOCK_PHONE).verifiedPhone(true).build());
    MagicLinkTestUserResponse response =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.SMS);
    assertThat(response.getLink()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    AuthenticationInfo authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    MagicLinkTestUserResponse response2 =
        userService.generateMagicLinkForTestUser(loginId, MOCK_URL, DeliveryMethod.SMS);
    assertThat(response2.getLink()).isNotBlank();
    magicLinkService.updateUserPhone(
        DeliveryMethod.SMS, loginId, "+1-555-555-5556", MOCK_URL, authInfo.getRefreshToken().getJwt(), null);
    params = UriUtils.splitQuery("https://kuku.com" + response2.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    authInfo = magicLinkService.verify(params.get("t").get(0));
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

}
