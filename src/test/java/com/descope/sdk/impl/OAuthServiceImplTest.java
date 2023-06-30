package com.descope.sdk.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.OAuthService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.utils.JwtUtils;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class OAuthServiceImplTest {

  public static final String MOCK_PROJECT_ID = "someProjectId";
  public static final String MOCK_URL = "https://www.domain.com";
  public static final String MOCK_EMAIL = "username@domain.com";

  public static final UserResponse MOCK_USER_RESPONSE =
      new UserResponse(
          "someUserId",
          List.of(MOCK_EMAIL),
          true,
          false,
          Collections.emptyList(),
          Collections.emptyList(),
          "enabled",
          "",
          false);
  public static final JWTResponse MOCK_JWT_RESPONSE =
      new JWTResponse(
          "someSessionJwt",
          "someRefreshJwt",
          "",
          "/",
          1234567,
          1234567890,
          MOCK_USER_RESPONSE,
          true);
  public static final Token MOCK_TOKEN =
      Token.builder()
          .id("1")
          .projectId(MOCK_PROJECT_ID)
          .jwt("someJwtToken")
          .claims(Map.of("someClaim", 1))
          .build();
  @SuppressWarnings("checkstyle:LineLength")
  public static final SigningKey MOCK_SIGNING_KEY =
      SigningKey.builder()
          .e("AQAB")
          .kid(MOCK_PROJECT_ID)
          .kty("RSA")
          .n(
              "w8b3KRCep717H4MdVbwYHeb0vr891Ok1BL_TmC0XFUIKjRoKsWOcUZ9BFd6wR_5mnJuE7M8ZjVQRCbRlVgnh6AsEL3JA9Z6c1TpURTIXZxSE6NbeB7IMLMn5HWW7cjbnG4WO7E1PUCT6zCcBVz6EhA925GIJpyUxuY7oqJG-6NoOltI0Ocm6M2_7OIFMzFdw42RslqyX6l-SDdo_ZLq-XtcsCVRyj2YvmXUNF4Vq1x5syPOEQ-SezkvpBcb5Szi0ULpW5CvX2ieHAeHeQ2x8gkv6Dn2AW_dllQ--ZO-QH2QkxEXlMVqilwAdbA0k6BBtSkMC-7kD3A86bGGplpzz5Q")
          .build();

  private OAuthService oauthService;

  @BeforeEach
  void setUp() {
    var authParams = AuthParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.oauthService =
        AuthenticationServiceBuilder.buildServices(client, authParams).getOauthService();
  }

  @Test
  void testStart() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_URL).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      String start = oauthService.start("provider", "returnurl", new LoginOptions());
      Assertions.assertThat(start).isNotBlank().contains(MOCK_URL);
    }
  }

  @Test
  void testExchangeToken() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKey[] {MOCK_SIGNING_KEY}).when(apiProxy).get(any(), eq(SigningKey[].class));

    var provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);

      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = oauthService.exchangeToken("somecode");
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();

    Token sessionToken = authenticationInfo.getToken();
    Assertions.assertThat(sessionToken).isNotNull();
    Assertions.assertThat(sessionToken.getJwt()).isNotBlank();
    Assertions.assertThat(sessionToken.getClaims()).isNotEmpty();
    Assertions.assertThat(sessionToken.getProjectId()).isEqualTo(MOCK_PROJECT_ID);

    Token refreshToken = authenticationInfo.getRefreshToken();
    Assertions.assertThat(refreshToken).isNotNull();
    Assertions.assertThat(refreshToken.getJwt()).isNotBlank();
    Assertions.assertThat(refreshToken.getClaims()).isNotEmpty();
    Assertions.assertThat(refreshToken.getProjectId()).isEqualTo(MOCK_PROJECT_ID);

    UserResponse user = authenticationInfo.getUser();
    Assertions.assertThat(user).isNotNull();
    Assertions.assertThat(user.getUserId()).isNotBlank();
    Assertions.assertThat(user.getLoginIds()).isNotEmpty();
  }
}
