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
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.IDPResponse;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.MessageOptions;
import com.descope.model.magiclink.SignInOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.UpdateMessageOptions;
import com.descope.model.magiclink.request.VerifyRequest;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.MagicLinkTestUserResponse;
import com.descope.model.user.response.UserFamily;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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

  private static final ObjectMapper REQUEST_MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final String AUTH_MANAGEMENT_KEY = "auth-mgmt-key";

  private static final class Captured {
    private final AtomicReference<Supplier<String>> authHeader = new AtomicReference<>();
    private URI uri;
    private Map<String, Object> body;
  }

  private static Captured capture(ApiProxy apiProxy, Class<?> returnClz, Consumer<MagicLinkService> call) {
    return capture(TestUtils.getClient(), apiProxy, returnClz, call);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static Captured capture(Client client, ApiProxy apiProxy, Class<?> returnClz,
      Consumer<MagicLinkService> call) {
    Captured captured = new Captured();
    MagicLinkService service = AuthenticationServiceBuilder.buildServices(client).getMagicLinkService();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.authHeader.set(inv.getArgument(0));
        return apiProxy;
      });
      call.accept(service);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(returnClz));
    captured.uri = uriCaptor.getValue();
    captured.body = REQUEST_MAPPER.readValue(REQUEST_MAPPER.writeValueAsBytes(bodyCaptor.getValue()), Map.class);
    return captured;
  }

  private static Client clientWithAuthManagementKey() {
    Client client = TestUtils.getClient();
    client.setAuthManagementKey(AUTH_MANAGEMENT_KEY);
    return client;
  }

  private static LoginOptions fullLoginOptions() {
    return LoginOptions.builder()
        .stepup(true)
        .ssoAppId("sso-app")
        .pkceChallenge(new byte[] {1, 2, 3})
        .tenantId("T1")
        .build();
  }

  private static SignInOptions fullSignInOptions() {
    return SignInOptions.builder()
        .providerId("provider")
        .templateId("template")
        .ssoAppId("sso-app")
        .useRecovery(true)
        .build();
  }

  private static void assertSignInBody(Map<String, Object> body) {
    assertEquals(MOCK_EMAIL, body.get("loginId"));
    assertEquals(MOCK_DOMAIN, body.get("URI"));
    assertEquals("provider", body.get("providerId"));
    assertEquals("template", body.get("templateId"));
    assertEquals("sso-app", body.get("ssoAppId"));
    assertEquals(true, body.get("useRecovery"));
    @SuppressWarnings("unchecked")
    Map<String, Object> loginOptions = (Map<String, Object>) body.get("loginOptions");
    assertEquals(true, loginOptions.get("stepup"));
    assertEquals("sso-app", loginOptions.get("ssoAppId"));
    assertEquals("AQID", loginOptions.get("pkceChallenge"));
    assertEquals("T1", loginOptions.get("tenantId"));
  }

  @Test
  void testSignInWithSignInOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    String[] masked = new String[1];
    Captured captured = capture(clientWithAuthManagementKey(), apiProxy, MaskedEmailRes.class,
        s -> masked[0] = s.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
            fullLoginOptions(), fullSignInOptions()));
    assertEquals(MOCK_MASKED_EMAIL, masked[0]);
    assertEquals("/v1/auth/magiclink/signin/email", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY,
        captured.authHeader.get().get());
    assertSignInBody(captured.body);
  }

  @Test
  void testSignInWithSignInOptionsOmitsUnset() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    Captured captured = capture(apiProxy, MaskedPhoneRes.class,
        s -> s.signIn(DeliveryMethod.WHATSAPP, MOCK_PHONE, null, null, null, new SignInOptions()));
    assertEquals("/v1/auth/magiclink/signin/whatsapp", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID, captured.authHeader.get().get());
    assertEquals(mapOf("loginId", MOCK_PHONE), captured.body);
  }

  @Test
  void testSignInWithSignInOptionsForStepupWithoutToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null,
            fullLoginOptions(), fullSignInOptions()));
    assertNotNull(thrown);
    assertEquals("The token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    String[] masked = new String[1];
    Captured captured = capture(clientWithAuthManagementKey(), apiProxy, MaskedEmailRes.class,
        s -> masked[0] = s.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN,
            fullLoginOptions(), fullSignInOptions()));
    assertEquals(MOCK_MASKED_EMAIL, masked[0]);
    assertEquals("/v1/auth/magiclink/signup-in/email", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY,
        captured.authHeader.get().get());
    assertSignInBody(captured.body);
  }

  @Test
  void testSignUpOrInWithOptionsWithoutStepup() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    Captured captured = capture(apiProxy, MaskedPhoneRes.class,
        s -> s.signUpOrIn(DeliveryMethod.SMS, MOCK_PHONE, null, MOCK_REFRESH_TOKEN,
            LoginOptions.builder().tenantId("T1").build(), null));
    assertEquals("/v1/auth/magiclink/signup-in/sms", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID, captured.authHeader.get().get());
    assertEquals(MOCK_PHONE, captured.body.get("loginId"));
    @SuppressWarnings("unchecked")
    Map<String, Object> loginOptions = (Map<String, Object>) captured.body.get("loginOptions");
    assertEquals("T1", loginOptions.get("tenantId"));
    assertThat(captured.body).doesNotContainKeys("providerId", "templateId", "ssoAppId", "useRecovery");
  }

  @Test
  void testSignUpOrInWithOptionsForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signUpOrIn(DeliveryMethod.EMAIL, "", MOCK_DOMAIN, null, null, fullSignInOptions()));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInWithOptionsForStepupWithoutToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signUpOrIn(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, null,
            fullLoginOptions(), null));
    assertNotNull(thrown);
    assertEquals("The token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    User user = User.builder()
        .name("someName")
        .phone(MOCK_PHONE)
        .loginId("login-id")
        .recoveryEmail(MOCK_EMAIL)
        .recoveryPhone(MOCK_PHONE)
        .build();
    SignUpOptions signUpOptions = SignUpOptions.builder()
        .locale("en")
        .pkceChallenge(new byte[] {1, 2, 3})
        .templateId("login-template")
        .tenantId("T1")
        .build();
    String[] masked = new String[1];
    Captured captured = capture(clientWithAuthManagementKey(), apiProxy, MaskedPhoneRes.class,
        s -> masked[0] = s.signUp(DeliveryMethod.SMS, MOCK_PHONE, MOCK_DOMAIN, user, signUpOptions,
            new MessageOptions("provider", "template")));
    assertEquals(MOCK_MASKED_PHONE, masked[0]);
    assertEquals("/v1/auth/magiclink/signup/sms", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY, captured.authHeader.get().get());
    assertEquals(MOCK_PHONE, captured.body.get("loginId"));
    assertEquals(MOCK_DOMAIN, captured.body.get("URI"));
    assertEquals("provider", captured.body.get("providerId"));
    assertEquals("template", captured.body.get("templateId"));
    assertEquals(mapOf("name", "someName", "phone", MOCK_PHONE, "loginId", "login-id",
        "recoveryEmail", MOCK_EMAIL, "recoveryPhone", MOCK_PHONE), captured.body.get("user"));
    assertEquals(mapOf("locale", "en", "pkceChallenge", "AQID", "templateId", "login-template",
        "tenantId", "T1"), captured.body.get("loginOptions"));
  }

  @Test
  void testSignUpWithOptionsForInvalidPhone() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.signUp(DeliveryMethod.SMS, "not-a-phone", MOCK_DOMAIN, null, null,
            new MessageOptions("provider", "template")));
    assertNotNull(thrown);
    assertEquals("The user.phone argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserEmailWithMessageOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedEmailRes(MOCK_MASKED_EMAIL)).when(apiProxy).post(any(), any(), any());
    String[] masked = new String[1];
    Captured captured = capture(clientWithAuthManagementKey(), apiProxy, MaskedEmailRes.class,
        s -> masked[0] = s.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, MOCK_REFRESH_TOKEN, null,
            mapOf("k", "v"), new UpdateMessageOptions("provider", "template", "en", true)));
    assertEquals(MOCK_MASKED_EMAIL, masked[0]);
    assertEquals("/v1/auth/magiclink/update/email", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY,
        captured.authHeader.get().get());
    Map<String, Object> expected = new HashMap<>();
    expected.put("email", UPDATE_MOCK_EMAIL);
    expected.put("loginId", MOCK_EMAIL);
    expected.put("URI", MOCK_DOMAIN);
    expected.put("crossDevice", false);
    expected.put("addToLoginIDs", false);
    expected.put("onMergeUseExisting", false);
    expected.put("templateOptions", mapOf("k", "v"));
    expected.put("providerId", "provider");
    expected.put("templateId", "template");
    expected.put("locale", "en");
    expected.put("failOnConflict", true);
    assertEquals(expected, captured.body);
  }

  @Test
  void testUpdateUserEmailWithMessageOptionsForEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.updateUserEmail(MOCK_EMAIL, UPDATE_MOCK_EMAIL, MOCK_DOMAIN, "", null, null,
            new UpdateMessageOptions()));
    assertNotNull(thrown);
    assertEquals("The Refresh Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserPhoneWithMessageOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new MaskedPhoneRes(MOCK_MASKED_PHONE)).when(apiProxy).post(any(), any(), any());
    String[] masked = new String[1];
    Captured captured = capture(apiProxy, MaskedPhoneRes.class,
        s -> masked[0] = s.updateUserPhone(DeliveryMethod.WHATSAPP, MOCK_EMAIL, MOCK_PHONE, null, MOCK_REFRESH_TOKEN,
            null, null, new UpdateMessageOptions("provider", "template", "en", false)));
    assertEquals(MOCK_MASKED_PHONE, masked[0]);
    assertEquals("/v1/auth/magiclink/update/phone/whatsapp", captured.uri.getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, captured.authHeader.get().get());
    Map<String, Object> expected = new HashMap<>();
    expected.put("phone", MOCK_PHONE);
    expected.put("loginId", MOCK_EMAIL);
    expected.put("crossDevice", false);
    expected.put("addToLoginIDs", false);
    expected.put("onMergeUseExisting", false);
    expected.put("providerId", "provider");
    expected.put("templateId", "template");
    expected.put("locale", "en");
    expected.put("failOnConflict", false);
    assertEquals(expected, captured.body);
  }

  @Test
  void testUpdateUserPhoneWithMessageOptionsForInvalidMethod() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> magicLinkService.updateUserPhone(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_PHONE, MOCK_DOMAIN,
            MOCK_REFRESH_TOKEN, null, null, new UpdateMessageOptions()));
    assertNotNull(thrown);
    assertEquals("The Method argument is invalid", thrown.getMessage());
  }

  @SneakyThrows
  @Test
  void testVerifyFullResponse() {
    String json = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"d.com\",\"cookiePath\":\"/p\","
        + "\"cookieMaxAge\":10,\"cookieExpiration\":20,\"firstSeen\":true,\"sessionExpiration\":30,"
        + "\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"T1\",\"trustedDeviceJwt\":\"tdj\","
        + "\"nextRefreshSeconds\":40,\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\","
        + "\"sessionCookieDomain\":\"s.com\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\","
        + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
        + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\","
        + "\"idpGroups\":[\"g\"]},"
        + "\"user\":{\"userId\":\"U1\",\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true},"
        + "\"externalIds\":[\"e\"],\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,"
        + "\"push\":true,\"permissions\":[\"p\"],\"OIDC\":true,\"consentExpiration\":50,"
        + "\"recoveryEmail\":\"re@d.com\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+1555\","
        + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":60,\"roleIds\":[\"R1\"],\"recoveryCodes\":true,"
        + "\"dependent\":true,\"userFamilies\":[{\"familyId\":\"F1\",\"roleNames\":[\"rn\"],"
        + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"T2\","
        + "\"samlTenantId\":\"T3\",\"oidcTenantId\":\"T4\",\"consentTenantId\":\"T5\",\"lastLoginTime\":70,"
        + "\"userTenants\":[{\"tenantId\":\"T1\",\"permissions\":[\"tp\"],\"roleIds\":[\"TR1\"]}],"
        + "\"unknownField\":1}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());

    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = magicLinkService.verify("SomeToken");
      }
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uriCaptor.capture(), eq(new VerifyRequest("SomeToken")), eq(JWTResponse.class));
    assertEquals("/v1/auth/magiclink/verify", uriCaptor.getValue().getPath());

    assertEquals("d.com", info.getCookieDomain());
    assertEquals("/p", info.getCookiePath());
    assertEquals(10, info.getCookieMaxAge());
    assertEquals(20, info.getCookieExpiration());
    assertEquals(30, info.getSessionExpiration());
    assertEquals("ext", info.getExternalToken());
    assertEquals(mapOf("c", "v"), info.getClaims());
    assertEquals("T1", info.getTenantSSOID());
    assertEquals("tdj", info.getTrustedDeviceJwt());
    assertEquals(40, info.getNextRefreshSeconds());
    assertEquals("DSR", info.getCookieName());
    assertEquals("DS", info.getSessionCookieName());
    assertEquals("s.com", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("kdj", info.getKnownDeviceJwt());
    assertEquals(true, info.getSsoAuthenticationOnly());
    assertEquals(true, info.getUserCreationDeferred());

    IDPResponse idp = info.getIdpResponse();
    assertEquals("sr", idp.getSamlResponse());
    assertEquals("sgu", idp.getSamlGeneratedUser());
    assertEquals("sgr", idp.getSamlGeneratedRoles());
    assertEquals("or", idp.getOidcResponse());
    assertEquals("ogu", idp.getOidcGeneratedUser());
    assertEquals("ogr", idp.getOidcGeneratedRoles());

    UserResponse user = info.getUser();
    assertEquals(true, user.getTOTP());
    assertEquals(true, user.getSAML());
    assertEquals(mapOf("google", true), user.getOAuth());
    assertEquals(Arrays.asList("e"), user.getExternalIds());
    assertEquals(true, user.getWebauthn());
    assertEquals(true, user.getPassword());
    assertEquals(true, user.getEditable());
    assertEquals(true, user.getScim());
    assertEquals(true, user.getPush());
    assertEquals(Arrays.asList("p"), user.getPermissions());
    assertEquals(true, user.getOidc());
    assertEquals(50L, user.getConsentExpiration());
    assertEquals("re@d.com", user.getRecoveryEmail());
    assertEquals(true, user.getVerifiedRecoveryEmail());
    assertEquals("+1555", user.getRecoveryPhone());
    assertEquals(true, user.getVerifiedRecoveryPhone());
    assertEquals(60L, user.getModifiedTime());
    assertEquals(Arrays.asList("R1"), user.getRoleIds());
    assertEquals(true, user.getRecoveryCodes());
    assertEquals(true, user.getDependent());
    assertEquals(Collections.singletonList(new UserFamily("F1", Arrays.asList("rn"), Arrays.asList("fp"),
        mapOf("a", "b"))), user.getUserFamilies());
    assertEquals("T2", user.getScimTenantId());
    assertEquals("T3", user.getSamlTenantId());
    assertEquals("T4", user.getOidcTenantId());
    assertEquals("T5", user.getConsentTenantId());
    assertEquals(70L, user.getLastLoginTime());
    assertEquals(Arrays.asList("tp"), user.getUserTenants().get(0).getPermissions());
    assertEquals(Arrays.asList("TR1"), user.getUserTenants().get(0).getRoleIds());
  }

  @SneakyThrows
  @Test
  void testUserResponseReadsLegacyCasing() {
    UserResponse user = new ObjectMapper()
        .readValue("{\"totp\":true,\"saml\":false,\"oauth\":{\"google\":true}}", UserResponse.class);
    assertEquals(true, user.getTOTP());
    assertEquals(false, user.getSAML());
    assertEquals(mapOf("google", true), user.getOAuth());
  }
}
