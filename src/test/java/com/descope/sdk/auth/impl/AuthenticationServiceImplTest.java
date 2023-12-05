package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;

public class AuthenticationServiceImplTest {

  private AuthenticationService authenticationService;
  private UserService userService;
  private OTPService otpService;
  private RolesService roleService;
  private TenantService tenantService;

  @BeforeEach
  void setUp() {
    AuthParams authParams = TestUtils.getAuthParams();
    Client client = TestUtils.getClient();
    AuthenticationServices authService = AuthenticationServiceBuilder.buildServices(client, authParams);
    this.authenticationService = authService.getAuthService();
    this.otpService = authService.getOtpService();
    ManagementParams mgmtParams = TestUtils.getManagementParams();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client, mgmtParams);
    this.userService = mgmtServices.getUserService();
    this.roleService = mgmtServices.getRolesService();
    this.tenantService = mgmtServices.getTenantService();
  }

  @Test
  void testPermissionsAndRoles() {
    assertTrue(authenticationService.validatePermissions(MOCK_TOKEN, "someTenant", List.of("tp1", "tp2")));
    assertFalse(authenticationService.validatePermissions(MOCK_TOKEN, "someTenant", List.of("tp2", "tp3")));
    assertTrue(authenticationService.validatePermissions(MOCK_TOKEN, List.of("p1", "p2")));
    assertFalse(authenticationService.validatePermissions(MOCK_TOKEN, List.of("p2", "p3")));
    assertTrue(authenticationService.validateRoles(MOCK_TOKEN, "someTenant", List.of("tr1", "tr2")));
    assertFalse(authenticationService.validateRoles(MOCK_TOKEN, "someTenant", List.of("tr2", "tr3")));
    assertTrue(authenticationService.validateRoles(MOCK_TOKEN, List.of("r1", "r2")));
    assertFalse(authenticationService.validateRoles(MOCK_TOKEN, List.of("r2", "r3")));
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalPermissions() {
    String roleName = TestUtils.getRandomName("r-").substring(0, 20);
    roleService.create(roleName, "ttt", null);
    String tenantName = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(tenantName, List.of(tenantName + ".com"));
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId,
        UserRequest.builder()
          .email(loginId)
          .verifiedEmail(true)
          .roleNames(List.of(roleName))
          .build());
    userService.addTenant(loginId, tenantId);
    userService.addTenantRoles(loginId, tenantId, List.of(roleName));
    OTPTestUserResponse code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    assertTrue(authenticationService.validateRoles(authInfo.getToken(), List.of(roleName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), List.of(roleName + "x")));
    assertTrue(authenticationService.validateRoles(authInfo.getToken(), tenantId, List.of(roleName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), tenantId, List.of(roleName + "x")));
    userService.delete(loginId);
    tenantService.delete(tenantId);
    roleService.delete(roleName);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId, UserRequest.builder().email(loginId).verifiedEmail(true).build());
    OTPTestUserResponse code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    Token token = authenticationService.validateSessionWithToken(authInfo.getToken().getJwt());
    assertThat(token.getJwt()).isNotBlank();
    token = authenticationService.refreshSessionWithToken(authInfo.getRefreshToken().getJwt());
    assertThat(token.getJwt()).isNotBlank();
    authenticationService.logout(authInfo.getRefreshToken().getJwt());
    userService.delete(loginId);
  }
}
