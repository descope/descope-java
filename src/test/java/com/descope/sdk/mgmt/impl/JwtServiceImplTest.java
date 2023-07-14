package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.user.request.UserRequest;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import com.descope.sdk.mgmt.JwtService;
import com.descope.sdk.mgmt.UserService;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtServiceImplTest {

  private final Map<String, Object> mockCustomClaims = Map.of("test", "claim");
  private JwtService jwtService;
  private UserService userService;
  private OTPService otpService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getManagementParams();
    var client = TestUtils.getClient();
    var mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
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

  @Test
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId, UserRequest.builder().email(loginId).verifiedEmail(true).build());
    var code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    var authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    Assertions.assertThat(authInfo.getToken().getJwt()).isNotBlank();
    var newJwt = jwtService.updateJWTWithCustomClaims(authInfo.getToken().getJwt(), mockCustomClaims);
    assertNotNull(newJwt.getClaims());
    assertEquals(newJwt.getClaims().get("test"), "claim");
    userService.deleteAllTestUsers();
  }
}
