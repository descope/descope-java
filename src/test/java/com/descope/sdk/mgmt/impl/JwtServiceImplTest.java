package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
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

public class JwtServiceImplTest {

  private final Map<String, Object> mockCustomClaims = Map.of("test", "claim");
  private JwtService jwtService;
  private UserService userService;
  private OTPService otpService;

  @BeforeEach
  void setUp() {
    ManagementParams authParams = TestUtils.getManagementParams();
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
    this.jwtService = mgmtServices.getJwtService();
    this.userService = mgmtServices.getUserService();
    this.otpService = AuthenticationServiceBuilder.buildServices(client, TestUtils.getAuthParams()).getOtpService();
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
    assertEquals(newJwt.getClaims().get("test"), "claim");
    userService.delete(loginId);
  }
}
