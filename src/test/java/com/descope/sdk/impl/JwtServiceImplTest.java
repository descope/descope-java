package com.descope.sdk.impl;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.JwtService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class JwtServiceImplTest {

  public static final String MOCK_EMAIL = "username@domain.com";

  public static final UserResponse MOCK_USER_RESPONSE =
      new UserResponse(
          "someUserId",
          List.of(MOCK_EMAIL),
          "someEmail@descope.com",
          true,
          "+1-555-555-5555",
          false,
          "someName",
          Collections.emptyList(),
          Collections.emptyList(),
          "enabled",
          "",
          false,
          0L,
          Collections.emptyMap(),
          false,
          false,
          Collections.emptyMap());
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
  @SuppressWarnings("checkstyle:LineLength")
  public static final SigningKey MOCK_SIGNING_KEY =
      SigningKey.builder()
          .e("AQAB")
          .kid(MOCK_PROJECT_ID)
          .kty("RSA")
          .n(
              "w8b3KRCep717H4MdVbwYHeb0vr891Ok1BL_TmC0XFUIKjRoKsWOcUZ9BFd6wR_5mnJuE7M8ZjVQRCbRlVgnh6AsEL3JA9Z6c1TpURTIXZxSE6NbeB7IMLMn5HWW7cjbnG4WO7E1PUCT6zCcBVz6EhA925GIJpyUxuY7oqJG-6NoOltI0Ocm6M2_7OIFMzFdw42RslqyX6l-SDdo_ZLq-XtcsCVRyj2YvmXUNF4Vq1x5syPOEQ-SezkvpBcb5Szi0ULpW5CvX2ieHAeHeQ2x8gkv6Dn2AW_dllQ--ZO-QH2QkxEXlMVqilwAdbA0k6BBtSkMC-7kD3A86bGGplpzz5Q")
          .build();
  private final Map<String, Object> mockCustomClaims = Map.of("test", "claim");
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.jwtService = ManagementServiceBuilder.buildServices(client, authParams).getJwtService();
  }

  @Test
  void testUpdateJWTWithCustomClaims() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKey[] {MOCK_SIGNING_KEY}).when(apiProxy).get(any(), eq(SigningKey[].class));

    var provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));

    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = jwtService.updateJWTWithCustomClaims("someJwt", mockCustomClaims);
      Assertions.assertThat(response).isEqualTo("someSessionJwt");
    }
  }

  @Test
  void testupdateJWTWithCustomClaimsForEmptyJwt() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> jwtService.updateJWTWithCustomClaims("", mockCustomClaims));
    assertNotNull(thrown);
    assertEquals("The JWT argument is invalid", thrown.getMessage());
  }
}
