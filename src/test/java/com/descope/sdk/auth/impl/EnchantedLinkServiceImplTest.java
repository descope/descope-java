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
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.EnchantedLinkSessionBody;
import com.descope.model.enchantedlink.PhoneEnchantedLinkResponse;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdateEmailRequest;
import com.descope.model.magiclink.request.UpdatePhoneRequest;
import com.descope.model.magiclink.request.VerifyRequest;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

  private static final String PROVIDER_ID = "some-provider";
  private static final String TEMPLATE_ID = "some-template";
  private static final String SSO_APP_ID = "some-sso-app";

  // Mocks the proxy to answer with the given response and records the Authorization header
  @SuppressWarnings("unchecked")
  private static ApiProxy mockProxy(MockedStatic<ApiProxyBuilder> builder, Object response,
      AtomicReference<String> authHeader) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
    return apiProxy;
  }

  private static void assertSignInRequest(SignInRequest sent, String loginId) {
    assertThat(sent.getLoginId()).isEqualTo(loginId);
    assertThat(sent.getUri()).isEqualTo(MOCK_DOMAIN);
    assertThat(sent.getProviderId()).isEqualTo(PROVIDER_ID);
    assertThat(sent.getTemplateId()).isEqualTo(TEMPLATE_ID);
    assertThat(sent.getSsoAppId()).isEqualTo(SSO_APP_ID);
    assertThat(sent.getUseRecovery()).isTrue();
  }

  @Test
  void signInWithOptionsSendsThemToEmailRoute() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL), authHeader);
      LoginOptions loginOptions = LoginOptions.builder().templateId("nested-template").build();

      final EnchantedLinkResponse response = enchantedLinkService.signIn(MOCK_EMAIL, MOCK_DOMAIN, null, loginOptions,
          PROVIDER_ID, TEMPLATE_ID, SSO_APP_ID, true);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignInRequest> bodyCaptor = ArgumentCaptor.forClass(SignInRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signin/email");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertSignInRequest(bodyCaptor.getValue(), MOCK_EMAIL);
      assertThat(bodyCaptor.getValue().getLoginOptions().getTemplateId()).isEqualTo("nested-template");
      assertThat(response.getPendingRef()).isEqualTo("ref");
      assertThat(response.getLinkId()).isEqualTo("12");
      assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
    }
  }

  @Test
  void signInWithPhoneWithOptionsSendsThemToSmsRoute() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE),
          authHeader);
      LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();

      final PhoneEnchantedLinkResponse response = enchantedLinkService.signInWithPhone(MOCK_PHONE, MOCK_DOMAIN,
          MOCK_REFRESH_TOKEN, loginOptions, PROVIDER_ID, TEMPLATE_ID, SSO_APP_ID, true);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignInRequest> bodyCaptor = ArgumentCaptor.forClass(SignInRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signin/sms");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
      assertSignInRequest(bodyCaptor.getValue(), MOCK_PHONE);
      assertThat(response.getPendingRef()).isEqualTo("ref");
      assertThat(response.getLinkId()).isEqualTo("12");
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void signInWithOptionsRejectsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signIn("", MOCK_DOMAIN, null, null, PROVIDER_ID, TEMPLATE_ID, SSO_APP_ID,
            true)).getMessage());
    assertEquals("The token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signInWithPhone(MOCK_PHONE, MOCK_DOMAIN, null,
            LoginOptions.builder().mfa(true).build(), PROVIDER_ID, TEMPLATE_ID, SSO_APP_ID, true)).getMessage());
  }

  @Test
  void signUpOrInWithOptionsSendsThemToEmailAndSmsRoutes() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL), authHeader);

      final EnchantedLinkResponse response = enchantedLinkService.signUpOrIn(MOCK_EMAIL, MOCK_DOMAIN, PROVIDER_ID,
          TEMPLATE_ID, SSO_APP_ID, true);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignInRequest> bodyCaptor = ArgumentCaptor.forClass(SignInRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/email");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertSignInRequest(bodyCaptor.getValue(), MOCK_EMAIL);
      assertThat(response.getPendingRef()).isEqualTo("ref");
      assertThat(response.getLinkId()).isEqualTo("12");
      assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
    }
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE),
          authHeader);

      final PhoneEnchantedLinkResponse response = enchantedLinkService.signUpOrInWithPhone(MOCK_PHONE, MOCK_DOMAIN,
          PROVIDER_ID, TEMPLATE_ID, SSO_APP_ID, true);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignInRequest> bodyCaptor = ArgumentCaptor.forClass(SignInRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup-in/sms");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertSignInRequest(bodyCaptor.getValue(), MOCK_PHONE);
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void signUpOrInWithOptionsRejectsEmptyLoginId() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrInWithPhone("", MOCK_DOMAIN, PROVIDER_ID, TEMPLATE_ID, SSO_APP_ID,
            true)).getMessage());
  }

  @Test
  void signUpWithOptionsSendsThemToEmailAndSmsRoutes() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    SignUpOptions signUpOptions = new SignUpOptions(null, null);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL), authHeader);

      EnchantedLinkResponse response = enchantedLinkService.signUp(MOCK_EMAIL, MOCK_DOMAIN, null, signUpOptions,
          PROVIDER_ID, TEMPLATE_ID);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignUpRequest> bodyCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup/email");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      SignUpRequest sent = bodyCaptor.getValue();
      assertThat(sent.getEmail()).isEqualTo(MOCK_EMAIL);
      assertThat(sent.getLoginId()).isEqualTo(MOCK_EMAIL);
      assertThat(sent.getUri()).isEqualTo(MOCK_DOMAIN);
      assertThat(sent.getLoginOptions()).isSameAs(signUpOptions);
      assertThat(sent.getProviderId()).isEqualTo(PROVIDER_ID);
      assertThat(sent.getTemplateId()).isEqualTo(TEMPLATE_ID);
      assertThat(response.getPendingRef()).isEqualTo("ref");
      assertThat(response.getLinkId()).isEqualTo("12");
      assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
    }
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE),
          authHeader);

      PhoneEnchantedLinkResponse response = enchantedLinkService.signUpWithPhone(MOCK_PHONE, MOCK_DOMAIN, null,
          signUpOptions, PROVIDER_ID, TEMPLATE_ID);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<SignUpRequest> bodyCaptor = ArgumentCaptor.forClass(SignUpRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/signup/sms");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      SignUpRequest sent = bodyCaptor.getValue();
      assertThat(sent.getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getProviderId()).isEqualTo(PROVIDER_ID);
      assertThat(sent.getTemplateId()).isEqualTo(TEMPLATE_ID);
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void updateUserEmailWithOptionsSendsThem() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new EnchantedLinkResponse("ref", "12", MOCK_MASKED_EMAIL), authHeader);

      EnchantedLinkResponse response = enchantedLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL,
          MOCK_DOMAIN, MOCK_REFRESH_TOKEN, new UpdateOptions(true, false), null, PROVIDER_ID, TEMPLATE_ID, "fr",
          true);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<UpdateEmailRequest> bodyCaptor = ArgumentCaptor.forClass(UpdateEmailRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/update/email");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
      UpdateEmailRequest sent = bodyCaptor.getValue();
      assertThat(sent.getLoginId()).isEqualTo(MOCK_EMAIL);
      assertThat(sent.getEmail()).isEqualTo(UPDATE_MOCK_EMAIL);
      assertThat(sent.isAddToLoginIds()).isTrue();
      assertThat(sent.getProviderId()).isEqualTo(PROVIDER_ID);
      assertThat(sent.getTemplateId()).isEqualTo(TEMPLATE_ID);
      assertThat(sent.getLocale()).isEqualTo("fr");
      assertThat(sent.getFailOnConflict()).isTrue();
      assertThat(response.getPendingRef()).isEqualTo("ref");
      assertThat(response.getLinkId()).isEqualTo("12");
      assertThat(response.getMaskedEmail()).isEqualTo(MOCK_MASKED_EMAIL);
    }
  }

  @Test
  void updateUserPhoneWithOptionsSendsThem() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new PhoneEnchantedLinkResponse("ref", "12", MOCK_MASKED_PHONE),
          authHeader);

      PhoneEnchantedLinkResponse response = enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE,
          MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null, null, PROVIDER_ID, TEMPLATE_ID, "fr", false);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<UpdatePhoneRequest> bodyCaptor = ArgumentCaptor.forClass(UpdatePhoneRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(PhoneEnchantedLinkResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/update/phone/sms");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
      UpdatePhoneRequest sent = bodyCaptor.getValue();
      assertThat(sent.getPhone()).isEqualTo(MOCK_PHONE);
      assertThat(sent.getProviderId()).isEqualTo(PROVIDER_ID);
      assertThat(sent.getTemplateId()).isEqualTo(TEMPLATE_ID);
      assertThat(sent.getLocale()).isEqualTo("fr");
      assertThat(sent.getFailOnConflict()).isFalse();
      assertThat(response.getPendingRef()).isEqualTo("ref");
      assertThat(response.getLinkId()).isEqualTo("12");
      assertThat(response.getMaskedPhone()).isEqualTo(MOCK_MASKED_PHONE);
    }
  }

  @Test
  void updateWithOptionsRejectsInvalidArguments() {
    assertEquals("The Email argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserEmail(MOCK_EMAIL, "abc", MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null, null,
            PROVIDER_ID, TEMPLATE_ID, "fr", true)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, "", null, null,
            PROVIDER_ID, TEMPLATE_ID, "fr", true)).getMessage());
  }

  @Test
  void verifyWithPkceVerifierSendsIt() throws Exception {
    AtomicReference<String> authHeader = new AtomicReference<>();
    byte[] verifier = "verifier".getBytes(StandardCharsets.UTF_8);
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, new EmptyResponse(), authHeader);

      enchantedLinkService.verify("token", verifier);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<VerifyRequest> bodyCaptor = ArgumentCaptor.forClass(VerifyRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(EmptyResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/verify");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertThat(new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
          .writeValueAsString(bodyCaptor.getValue()))
          .isEqualTo("{\"token\":\"token\",\"pkceVerifier\":\"dmVyaWZpZXI=\"}");
    }
  }

  @Test
  void requestsWithoutNewOptionsSerializeAsBefore() throws Exception {
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    assertThat(mapper.writeValueAsString(new SignInRequest(MOCK_DOMAIN, MOCK_EMAIL, null)))
        .isEqualTo("{\"loginId\":\"" + MOCK_EMAIL + "\",\"URI\":\"" + MOCK_DOMAIN + "\"}");
    assertThat(mapper.writeValueAsString(new VerifyRequest("token"))).isEqualTo("{\"token\":\"token\"}");
  }

  @Test
  void getSessionReturnsJwtResponseFields() throws Exception {
    JWTResponse jwtResponse = new ObjectMapper().readValue("{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\","
        + "\"sessionExpiration\":10,\"externalToken\":\"ext\",\"claims\":{\"k\":\"v\"},"
        + "\"tenantSSOID\":\"sso-id\",\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":20,"
        + "\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\","
        + "\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\",\"userCreationDeferred\":true,"
        + "\"externalPasskey\":true}", JWTResponse.class);
    AtomicReference<String> authHeader = new AtomicReference<>();
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      ApiProxy apiProxy = mockProxy(builder, jwtResponse, authHeader);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = enchantedLinkService.getSession("testRef");
      }
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<EnchantedLinkSessionBody> bodyCaptor = ArgumentCaptor.forClass(EnchantedLinkSessionBody.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(JWTResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/enchantedlink/pending-session");
      assertThat(bodyCaptor.getValue().getPendingRef()).isEqualTo("testRef");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    }
    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getSessionExpiration()).isEqualTo(10);
    assertThat(info.getExternalToken()).isEqualTo("ext");
    assertThat(info.getClaims()).containsEntry("k", "v");
    assertThat(info.getTenantSsoId()).isEqualTo("sso-id");
    assertThat(info.getTrustedDeviceJwt()).isEqualTo("tdj");
    assertThat(info.getNextRefreshSeconds()).isEqualTo(20);
    assertThat(info.getCookieName()).isEqualTo("cn");
    assertThat(info.getSessionCookieName()).isEqualTo("scn");
    assertThat(info.getSessionCookieDomain()).isEqualTo("scd");
    assertThat(info.getUnsavedSso()).isTrue();
    assertThat(info.getKnownDeviceJwt()).isEqualTo("kdj");
    assertThat(info.getUserCreationDeferred()).isTrue();
    assertThat(info.getExternalPasskey()).isTrue();
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
