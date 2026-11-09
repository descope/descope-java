package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_URL;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EnchantedLinkSessionBody;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.notp.NOTPOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPTemplates;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.notp.request.NOTPLoginOptions;
import com.descope.model.notp.request.NOTPSignInRequest;
import com.descope.model.notp.request.NOTPSignUpRequest;
import com.descope.model.notp.request.NOTPUpdateUserRequest;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.AuthUtils;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class NOTPServiceImplTest {
  private static final String PROVIDER = "whatsapp";
  private static final NOTPResponse MOCK_NOTP_RESPONSE = new NOTPResponse("pending-ref", MOCK_URL, "image");
  private static final NOTPTemplates TEMPLATES = new NOTPTemplates("verify", "success", "error");
  private static final NOTPOptions OPTIONS = new NOTPOptions("provider-id", TEMPLATES, "sso-app");

  private Client client;
  private NOTPService notpService;

  @BeforeEach
  void setUp() {
    client = TestUtils.getClient();
    notpService = AuthenticationServiceBuilder.buildServices(client).getNotpService();
  }

  @SuppressWarnings("unchecked")
  private ArgumentCaptor<Supplier<String>> authCaptor() {
    return ArgumentCaptor.forClass(Supplier.class);
  }

  private String bearer(String token) {
    return AuthUtils.getBearerHeader(PROJECT_ID, token, client.getAuthManagementKey());
  }

  private static void assertResponse(NOTPResponse response) {
    assertThat(response.getPendingRef()).isEqualTo("pending-ref");
    assertThat(response.getRedirectUrl()).isEqualTo(MOCK_URL);
    assertThat(response.getImage()).isEqualTo("image");
  }

  @Test
  void signInSendsRequest() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> auth = authCaptor();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(auth.capture(), any())).thenReturn(apiProxy);
      LoginOptions loginOptions = LoginOptions.builder().stepup(true).build();
      NOTPResponse response = notpService.signIn(PROVIDER, MOCK_PHONE, MOCK_REFRESH_TOKEN, loginOptions, OPTIONS);

      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<NOTPSignInRequest> body = ArgumentCaptor.forClass(NOTPSignInRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(NOTPResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/notp/whatsapp/signin");
      assertThat(auth.getValue().get()).isEqualTo(bearer(MOCK_REFRESH_TOKEN));
      assertEquals(new NOTPSignInRequest(PROVIDER, MOCK_PHONE, NOTPLoginOptions.from(loginOptions), "provider-id",
          TEMPLATES, "sso-app"), body.getValue());
      assertResponse(response);
    }
  }

  @Test
  void signInWithoutOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> auth = authCaptor();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(auth.capture(), any())).thenReturn(apiProxy);
      notpService.signIn(PROVIDER, null, null, null, null);

      ArgumentCaptor<NOTPSignInRequest> body = ArgumentCaptor.forClass(NOTPSignInRequest.class);
      verify(apiProxy, times(1)).post(any(), body.capture(), eq(NOTPResponse.class));
      assertThat(auth.getValue().get()).isEqualTo(bearer(null));
      assertEquals(NOTPSignInRequest.builder().provider(PROVIDER).build(), body.getValue());
    }
  }

  @Test
  void signInRejectsInvalidArguments() {
    assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.signIn("", MOCK_PHONE, null, null, null)).getMessage());
    assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.signIn("..", MOCK_PHONE, null, null, null)).getMessage());
    assertEquals("The token argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.signIn(PROVIDER, MOCK_PHONE, "", LoginOptions.builder().mfa(true).build(), null))
        .getMessage());
  }

  @Test
  void signUpSendsRequest() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> auth = authCaptor();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(auth.capture(), any())).thenReturn(apiProxy);
      SignUpOptions signUpOptions = new SignUpOptions(Collections.singletonMap("k", "v"),
          Collections.singletonMap("t", "o"));
      User user = User.builder().name("name").build();
      NOTPResponse response = notpService.signUp(PROVIDER, MOCK_PHONE, user, signUpOptions, OPTIONS);

      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<NOTPSignUpRequest> body = ArgumentCaptor.forClass(NOTPSignUpRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(NOTPResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/notp/whatsapp/signup");
      assertThat(auth.getValue().get()).isEqualTo(bearer(null));
      User expectedUser = User.builder().name("name").phone(MOCK_PHONE).build();
      assertEquals(new NOTPSignUpRequest(PROVIDER, MOCK_PHONE, MOCK_PHONE, expectedUser, "provider-id", TEMPLATES,
          "sso-app", signUpOptions), body.getValue());
      assertResponse(response);
    }
  }

  @Test
  void signUpRejectsInvalidArguments() {
    assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.signUp(" ", MOCK_PHONE, null, null, null)).getMessage());
  }

  @Test
  void signUpOrInSendsRequest() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> auth = authCaptor();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(auth.capture(), any())).thenReturn(apiProxy);
      SignUpOptions signUpOptions = new SignUpOptions(Collections.singletonMap("k", "v"),
          Collections.singletonMap("t", "o"));
      NOTPResponse response = notpService.signUpOrIn(PROVIDER, MOCK_PHONE, signUpOptions, OPTIONS);

      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<NOTPSignInRequest> body = ArgumentCaptor.forClass(NOTPSignInRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(NOTPResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/notp/whatsapp/signup-in");
      assertThat(auth.getValue().get()).isEqualTo(bearer(null));
      NOTPLoginOptions expectedLoginOptions = NOTPLoginOptions.builder()
          .customClaims(signUpOptions.getCustomClaims())
          .templateOptions(signUpOptions.getTemplateOptions())
          .build();
      assertEquals(new NOTPSignInRequest(PROVIDER, MOCK_PHONE, expectedLoginOptions, "provider-id", TEMPLATES,
          "sso-app"), body.getValue());
      assertResponse(response);
    }
  }

  @Test
  void signUpOrInRejectsInvalidArguments() {
    assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.signUpOrIn(null, MOCK_PHONE, null, null)).getMessage());
  }

  @Test
  void updateUserSendsRequest() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_NOTP_RESPONSE).when(apiProxy).post(any(), any(), any());
    ArgumentCaptor<Supplier<String>> auth = authCaptor();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(auth.capture(), any())).thenReturn(apiProxy);
      NOTPUpdateOptions updateOptions = new NOTPUpdateOptions(true, true, Collections.singletonMap("t", "o"),
          "provider-id", TEMPLATES, "sso-app", "en", true);
      NOTPResponse response = notpService.updateUser(PROVIDER, "login", MOCK_PHONE, MOCK_REFRESH_TOKEN,
          updateOptions);

      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<NOTPUpdateUserRequest> body = ArgumentCaptor.forClass(NOTPUpdateUserRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(NOTPResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/notp/whatsapp/update");
      assertThat(auth.getValue().get()).isEqualTo(bearer(MOCK_REFRESH_TOKEN));
      assertEquals(new NOTPUpdateUserRequest(PROVIDER, "login", MOCK_PHONE, true, true, "provider-id", TEMPLATES,
          Collections.singletonMap("t", "o"), "sso-app", "en", true), body.getValue());
      assertResponse(response);
    }
  }

  @Test
  void updateUserRejectsInvalidArguments() {
    assertEquals("The Provider argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.updateUser("", "login", MOCK_PHONE, MOCK_REFRESH_TOKEN, null)).getMessage());
    assertEquals("The Login ID argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.updateUser(PROVIDER, "", MOCK_PHONE, MOCK_REFRESH_TOKEN, null)).getMessage());
    assertEquals("The Refresh Token argument is invalid", assertThrows(ServerCommonException.class,
        () -> notpService.updateUser(PROVIDER, "login", MOCK_PHONE, "", null)).getMessage());
  }

  @Test
  void getSessionSendsRequest() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    ArgumentCaptor<Supplier<String>> auth = authCaptor();
    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(auth.capture(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = notpService.getSession("pending-ref");
      }
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy, times(1)).post(uri.capture(), eq(new EnchantedLinkSessionBody("pending-ref")),
          eq(JWTResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/notp/pending-session");
      assertThat(auth.getValue().get()).isEqualTo(bearer(null));
    }
    assertThat(authenticationInfo.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(authenticationInfo.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertThat(authenticationInfo.getUser()).isEqualTo(MOCK_JWT_RESPONSE.getUser());
    assertThat(authenticationInfo.getFirstSeen()).isEqualTo(MOCK_JWT_RESPONSE.getFirstSeen());
  }
}
