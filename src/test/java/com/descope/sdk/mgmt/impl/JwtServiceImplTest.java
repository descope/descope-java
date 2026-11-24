package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.request.ClientAssertionRequest;
import com.descope.model.jwt.response.ClientAssertionResponse;
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
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
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

  @Test
  void testGenerateClientAssertionJwtWithUnsupportedAlgorithm() {
    assertThatThrownBy(() -> jwtService.generateClientAssertionJwt(
            "issuer",
            "subject",
            java.util.Arrays.asList("https://example.com"),
            3600,
            false,
            "HS256"
        ))
        .isInstanceOf(ServerCommonException.class)
        .hasMessage("The algorithm argument is invalid");
  }

  @Test
  void testGenerateClientAssertionJwtWithValidAlgorithm() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new ClientAssertionResponse("mock.jwt.token"))
        .when(apiProxy).post(any(), any(), eq(ClientAssertionResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      JwtService service = new JwtServiceImpl(
          Client.builder().projectId("test-project").managementKey("test-key").build());
      service.generateClientAssertionJwt("issuer", "subject",
          java.util.Arrays.asList("https://example.com"), 3600, false, "RS256");
      ArgumentCaptor<ClientAssertionRequest> body = ArgumentCaptor.forClass(ClientAssertionRequest.class);
      verify(apiProxy).post(any(), body.capture(), eq(ClientAssertionResponse.class));
      assertEquals("RS256", body.getValue().getAlgorithm());
    }
  }
}
