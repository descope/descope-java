package com.descope.sdk.mgmt.impl;

import static com.descope.sdk.auth.impl.TestAuthUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.auth.impl.TestAuthUtils.MOCK_SIGNING_KEY;
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
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.JwtService;
import java.security.Key;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class JwtServiceImplTest {

  private final Map<String, Object> mockCustomClaims = Map.of("test", "claim");
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    var authParams = TestMgmtUtils.getManagementParams();
    var client = TestUtils.getClient();
    this.jwtService = ManagementServiceBuilder.buildServices(client, authParams).getJwtService();
  }

  @Test
  void testUpdateJWTWithCustomClaims() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKey[] {MOCK_SIGNING_KEY})
      .when(apiProxy).get(any(), eq(SigningKey[].class));

    var provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));

    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
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
