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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.EnchantedLinkSignInOptions;
import com.descope.model.enchantedlink.EnchantedLinkSignUpOptions;
import com.descope.model.enchantedlink.EnchantedLinkUpdateOptions;
import com.descope.model.enchantedlink.PhoneEnchantedLinkResponse;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdatePhoneRequest;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.EnchantedLinkTestUserResponse;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.SneakyThrows;
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

  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final String PKCE = "cGtjZQ==";

  /** What a call sent, and what it returned. */
  private static class Sent {
    String auth;
    URI uri;
    Object body;
    Object result;
  }

  @SuppressWarnings("unchecked")
  private static Sent send(Object response, Supplier<Object> call) {
    Sent sent = new Sent();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doAnswer(inv -> {
      sent.uri = inv.getArgument(0);
      sent.body = inv.getArgument(1);
      return response;
    }).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.auth = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      sent.result = call.get();
    }
    return sent;
  }

  @SneakyThrows
  private static void assertBody(String expectedJson, Object body) {
    assertEquals(MAPPER.readTree(expectedJson), MAPPER.valueToTree(body));
  }

  private static LoginOptions fullLoginOptions(boolean stepup) {
    return LoginOptions.builder()
        .stepup(stepup)
        .customClaims(Collections.singletonMap("k", "v"))
        .locale("en")
        .templateId("t1")
        .ssoAppId("app1")
        .pkceChallenge(PKCE)
        .tenantId("tenant1")
        .jwt("not-sent")
        .build();
  }

  private static final String LOGIN_OPTIONS_JSON = "{\"stepup\":false,\"mfa\":false,\"customClaims\":{\"k\":\"v\"},"
      + "\"revokeOtherSessions\":false,\"locale\":\"en\",\"templateId\":\"t1\",\"ssoAppId\":\"app1\","
      + "\"pkceChallenge\":\"" + PKCE + "\",\"tenantId\":\"tenant1\"}";

  private static final EnchantedLinkSignInOptions SIGN_IN_OPTIONS = EnchantedLinkSignInOptions.builder()
      .providerId("p1").templateId("t2").ssoAppId("app2").useRecovery(true).build();

  private static final String SIGN_IN_OPTIONS_JSON =
      "\"providerId\":\"p1\",\"templateId\":\"t2\",\"ssoAppId\":\"app2\",\"useRecovery\":true";

  @Test
  void signInWithOptionsSendsSpecBody() {
    EnchantedLinkResponse expected = new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL);
    Sent sent = send(expected, () -> enchantedLinkService.signIn(
        MOCK_EMAIL, MOCK_URL, null, fullLoginOptions(false), SIGN_IN_OPTIONS));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/signin/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertBody("{\"loginId\":\"" + MOCK_EMAIL + "\",\"redirectUrl\":\"" + MOCK_URL + "\",\"loginOptions\":"
        + LOGIN_OPTIONS_JSON + "," + SIGN_IN_OPTIONS_JSON + "}", sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void signInWithPhoneWithOptionsSendsStepUpToken() {
    PhoneEnchantedLinkResponse expected = new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE);
    Sent sent = send(expected, () -> enchantedLinkService.signInWithPhone(
        MOCK_PHONE, MOCK_URL, MOCK_REFRESH_TOKEN, fullLoginOptions(true), null));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/signin/sms");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertBody("{\"loginId\":\"" + MOCK_PHONE + "\",\"redirectUrl\":\"" + MOCK_URL + "\",\"loginOptions\":"
        + LOGIN_OPTIONS_JSON.replace("\"stepup\":false", "\"stepup\":true") + "}", sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void signUpOrInWithOptionsSendsSpecBody() {
    EnchantedLinkResponse expected = new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL);
    Sent sent = send(expected, () -> enchantedLinkService.signUpOrIn(
        MOCK_EMAIL, MOCK_URL, null, fullLoginOptions(false), SIGN_IN_OPTIONS));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertBody("{\"loginId\":\"" + MOCK_EMAIL + "\",\"redirectUrl\":\"" + MOCK_URL + "\",\"loginOptions\":"
        + LOGIN_OPTIONS_JSON + "," + SIGN_IN_OPTIONS_JSON + "}", sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void signUpOrInWithPhoneWithOptionsSendsSpecBody() {
    PhoneEnchantedLinkResponse expected = new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE);
    Sent sent = send(expected, () -> enchantedLinkService.signUpOrInWithPhone(
        MOCK_PHONE, MOCK_URL, MOCK_REFRESH_TOKEN, fullLoginOptions(true), SIGN_IN_OPTIONS));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/sms");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertBody("{\"loginId\":\"" + MOCK_PHONE + "\",\"redirectUrl\":\"" + MOCK_URL + "\",\"loginOptions\":"
        + LOGIN_OPTIONS_JSON.replace("\"stepup\":false", "\"stepup\":true") + "," + SIGN_IN_OPTIONS_JSON + "}",
        sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void signInWithOptionsRejectsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signIn("", MOCK_URL, null, null, SIGN_IN_OPTIONS)).getMessage());
    assertEquals("The token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signInWithPhone(MOCK_PHONE, MOCK_URL, "", fullLoginOptions(true), null))
        .getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrIn("", MOCK_URL, null, null, null)).getMessage());
    assertEquals("The token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrInWithPhone(MOCK_PHONE, MOCK_URL, null, fullLoginOptions(true), null))
        .getMessage());
  }

  private static final String SIGN_UP_OPTIONS_JSON = "{\"customClaims\":{\"k\":\"v\"},\"locale\":\"en\","
      + "\"pkceChallenge\":\"" + PKCE + "\",\"templateId\":\"t1\",\"tenantId\":\"tenant1\"}";

  private static SignUpOptions fullSignUpOptions() {
    return SignUpOptions.builder().customClaims(Collections.singletonMap("k", "v")).locale("en")
        .pkceChallenge(PKCE).templateId("t1").tenantId("tenant1").build();
  }

  @Test
  void signUpWithOptionsSendsSpecBody() {
    User user = User.builder().name("n").loginId("l1").recoveryEmail("r@domain.com").recoveryPhone("+15555555556")
        .build();
    EnchantedLinkResponse expected = new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL);
    Sent sent = send(expected, () -> enchantedLinkService.signUp(MOCK_EMAIL, MOCK_URL, user, fullSignUpOptions(),
        EnchantedLinkSignUpOptions.builder().providerId("p1").templateId("t2").build()));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/signup/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertBody("{\"email\":\"" + MOCK_EMAIL + "\",\"loginId\":\"" + MOCK_EMAIL + "\",\"user\":{\"name\":\"n\","
        + "\"email\":\"" + MOCK_EMAIL + "\",\"loginId\":\"l1\",\"recoveryEmail\":\"r@domain.com\","
        + "\"recoveryPhone\":\"+15555555556\"},\"redirectUrl\":\"" + MOCK_URL + "\",\"providerId\":\"p1\","
        + "\"templateId\":\"t2\",\"loginOptions\":" + SIGN_UP_OPTIONS_JSON + "}", sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void signUpWithPhoneWithOptionsSendsSpecBody() {
    PhoneEnchantedLinkResponse expected = new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE);
    Sent sent = send(expected, () -> enchantedLinkService.signUpWithPhone(MOCK_PHONE, MOCK_URL, null,
        fullSignUpOptions(), null));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/signup/sms");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertBody("{\"phone\":\"" + MOCK_PHONE + "\",\"loginId\":\"" + MOCK_PHONE + "\",\"user\":{\"phone\":\""
        + MOCK_PHONE + "\"},\"redirectUrl\":\"" + MOCK_URL + "\",\"loginOptions\":" + SIGN_UP_OPTIONS_JSON + "}",
        sent.body);
    assertEquals(expected, sent.result);
  }

  private static final EnchantedLinkUpdateOptions UPDATE_OPTIONS = EnchantedLinkUpdateOptions.builder()
      .providerId("p1").templateId("t1").locale("en").failOnConflict(true).build();

  private static final String UPDATE_OPTIONS_JSON = "\"redirectUrl\":\"" + MOCK_URL + "\",\"addToLoginIDs\":true,"
      + "\"onMergeUseExisting\":false,\"providerId\":\"p1\",\"templateId\":\"t1\",\"templateOptions\":{\"k\":\"v\"},"
      + "\"locale\":\"en\",\"failOnConflict\":true";

  @Test
  void updateUserEmailWithOptionsSendsSpecBody() {
    EnchantedLinkResponse expected = new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL);
    Sent sent = send(expected, () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_URL,
        MOCK_REFRESH_TOKEN, new UpdateOptions(true, false), Collections.singletonMap("k", "v"), UPDATE_OPTIONS));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/update/email");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertBody("{\"loginId\":\"" + MOCK_EMAIL + "\",\"email\":\"" + UPDATE_MOCK_EMAIL + "\","
        + UPDATE_OPTIONS_JSON + "}", sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void updateUserPhoneWithOptionsSendsSpecBody() {
    PhoneEnchantedLinkResponse expected = new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE);
    Sent sent = send(expected, () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_URL,
        MOCK_REFRESH_TOKEN, new UpdateOptions(true, false), Collections.singletonMap("k", "v"), UPDATE_OPTIONS));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/update/phone/sms");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertBody("{\"loginId\":\"" + MOCK_EMAIL + "\",\"phone\":\"" + MOCK_PHONE + "\"," + UPDATE_OPTIONS_JSON + "}",
        sent.body);
    assertEquals(expected, sent.result);
  }

  @Test
  void updateWithOptionsRejectsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail("", MOCK_EMAIL, MOCK_URL, MOCK_REFRESH_TOKEN, null, null,
            UPDATE_OPTIONS)).getMessage());
    assertEquals("The Email argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_URL, MOCK_REFRESH_TOKEN, null, null,
            UPDATE_OPTIONS)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_URL, "", null, null,
            UPDATE_OPTIONS)).getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone("", MOCK_PHONE, MOCK_URL, MOCK_REFRESH_TOKEN, null, null,
            UPDATE_OPTIONS)).getMessage());
    assertEquals("The Phone argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, "abc", MOCK_URL, MOCK_REFRESH_TOKEN, null, null,
            UPDATE_OPTIONS)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_URL, null, null, null,
            UPDATE_OPTIONS)).getMessage());
  }

  @Test
  void verifyWithPkceVerifierSendsSpecBody() {
    Sent sent = send(new EmptyResponse(), () -> {
      enchantedLinkService.verify("token", PKCE);
      return null;
    });
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/verify");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertBody("{\"token\":\"token\",\"pkceVerifier\":\"" + PKCE + "\"}", sent.body);
  }

  @Test
  @SneakyThrows
  void getSessionReturnsFullResponse() {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"d\",\"cookiePath\":\"/\","
        + "\"cookieMaxAge\":10,\"cookieExpiration\":20,\"firstSeen\":true,\"sessionExpiration\":30,"
        + "\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"t1\",\"trustedDeviceJwt\":\"tdj\","
        + "\"nextRefreshSeconds\":40,\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\","
        + "\"sessionCookieDomain\":\"scd\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\"},"
        + "\"user\":{\"userId\":\"u1\",\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true},"
        + "\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,"
        + "\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,\"consentExpiration\":50,"
        + "\"recoveryEmail\":\"re\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"rp\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":60,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,"
        + "\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"rn\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\","
        + "\"samlTenantId\":\"sat\",\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":70,"
        + "\"userTenants\":[{\"tenantId\":\"t1\",\"permissions\":[\"tp\"],\"roleIds\":[\"tr\"]}]},"
        + "\"unknownField\":1}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, JWTResponse.class);
    AuthenticationInfo[] info = new AuthenticationInfo[1];
    Sent sent;
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      sent = send(jwtResponse, () -> info[0] = enchantedLinkService.getSession("ref"));
    }
    assertThat(sent.uri.getPath()).isEqualTo("/v1/auth/enchantedlink/pending-session");
    assertThat(sent.auth).isEqualTo("Bearer " + PROJECT_ID);
    assertBody("{\"pendingRef\":\"ref\"}", sent.body);

    AuthenticationInfo res = info[0];
    assertThat(res.getCookieDomain()).isEqualTo("d");
    assertThat(res.getCookiePath()).isEqualTo("/");
    assertThat(res.getCookieMaxAge()).isEqualTo(10);
    assertThat(res.getCookieExpiration()).isEqualTo(20);
    assertThat(res.getSessionExpiration()).isEqualTo(30);
    assertThat(res.getExternalToken()).isEqualTo("ext");
    assertThat(res.getClaims()).containsEntry("c", "v");
    assertThat(res.getTenantSSOID()).isEqualTo("t1");
    assertThat(res.getTrustedDeviceJwt()).isEqualTo("tdj");
    assertThat(res.getNextRefreshSeconds()).isEqualTo(40);
    assertThat(res.getCookieName()).isEqualTo("cn");
    assertThat(res.getSessionCookieName()).isEqualTo("scn");
    assertThat(res.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(res.getUnsavedSSO()).isTrue();
    assertThat(res.getKnownDeviceJwt()).isEqualTo("kdj");
    assertThat(res.getSsoAuthenticationOnly()).isTrue();
    assertThat(res.getUserCreationDeferred()).isTrue();
    assertThat(res.getFirstSeen()).isTrue();
    assertThat(res.getIdpResponse().getSamlResponse()).isEqualTo("sr");
    assertThat(res.getIdpResponse().getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(res.getIdpResponse().getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(res.getIdpResponse().getOidcResponse()).isEqualTo("or");
    assertThat(res.getIdpResponse().getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(res.getIdpResponse().getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = res.getUser();
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getExternalIds()).containsExactly("e1");
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getSCIM()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("p1");
    assertThat(user.getOIDC()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(50L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("rp");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(60L);
    assertThat(user.getRoleIds()).containsExactly("r1");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    assertThat(user.getUserFamilies().get(0).getFamilyId()).isEqualTo("f1");
    assertThat(user.getUserFamilies().get(0).getRoleNames()).containsExactly("rn");
    assertThat(user.getUserFamilies().get(0).getPermissions()).containsExactly("fp");
    assertThat(user.getUserFamilies().get(0).getFamilyScopedAttributes()).containsEntry("a", "b");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(70L);
    assertThat(user.getUserTenants().get(0).getPermissions()).containsExactly("tp");
    assertThat(user.getUserTenants().get(0).getRoleIds()).containsExactly("tr");
    // Response-only tenant fields are never sent back on requests
    assertThat(MAPPER.writeValueAsString(user.getUserTenants().get(0))).isEqualTo("{\"tenantId\":\"t1\"}");
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
