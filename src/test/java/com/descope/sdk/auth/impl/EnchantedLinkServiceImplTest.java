package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_DOMAIN;
import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_MASKED_EMAIL;
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

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.SigningKeysResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
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
