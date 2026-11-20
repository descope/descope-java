package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_DOMAIN;
import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_MASKED_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_MASKED_PHONE;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.PhoneEnchantedLinkResponse;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdatePhoneRequest;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.EnchantedLinkTestUserResponse;
import com.descope.model.user.response.UserFamily;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.EnchantedLinkService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.descope.utils.UriUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class EnchantedLinkServiceImplTest {
  private EnchantedLinkService enchantedLinkService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.enchantedLinkService =
        AuthenticationServiceBuilder.buildServices(client).getEnchantedLinkService();
    this.userService = ManagementServiceBuilder.buildServices(client).getUserService();
  }

  @Test
  void signUp() {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(EnchantedLinkResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(),
        any())).thenReturn(apiProxy);
      EnchantedLinkResponse signUp = enchantedLinkService.signUp(MOCK_EMAIL, MOCK_DOMAIN, user);
      assertNotNull(signUp);
    }
  }

  @Test
  void signIn() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(EnchantedLinkResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      EnchantedLinkResponse response = enchantedLinkService.signIn(MOCK_EMAIL, MOCK_DOMAIN, null, null);
      assertThat(response).isNotNull();
    }
  }

  @Test
  void testSignUpOrInForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(EnchantedLinkResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      EnchantedLinkResponse response = enchantedLinkService.signUpOrIn(MOCK_EMAIL, MOCK_DOMAIN);
      assertThat(response).isNotNull();
    }
  }

  @Test
  void testSignUpOrInForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> enchantedLinkService.signUpOrIn("", MOCK_DOMAIN));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyLoginId() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> enchantedLinkService.updateUserEmail("", MOCK_EMAIL, MOCK_DOMAIN, "", null));

    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "", MOCK_DOMAIN, "", null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForInvalidEmail() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_DOMAIN, "", null));

    assertNotNull(thrown);
    assertEquals("The Email argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForEmptyRefreshToken() {

    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN, "", null));

    assertNotNull(thrown);
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    EnchantedLinkResponse mockRes = new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL);
    doReturn(mockRes).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      EnchantedLinkResponse enchantedLinkRes =
          enchantedLinkService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null);
      assertThat(enchantedLinkRes.getMaskedEmail()).isNotBlank().contains("*");
    }
  }

  @Test
  void signUpWithPhoneSendsPhoneBodyToSmsRoute() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      PhoneEnchantedLinkResponse response =
          enchantedLinkService.signUpWithPhone(MOCK_PHONE, MOCK_DOMAIN, null);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignUpRequest> bodyCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup/sms");
      SignUpRequest sent = bodyCaptor.getValue();
      assertThat(sent.getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getLoginId()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getEmail()).isNull();
      assertThat(sent.getUri()).isEqualTo(MOCK_DOMAIN);
      assertThat(sent.getUser().getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void signInWithPhoneSendsLoginIdToSmsRoute() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      PhoneEnchantedLinkResponse response =
          enchantedLinkService.signInWithPhone(MOCK_PHONE, MOCK_DOMAIN, null, null);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignInRequest> bodyCaptor = ArgumentCaptor.forClass(SignInRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signin/sms");
      SignInRequest sent = bodyCaptor.getValue();
      assertThat(sent.getLoginId()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getUri()).isEqualTo(MOCK_DOMAIN);
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void signUpOrInWithPhoneSendsLoginIdToSmsRoute() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      PhoneEnchantedLinkResponse response =
          enchantedLinkService.signUpOrInWithPhone(MOCK_PHONE, MOCK_DOMAIN);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignInRequest> bodyCaptor = ArgumentCaptor.forClass(SignInRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/sms");
      SignInRequest sent = bodyCaptor.getValue();
      assertThat(sent.getLoginId()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getUri()).isEqualTo(MOCK_DOMAIN);
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void updateUserPhoneSendsPhoneBodyToSmsRoute() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      PhoneEnchantedLinkResponse response = enchantedLinkService.updateUserPhone(
          MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<UpdatePhoneRequest> bodyCaptor = ArgumentCaptor.forClass(UpdatePhoneRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath())
          .isEqualTo("/v1/auth/enchantedlink/update/phone/sms");
      UpdatePhoneRequest sent = bodyCaptor.getValue();
      assertThat(sent.getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getLoginId()).isEqualTo(MOCK_EMAIL);
      assertThat(sent.getUri()).isEqualTo(MOCK_DOMAIN);
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void updateUserPhoneRejectsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone("", MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null))
        .getMessage());
    assertEquals("The Phone argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, "abc", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null))
        .getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, "", null))
        .getMessage());
  }

  @Test
  void testGetSession() {
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
        authenticationInfo = enchantedLinkService.getSession("testRef");
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
  void testVerify() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(EmptyResponse.class)).when(apiProxy).post(any(), any(), any());

    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        enchantedLinkService.verify("token");
      }
    }
  }

  @Test
  void signInSendsProviderSsoAppAndRecovery() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    EnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.signIn(MOCK_EMAIL, MOCK_DOMAIN, null, null, "p1", "app1", true));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signin/email");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("URI", MOCK_DOMAIN, "loginId", MOCK_EMAIL,
        "providerId", "p1", "ssoAppId", "app1", "useRecovery", true));
    assertThat(response.getPendingRef()).isEqualTo(MOCK_URL);
    assertThat(response.getLinkId()).isEqualTo(MOCK_URL);
    assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
  }

  @Test
  void signInWithPhoneSendsProviderSsoAppAndRecovery() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    PhoneEnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.signInWithPhone(MOCK_PHONE, MOCK_DOMAIN, null, null, "p1", "app1", false));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
        eq(PhoneEnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signin/sms");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("URI", MOCK_DOMAIN, "loginId", MOCK_PHONE,
        "providerId", "p1", "ssoAppId", "app1", "useRecovery", false));
    assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
  }

  @Test
  void signInWithoutNewFieldsOmitsThem() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> header = new AtomicReference<>();
    withProxy(apiProxy, header, () -> enchantedLinkService.signIn(MOCK_EMAIL, MOCK_DOMAIN, null, null));

    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(any(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("URI", MOCK_DOMAIN, "loginId", MOCK_EMAIL));
  }

  @Test
  void signInWithNewFieldsRejectsEmptyLoginId() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signIn("", MOCK_DOMAIN, null, null, "p1", "app1", true)).getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signInWithPhone("", MOCK_DOMAIN, null, null, "p1", "app1", true)).getMessage());
  }

  @Test
  void signUpOrInSendsProviderSsoAppAndRecovery() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    EnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.signUpOrIn(MOCK_EMAIL, MOCK_DOMAIN, "p1", "app1", true));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/email");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("URI", MOCK_DOMAIN, "loginId", MOCK_EMAIL,
        "providerId", "p1", "ssoAppId", "app1", "useRecovery", true));
    assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
  }

  @Test
  void signUpOrInWithPhoneSendsProviderSsoAppAndRecovery() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    PhoneEnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.signUpOrInWithPhone(MOCK_PHONE, MOCK_DOMAIN, "p1", "app1", true));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
        eq(PhoneEnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/sms");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("URI", MOCK_DOMAIN, "loginId", MOCK_PHONE,
        "providerId", "p1", "ssoAppId", "app1", "useRecovery", true));
    assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
  }

  @Test
  void signUpOrInWithNewFieldsRejectsEmptyLoginId() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrIn("", MOCK_DOMAIN, "p1", "app1", true)).getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrInWithPhone("", MOCK_DOMAIN, "p1", "app1", true)).getMessage());
  }

  @Test
  void signUpSendsProviderAndTemplate() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    EnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.signUp(MOCK_EMAIL, MOCK_DOMAIN, null, null, "p1", "t1"));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup/email");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = toJson(bodyCaptor.getValue());
    assertThat(body).containsEntry("providerId", "p1").containsEntry("templateId", "t1")
        .containsEntry("email", MOCK_EMAIL).containsEntry("loginId", MOCK_EMAIL).containsEntry("URI", MOCK_DOMAIN);
    assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
  }

  @Test
  void signUpWithPhoneSendsProviderAndTemplate() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    PhoneEnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.signUpWithPhone(MOCK_PHONE, MOCK_DOMAIN, null, null, "p1", "t1"));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
        eq(PhoneEnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup/sms");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    Map<String, Object> body = toJson(bodyCaptor.getValue());
    assertThat(body).containsEntry("providerId", "p1").containsEntry("templateId", "t1")
        .containsEntry("phone", MOCK_PHONE).containsEntry("loginId", MOCK_PHONE).containsEntry("URI", MOCK_DOMAIN);
    assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
  }

  @Test
  void signUpWithoutNewFieldsOmitsThem() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> header = new AtomicReference<>();
    withProxy(apiProxy, header, () -> enchantedLinkService.signUp(MOCK_EMAIL, MOCK_DOMAIN, null));

    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(any(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(toJson(bodyCaptor.getValue())).doesNotContainKeys("providerId", "templateId");
  }

  @Test
  void updateUserEmailSendsNewFields() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    EnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
            new UpdateOptions(true, false), null, "p1", "t1", "en", true));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/update/email");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("email", UPDATE_MOCK_EMAIL, "loginId", MOCK_EMAIL,
        "URI", MOCK_DOMAIN, "crossDevice", false, "addToLoginIDs", true, "onMergeUseExisting", false,
        "providerId", "p1", "templateId", "t1", "locale", "en", "failOnConflict", true));
    assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
  }

  @Test
  void updateUserPhoneSendsNewFields() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    PhoneEnchantedLinkResponse response = withProxy(apiProxy, header,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
            new UpdateOptions(true, false), null, "p1", "t1", "en", false));

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
        eq(PhoneEnchantedLinkResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/update/phone/sms");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("phone", MOCK_PHONE, "loginId", MOCK_EMAIL,
        "URI", MOCK_DOMAIN, "crossDevice", false, "addToLoginIDs", true, "onMergeUseExisting", false,
        "providerId", "p1", "templateId", "t1", "locale", "en", "failOnConflict", false));
    assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
  }

  @Test
  void updateUserWithoutNewFieldsOmitsThem() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> header = new AtomicReference<>();
    withProxy(apiProxy, header, () -> enchantedLinkService.updateUserEmail(
        MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null));

    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(any(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("email", UPDATE_MOCK_EMAIL, "loginId", MOCK_EMAIL,
        "URI", MOCK_DOMAIN, "crossDevice", false, "addToLoginIDs", false, "onMergeUseExisting", false));
  }

  @Test
  void updateUserWithNewFieldsRejectsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail("", UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null,
            null, "p1", "t1", "en", true)).getMessage());
    assertEquals("The Email argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null,
            null, "p1", "t1", "en", true)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, "", null,
            null, "p1", "t1", "en", true)).getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone("", MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null,
            null, "p1", "t1", "en", true)).getMessage());
    assertEquals("The Phone argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, "", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null,
            null, "p1", "t1", "en", true)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, "", null,
            null, "p1", "t1", "en", true)).getMessage());
  }

  @Test
  void verifySendsPkceVerifier() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> header = new AtomicReference<>();
    withProxy(apiProxy, header, () -> {
      enchantedLinkService.verify("token", "verifier".getBytes(StandardCharsets.UTF_8));
      return null;
    });

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EmptyResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/verify");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("token", "token", "pkceVerifier",
        Base64.getEncoder().encodeToString("verifier".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  void verifyWithoutPkceVerifierOmitsIt() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> header = new AtomicReference<>();
    withProxy(apiProxy, header, () -> {
      enchantedLinkService.verify("token");
      return null;
    });

    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(any(), bodyCaptor.capture(), eq(EmptyResponse.class));
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("token", "token"));
  }

  @Test
  void getSessionReadsFullResponse() throws Exception {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"firstSeen\":true,"
        + "\"sessionExpiration\":11,\"externalToken\":\"et\",\"tenantSSOID\":\"ts\","
        + "\"trustedDeviceJwt\":\"td\",\"nextRefreshSeconds\":12,\"cookieName\":\"cn\","
        + "\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"kd\",\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\","
        + "\"samlGeneratedRoles\":\"sgr\",\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\","
        + "\"oidcGeneratedRoles\":\"ogr\",\"idpGroups\":[\"g\"]},"
        + "\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"OAuth\":{\"google\":true},"
        + "\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,"
        + "\"permissions\":[\"perm\"],\"OIDC\":true,\"consentExpiration\":21,"
        + "\"recoveryEmail\":\"re@x.com\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+1\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":22,\"roleIds\":[\"rid\"],"
        + "\"recoveryCodes\":true,\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\","
        + "\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"a\":\"b\"}}],"
        + "\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\",\"oidcTenantId\":\"ot\","
        + "\"consentTenantId\":\"ct\",\"lastLoginTime\":23,\"unknownField\":1}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    AuthenticationInfo info;
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      info = withProxy(apiProxy, header, () -> enchantedLinkService.getSession("testRef"));
    }

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(JWTResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/pending-session");
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(toJson(bodyCaptor.getValue())).isEqualTo(jsonOf("pendingRef", "testRef"));

    assertThat(info.getSessionExpiration()).isEqualTo(11);
    assertThat(info.getExternalToken()).isEqualTo("et");
    assertThat(info.getTenantSSOID()).isEqualTo("ts");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("td");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(12);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSSO()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kd");
    assertThat(info.getSsoAuthenticationOnly()).isTrue();
    assertThat(info.getUserCreationDeferred()).isTrue();

    IDPResponse idp = info.getIdpResponse();
    assertThat(idp.getSamlResponse()).isEqualTo("sr");
    assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(idp.getOidcResponse()).isEqualTo("or");
    assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");
    assertThat(idp.getIdpGroups()).containsExactly("g");

    UserResponse user = info.getUser();
    assertThat(user.getUserId()).isEqualTo("u1");
    assertThat(user.getExternalIds()).containsExactly("e1");
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getScim()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("perm");
    assertThat(user.getOidc()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(21L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re@x.com");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("+1");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(22L);
    assertThat(user.getRoleIds()).containsExactly("rid");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    assertThat(user.getUserFamilies()).hasSize(1);
    UserFamily family = user.getUserFamilies().get(0);
    assertThat(family.getFamilyId()).isEqualTo("f1");
    assertThat(family.getRoleNames()).containsExactly("fr");
    assertThat(family.getPermissions()).containsExactly("fp");
    assertThat(family.getFamilyScopedAttributes()).containsEntry("a", "b");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(23L);
  }

  @Test
  void userResponseStillReadsLowercaseOauth() throws Exception {
    UserResponse user = new ObjectMapper().readValue("{\"oauth\":{\"google\":true}}", UserResponse.class);
    assertThat(user.getOAuth()).containsEntry("google", true);
  }

  /** Runs the call with the given proxy and captures the Authorization header it was built with. */
  @SuppressWarnings("unchecked")
  private static <T> T withProxy(ApiProxy apiProxy, AtomicReference<String> header, Supplier<T> call) {
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      return call.get();
    }
  }

  private static Map<String, Object> jsonOf(Object... keysAndValues) {
    Map<String, Object> m = new HashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      m.put((String) keysAndValues[i], keysAndValues[i + 1]);
    }
    return m;
  }

  /** Serializes a request body the way the API proxy does. */
  private static Map<String, Object> toJson(Object body) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    return objectMapper.readValue(objectMapper.writeValueAsString(body), new TypeReference<Map<String, Object>>() {});
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    EnchantedLinkTestUserResponse response = userService.generateEnchantedLinkForTestUser(loginId, MOCK_URL);
    assertThat(response.getLink()).isNotBlank();
    assertThat(response.getPendingRef()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    enchantedLinkService.verify(params.get("t").get(0));
    AuthenticationInfo authInfo = enchantedLinkService.getSession(response.getPendingRef());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalUpdateEmail() {
    String loginId = TestUtils.getRandomName("u-");
    userService.createTestUser(
        loginId, UserRequest.builder().email(loginId + "@descope.com").build());
    EnchantedLinkTestUserResponse response = userService.generateEnchantedLinkForTestUser(loginId, MOCK_URL);
    assertThat(response.getLink()).isNotBlank();
    assertThat(response.getPendingRef()).isNotBlank();
    Map<String, List<String>> params = UriUtils.splitQuery("https://kuku.com" + response.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    enchantedLinkService.verify(params.get("t").get(0));
    AuthenticationInfo authInfo = enchantedLinkService.getSession(response.getPendingRef());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    EnchantedLinkTestUserResponse response2 = userService.generateEnchantedLinkForTestUser(loginId, MOCK_URL);
    assertThat(response2.getLink()).isNotBlank();
    assertThat(response2.getPendingRef()).isNotBlank();
    enchantedLinkService.updateUserEmail(
        loginId, loginId + "1@descope.com", MOCK_URL, authInfo.getRefreshToken().getJwt(), null);
    params = UriUtils.splitQuery("https://kuku.com" + response2.getLink());
    assertThat(params.get("t").size()).isEqualTo(1);
    enchantedLinkService.verify(params.get("t").get(0));
    authInfo = enchantedLinkService.getSession(response2.getPendingRef());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();    
    userService.delete(loginId);
  }
}
