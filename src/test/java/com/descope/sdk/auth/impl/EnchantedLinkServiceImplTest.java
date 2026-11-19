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
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.EnchantedLinkSignInOptions;
import com.descope.model.enchantedlink.EnchantedLinkSignUpOptions;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

  private static final ObjectMapper REQUEST_JSON = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final byte[] PKCE = "pkce".getBytes(StandardCharsets.UTF_8);
  private static final String PKCE_BASE64 = "cGtjZQ==";

  private static class Sent {
    private String authorization;
    private String path;
    private JsonNode body;
    private Object response;
  }

  // Runs one call and captures the Authorization header, route and JSON body it sent
  @SuppressWarnings("unchecked")
  private Sent send(Object response, Function<EnchantedLinkService, Object> call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    Sent sent = new Sent();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.authorization = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      sent.response = call.apply(enchantedLinkService);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), any());
    sent.path = uriCaptor.getValue().getPath();
    sent.body = wire(bodyCaptor.getValue());
    return sent;
  }

  // The JSON the proxy sends for a request body
  private static JsonNode wire(Object body) {
    try {
      return REQUEST_JSON.readTree(REQUEST_JSON.writeValueAsString(body));
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static JsonNode json(String singleQuoted) {
    try {
      return REQUEST_JSON.readTree(singleQuoted.replace('\'', '"'));
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static LoginOptions newLoginOptions(boolean stepup) {
    return LoginOptions.builder().stepup(stepup).ssoAppId("app").pkceChallenge(PKCE).tenantId("tenant").build();
  }

  private static void assertSignInBody(JsonNode body, String loginId) {
    assertThat(body.get("loginId").asText()).isEqualTo(loginId);
    assertThat(body.get("URI").asText()).isEqualTo(MOCK_DOMAIN);
    assertThat(body.at("/loginOptions/ssoAppId").asText()).isEqualTo("app");
    assertThat(body.at("/loginOptions/pkceChallenge").asText()).isEqualTo(PKCE_BASE64);
    assertThat(body.at("/loginOptions/tenantId").asText()).isEqualTo("tenant");
    assertThat(body.get("providerId").asText()).isEqualTo("provider");
    assertThat(body.get("templateId").asText()).isEqualTo("template");
    assertThat(body.get("ssoAppId").asText()).isEqualTo("app");
    assertThat(body.get("useRecovery").asBoolean()).isTrue();
  }

  private static EnchantedLinkSignInOptions newSignInOptions() {
    return new EnchantedLinkSignInOptions("provider", "template", "app", true);
  }

  @Test
  void signInWithOptionsSendsAllFields() {
    EnchantedLinkResponse response = new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL);
    Sent sent = send(response, s -> s.signIn(MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, newLoginOptions(true),
        newSignInOptions()));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/signin/email");
    assertSignInBody(sent.body, MOCK_EMAIL);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void signInWithPhoneWithOptionsSendsAllFields() {
    PhoneEnchantedLinkResponse response = new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE);
    Sent sent = send(response, s -> s.signInWithPhone(MOCK_PHONE, MOCK_DOMAIN, null, newLoginOptions(false),
        newSignInOptions()));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/signin/sms");
    assertSignInBody(sent.body, MOCK_PHONE);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void signUpOrInWithOptionsSendsAllFields() {
    EnchantedLinkResponse response = new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL);
    Sent sent = send(response, s -> s.signUpOrIn(MOCK_EMAIL, MOCK_DOMAIN, null, newLoginOptions(false),
        newSignInOptions()));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/signup-in/email");
    assertSignInBody(sent.body, MOCK_EMAIL);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void signUpOrInWithPhoneWithOptionsSendsAllFields() {
    PhoneEnchantedLinkResponse response = new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE);
    Sent sent = send(response, s -> s.signUpOrInWithPhone(MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
        newLoginOptions(true), newSignInOptions()));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/signup-in/sms");
    assertSignInBody(sent.body, MOCK_PHONE);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void signUpOrInWithOptionsRejectsInvalidArguments() {
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrIn("", MOCK_DOMAIN, null, null, newSignInOptions())).getMessage());
    assertEquals("The token argument is invalid", assertThrows(ServerCommonException.class,
        () -> enchantedLinkService.signUpOrInWithPhone(MOCK_PHONE, MOCK_DOMAIN, "", newLoginOptions(true), null))
        .getMessage());
  }

  @Test
  void signUpOrInWithoutOptionsSendsUnchangedBody() {
    Sent sent = send(new EnchantedLinkResponse(), s -> s.signUpOrIn(MOCK_EMAIL, MOCK_DOMAIN));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.body).isEqualTo(json("{'URI':'" + MOCK_DOMAIN + "','loginId':'" + MOCK_EMAIL + "'}"));
  }

  private static User newSignUpUser(String loginId) {
    return User.builder().loginId(loginId).name("name").recoveryEmail("recovery@domain.com")
        .recoveryPhone("+15555555556").build();
  }

  private static SignUpOptions newSignUpOptions() {
    return SignUpOptions.builder().locale("en").pkceChallenge(PKCE).templateId("login-template").tenantId("tenant")
        .build();
  }

  private static void assertSignUpBody(JsonNode body, String loginId) {
    assertThat(body.get("loginId").asText()).isEqualTo(loginId);
    assertThat(body.at("/user/loginId").asText()).isEqualTo(loginId);
    assertThat(body.at("/user/recoveryEmail").asText()).isEqualTo("recovery@domain.com");
    assertThat(body.at("/user/recoveryPhone").asText()).isEqualTo("+15555555556");
    assertThat(body.get("providerId").asText()).isEqualTo("provider");
    assertThat(body.get("templateId").asText()).isEqualTo("template");
    assertThat(body.at("/loginOptions/locale").asText()).isEqualTo("en");
    assertThat(body.at("/loginOptions/pkceChallenge").asText()).isEqualTo(PKCE_BASE64);
    assertThat(body.at("/loginOptions/templateId").asText()).isEqualTo("login-template");
    assertThat(body.at("/loginOptions/tenantId").asText()).isEqualTo("tenant");
  }

  @Test
  void signUpWithOptionsSendsAllFields() {
    EnchantedLinkResponse response = new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL);
    Sent sent = send(response, s -> s.signUp(MOCK_EMAIL, MOCK_DOMAIN, newSignUpUser(MOCK_EMAIL), newSignUpOptions(),
        new EnchantedLinkSignUpOptions("provider", "template")));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/signup/email");
    assertSignUpBody(sent.body, MOCK_EMAIL);
    assertThat(sent.body.get("email").asText()).isEqualTo(MOCK_EMAIL);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void signUpWithPhoneWithOptionsSendsAllFields() {
    PhoneEnchantedLinkResponse response = new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE);
    Sent sent = send(response, s -> s.signUpWithPhone(MOCK_PHONE, MOCK_DOMAIN, newSignUpUser(MOCK_PHONE),
        newSignUpOptions(), new EnchantedLinkSignUpOptions("provider", "template")));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/signup/sms");
    assertSignUpBody(sent.body, MOCK_PHONE);
    assertThat(sent.body.get("phone").asText()).isEqualTo(MOCK_PHONE);
    assertThat(sent.response).isEqualTo(response);
  }

  private static UpdateOptions newUpdateOptions() {
    return UpdateOptions.builder().providerId("provider").templateId("template").locale("en").failOnConflict(true)
        .build();
  }

  private static void assertUpdateBody(JsonNode body) {
    assertThat(body.get("providerId").asText()).isEqualTo("provider");
    assertThat(body.get("templateId").asText()).isEqualTo("template");
    assertThat(body.get("locale").asText()).isEqualTo("en");
    assertThat(body.get("failOnConflict").asBoolean()).isTrue();
  }

  @Test
  void updateUserEmailWithOptionsSendsAllFields() {
    EnchantedLinkResponse response = new EnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_EMAIL);
    Sent sent = send(response, s -> s.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
        newUpdateOptions()));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/update/email");
    assertThat(sent.body.get("email").asText()).isEqualTo(MOCK_EMAIL);
    assertUpdateBody(sent.body);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void updateUserPhoneWithOptionsSendsAllFields() {
    PhoneEnchantedLinkResponse response = new PhoneEnchantedLinkResponse(MOCK_URL, MOCK_URL, MOCK_MASKED_PHONE);
    Sent sent = send(response, s -> s.updateUserPhone(MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
        newUpdateOptions()));
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/update/phone/sms");
    assertThat(sent.body.get("phone").asText()).isEqualTo(MOCK_PHONE);
    assertUpdateBody(sent.body);
    assertThat(sent.response).isEqualTo(response);
  }

  @Test
  void updateUserEmailWithoutNewOptionsOmitsThem() {
    Sent sent = send(new EnchantedLinkResponse(), s -> s.updateUserEmail(MOCK_EMAIL, MOCK_EMAIL, MOCK_DOMAIN,
        MOCK_REFRESH_TOKEN, null));
    assertThat(sent.body.has("providerId")).isFalse();
    assertThat(sent.body.has("templateId")).isFalse();
    assertThat(sent.body.has("locale")).isFalse();
    assertThat(sent.body.has("failOnConflict")).isFalse();
  }

  @Test
  void verifyWithPkceVerifierSendsIt() {
    Sent sent = send(new EmptyResponse(), s -> {
      s.verify("token", PKCE);
      return null;
    });
    assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
    assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/verify");
    assertThat(sent.body).isEqualTo(json("{'token':'token','pkceVerifier':'" + PKCE_BASE64 + "'}"));
  }

  private static final String FULL_JWT_RESPONSE = ("{'sessionJwt':'s','refreshJwt':'r','cookieDomain':'d',"
      + "'cookiePath':'/p','cookieMaxAge':1,'cookieExpiration':2,'firstSeen':true,'sessionExpiration':3,"
      + "'externalToken':'ext','claims':{'c':'v'},'tenantSSOID':'sso','trustedDeviceJwt':'tdj',"
      + "'nextRefreshSeconds':4,'cookieName':'cn','sessionCookieName':'scn','sessionCookieDomain':'scd',"
      + "'unsavedSSO':true,'knownDeviceJwt':'kdj','ssoAuthenticationOnly':true,'userCreationDeferred':true,"
      + "'unknownField':1,"
      + "'idpResponse':{'samlResponse':'sr','samlGeneratedUser':'sgu','samlGeneratedRoles':'sgr',"
      + "'oidcResponse':'or','oidcGeneratedUser':'ogu','oidcGeneratedRoles':'ogr','idpGroups':['g']},"
      + "'user':{'userId':'u1','userTenants':[{'tenantId':'t1','permissions':['p1'],'roleIds':['r1']}],"
      + "'externalIds':['e1'],'TOTP':true,'SAML':true,'OAuth':{'google':true},'webauthn':true,'password':true,"
      + "'editable':true,'SCIM':true,'push':true,'permissions':['p2'],'OIDC':true,'consentExpiration':5,"
      + "'recoveryEmail':'re@domain.com','verifiedRecoveryEmail':true,'recoveryPhone':'+15555555556',"
      + "'verifiedRecoveryPhone':true,'modifiedTime':6,'roleIds':['r2'],'recoveryCodes':true,'dependent':true,"
      + "'userFamilies':[{'familyId':'f1','roleNames':['fr'],'permissions':['fp'],"
      + "'familyScopedAttributes':{'a':'b'}}],'scimTenantId':'st','samlTenantId':'sat','oidcTenantId':'ot',"
      + "'consentTenantId':'ct','lastLoginTime':7}}").replace('\'', '"');

  @Test
  void getSessionExposesAllResponseFields() throws Exception {
    JWTResponse jwtResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(FULL_JWT_RESPONSE, JWTResponse.class);
    AuthenticationInfo info;
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      Sent sent = send(jwtResponse, s -> s.getSession("ref"));
      assertThat(sent.authorization).isEqualTo("Bearer " + PROJECT_ID);
      assertThat(sent.path).isEqualTo("/v1/auth/enchantedlink/pending-session");
      assertThat(sent.body).isEqualTo(json("{'pendingRef':'ref'}"));
      info = (AuthenticationInfo) sent.response;
    }
    assertThat(info.getCookieDomain()).isEqualTo("d");
    assertThat(info.getCookiePath()).isEqualTo("/p");
    assertThat(info.getCookieMaxAge()).isEqualTo(1);
    assertThat(info.getCookieExpiration()).isEqualTo(2);
    assertThat(info.getFirstSeen()).isTrue();
    assertThat(info.getSessionExpiration()).isEqualTo(3);
    assertThat(info.getExternalToken()).isEqualTo("ext");
    assertThat(info.getClaims()).containsEntry("c", "v");
    assertThat(info.getTenantSSOID()).isEqualTo("sso");
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
    assertThat(idp.getIdpGroups()).containsExactly("g");
    assertThat(idp.getSamlResponse()).isEqualTo("sr");
    assertThat(idp.getSamlGeneratedUser()).isEqualTo("sgu");
    assertThat(idp.getSamlGeneratedRoles()).isEqualTo("sgr");
    assertThat(idp.getOidcResponse()).isEqualTo("or");
    assertThat(idp.getOidcGeneratedUser()).isEqualTo("ogu");
    assertThat(idp.getOidcGeneratedRoles()).isEqualTo("ogr");

    UserResponse user = info.getUser();
    assertThat(user.getUserTenants().get(0).getPermissions()).containsExactly("p1");
    assertThat(user.getUserTenants().get(0).getRoleIds()).containsExactly("r1");
    assertThat(user.getExternalIds()).containsExactly("e1");
    assertThat(user.getTOTP()).isTrue();
    assertThat(user.getSAML()).isTrue();
    assertThat(user.getOAuth()).containsEntry("google", true);
    assertThat(user.getWebauthn()).isTrue();
    assertThat(user.getPassword()).isTrue();
    assertThat(user.getEditable()).isTrue();
    assertThat(user.getScim()).isTrue();
    assertThat(user.getPush()).isTrue();
    assertThat(user.getPermissions()).containsExactly("p2");
    assertThat(user.getOidc()).isTrue();
    assertThat(user.getConsentExpiration()).isEqualTo(5L);
    assertThat(user.getRecoveryEmail()).isEqualTo("re@domain.com");
    assertThat(user.getVerifiedRecoveryEmail()).isTrue();
    assertThat(user.getRecoveryPhone()).isEqualTo("+15555555556");
    assertThat(user.getVerifiedRecoveryPhone()).isTrue();
    assertThat(user.getModifiedTime()).isEqualTo(6L);
    assertThat(user.getRoleIds()).containsExactly("r2");
    assertThat(user.getRecoveryCodes()).isTrue();
    assertThat(user.getDependent()).isTrue();
    UserFamily family = user.getUserFamilies().get(0);
    assertThat(family.getFamilyId()).isEqualTo("f1");
    assertThat(family.getRoleNames()).containsExactly("fr");
    assertThat(family.getPermissions()).containsExactly("fp");
    assertThat(family.getFamilyScopedAttributes()).containsEntry("a", "b");
    assertThat(user.getScimTenantId()).isEqualTo("st");
    assertThat(user.getSamlTenantId()).isEqualTo("sat");
    assertThat(user.getOidcTenantId()).isEqualTo("ot");
    assertThat(user.getConsentTenantId()).isEqualTo("ct");
    assertThat(user.getLastLoginTime()).isEqualTo(7L);
  }

  @Test
  void responseOnlyTenantFieldsAreNotSent() {
    AssociatedTenant tenant = AssociatedTenant.builder().tenantId("t1").roleNames(Arrays.asList("r"))
        .permissions(Arrays.asList("p")).roleIds(Arrays.asList("id")).build();
    assertThat(wire(tenant)).isEqualTo(json("{'tenantId':'t1','roleNames':['r']}"));
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
