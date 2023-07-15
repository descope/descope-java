package com.descope.sdk.auth.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.descope.enums.DeliveryMethod;
import com.descope.model.jwt.Token;
import com.descope.model.user.request.UserRequest;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthenticationServiceImplTest {

  private AuthenticationService authenticationService;
  private UserService userService;
  private OTPService otpService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getAuthParams();
    var client = TestUtils.getClient();
    var authService = AuthenticationServiceBuilder.buildServices(client, authParams);
    this.authenticationService = authService.getAuthService();
    this.otpService = authService.getOtpService();
    var mgmtParams = TestUtils.getManagementParams();
    this.userService = ManagementServiceBuilder.buildServices(client, mgmtParams).getUserService();
  }

  @Test
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId, UserRequest.builder().email(loginId).verifiedEmail(true).build());
    var code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    var authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    Token token = authenticationService.validateSessionWithToken(authInfo.getToken().getJwt());
    assertThat(token.getJwt()).isNotBlank();
    // token = authenticationService.refreshSessionWithToken(authInfo.getRefreshToken().getJwt());
    // assertThat(token.getJwt()).isNotBlank();
    // authenticationService.logout(authInfo.getRefreshToken().getJwt());
    userService.delete(loginId);
  }
}
