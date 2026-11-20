package com.descope.sdk.mgmt.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.mgmt.JwtService;
import com.descope.sdk.mgmt.UserService;
import com.descope.utils.JwtUtils;
import java.util.Collections;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class JwtServiceImplTest {

  private final Map<String, Object> mockCustomClaims = mapOf("test", "claim");
  private JwtService jwtService;
  private UserService userService;
  private OTPService otpService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.jwtService = mgmtServices.getJwtService();
    this.userService = mgmtServices.getUserService();
    this.otpService = AuthenticationServiceBuilder.buildServices(client).getOtpService();
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

  @Test
  void testAnonymousCopiesResponseFields() {
    JWTResponse jwtResponse = new JWTResponse();
    jwtResponse.setSessionJwt(MOCK_JWT_RESPONSE.getSessionJwt());
    jwtResponse.setRefreshJwt(MOCK_JWT_RESPONSE.getRefreshJwt());
    jwtResponse.setUser(MOCK_JWT_RESPONSE.getUser());
    jwtResponse.setSessionExpiration(1234);
    jwtResponse.setSsoAuthenticationOnly(true);
    jwtResponse.setUserCreationDeferred(true);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), eq(JWTResponse.class));
    doReturn(new SigningKeysResponse(Collections.emptyList()))
        .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    AuthenticationInfo authInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      authInfo = jwtService.anonymous(AnonymousUserRequest.builder().build());
    }
    assertEquals(MOCK_TOKEN, authInfo.getToken());
    assertEquals(1234, authInfo.getSessionExpiration());
    assertEquals(true, authInfo.getSsoAuthenticationOnly());
    assertEquals(true, authInfo.getUserCreationDeferred());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId, UserRequest.builder().email(loginId).verifiedEmail(true).build());
    OTPTestUserResponse code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    Assertions.assertThat(authInfo.getToken().getJwt()).isNotBlank();
    Token newJwt = jwtService.updateJWTWithCustomClaims(authInfo.getToken().getJwt(), mockCustomClaims);
    assertNotNull(newJwt.getClaims());
    assertEquals("claim", newJwt.getClaims().get("test"));
    userService.delete(loginId);

    AuthenticationInfo anonyUser = jwtService.anonymous(
            AnonymousUserRequest.builder().customClaims(mockCustomClaims).build());
    assertNotNull(anonyUser.getToken());
    assertNull(anonyUser.getUser());
  }
}
