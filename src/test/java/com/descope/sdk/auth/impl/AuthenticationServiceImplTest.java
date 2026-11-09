package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AccessKeyLoginOptions;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.UserFamily;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class AuthenticationServiceImplTest {

  private AuthenticationService authenticationService;
  private UserService userService;
  private OTPService otpService;
  private RolesService roleService;
  private TenantService tenantService;
  private AccessKeyService accessKeyService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    AuthenticationServices authService = AuthenticationServiceBuilder.buildServices(client);
    this.authenticationService = authService.getAuthService();
    this.otpService = authService.getOtpService();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.userService = mgmtServices.getUserService();
    this.roleService = mgmtServices.getRolesService();
    this.tenantService = mgmtServices.getTenantService();
    this.accessKeyService = mgmtServices.getAccessKeyService();
  }

  @Test
  void testPermissionsAndRoles() {
    assertTrue(authenticationService.validatePermissions(MOCK_TOKEN, "someTenant", Arrays.asList("tp1", "tp2")));
    assertFalse(authenticationService.validatePermissions(MOCK_TOKEN, "someTenant", Arrays.asList("tp2", "tp3")));
    assertTrue(authenticationService.validatePermissions(MOCK_TOKEN, Arrays.asList("p1", "p2")));
    assertFalse(authenticationService.validatePermissions(MOCK_TOKEN, Arrays.asList("p2", "p3")));
    assertTrue(authenticationService.validateRoles(MOCK_TOKEN, "someTenant", Arrays.asList("tr1", "tr2")));
    assertFalse(authenticationService.validateRoles(MOCK_TOKEN, "someTenant", Arrays.asList("tr2", "tr3")));
    assertTrue(authenticationService.validateRoles(MOCK_TOKEN, Arrays.asList("r1", "r2")));
    assertFalse(authenticationService.validateRoles(MOCK_TOKEN, Arrays.asList("r2", "r3")));
  }

  @Test
  void testGetMatchedPermissionsAndRoles() {
    assertEquals(Arrays.asList("tp1", "tp2"),
        authenticationService.getMatchedPermissions(MOCK_TOKEN, "someTenant", Arrays.asList("tp1", "tp2")));
    assertEquals(Arrays.asList(),
        authenticationService.getMatchedPermissions(MOCK_TOKEN, "someTenant", null));
    assertEquals(Arrays.asList("tp1", "tp2"),
        authenticationService.getMatchedPermissions(MOCK_TOKEN, "someTenant", Arrays.asList("tp1", "tp2", "tp3")));
    assertEquals(Arrays.asList("p1", "p2"),
        authenticationService.getMatchedPermissions(MOCK_TOKEN, Arrays.asList("p1", "p2")));
    assertEquals(Arrays.asList(),
        authenticationService.getMatchedPermissions(MOCK_TOKEN, null));
    assertEquals(Arrays.asList("p1", "p2"),
        authenticationService.getMatchedPermissions(MOCK_TOKEN, Arrays.asList("p1", "p2", "p3")));
  }

  @Test
  void testMeForEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> authenticationService.me(""));
    assertNotNull(thrown);
    assertEquals("refresh token", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testMe() throws Exception {
    String body = "{\"userId\":\"u1\",\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,"
        + "\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
        + "\"consentExpiration\":1700000001,\"recoveryEmail\":\"r@descope.com\",\"verifiedRecoveryEmail\":true,"
        + "\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":1700000002,"
        + "\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"t1\",\"samlTenantId\":\"t2\","
        + "\"oidcTenantId\":\"t3\",\"consentTenantId\":\"t4\",\"lastLoginTime\":1700000003,\"unknown\":1}";
    UserResponse parsed = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(body, UserResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parsed).when(apiProxy).get(any(), eq(UserResponse.class));
    AtomicReference<String> authHeader = new AtomicReference<>();
    UserResponse user;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      user = authenticationService.me(MOCK_REFRESH_TOKEN);
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), eq(UserResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/me");
    assertThat(authHeader.get()).startsWith("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertEquals("u1", user.getUserId());
    assertEquals(Arrays.asList("e1"), user.getExternalIds());
    assertTrue(user.getWebauthn());
    assertTrue(user.getPassword());
    assertTrue(user.getEditable());
    assertTrue(user.getScim());
    assertTrue(user.getPush());
    assertEquals(Arrays.asList("p1"), user.getPermissions());
    assertTrue(user.getOidc());
    assertEquals(Long.valueOf(1700000001L), user.getConsentExpiration());
    assertEquals("r@descope.com", user.getRecoveryEmail());
    assertTrue(user.getVerifiedRecoveryEmail());
    assertEquals("+15555555555", user.getRecoveryPhone());
    assertTrue(user.getVerifiedRecoveryPhone());
    assertEquals(Long.valueOf(1700000002L), user.getModifiedTime());
    assertEquals(Arrays.asList("r1"), user.getRoleIds());
    assertTrue(user.getRecoveryCodes());
    assertTrue(user.getDependent());
    UserFamily family = user.getUserFamilies().get(0);
    assertEquals("f1", family.getFamilyId());
    assertEquals(Arrays.asList("fr"), family.getRoleNames());
    assertEquals(Arrays.asList("fp"), family.getPermissions());
    assertEquals(Collections.singletonMap("a", "b"), family.getFamilyScopedAttributes());
    assertEquals("t1", user.getScimTenantId());
    assertEquals("t2", user.getSamlTenantId());
    assertEquals("t3", user.getOidcTenantId());
    assertEquals("t4", user.getConsentTenantId());
    assertEquals(Long.valueOf(1700000003L), user.getLastLoginTime());
  }

  @Test
  void testUserResponseDeserializesNullNewFields() throws Exception {
    UserResponse user = new ObjectMapper().readValue(
        "{\"userId\":\"u1\",\"userFamilies\":null,\"permissions\":null}", UserResponse.class);
    assertNull(user.getUserFamilies());
    assertNull(user.getPermissions());
    assertNull(user.getScim());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalPermissions() {
    String roleName = TestUtils.getRandomName("r-").substring(0, 20);
    roleService.create(roleName, "ttt", null);
    String tenantName = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(tenantName, Arrays.asList(tenantName + ".com"));
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId,
        UserRequest.builder()
          .email(loginId)
          .verifiedEmail(true)
          .roleNames(Arrays.asList(roleName))
          .build());
    userService.addTenant(loginId, tenantId);
    userService.addTenantRoles(loginId, tenantId, Arrays.asList(roleName));
    OTPTestUserResponse code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    assertTrue(authenticationService.validateRoles(authInfo.getToken(), Arrays.asList(roleName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), Arrays.asList(roleName + "x")));
    assertTrue(authenticationService.validateRoles(authInfo.getToken(), tenantId, Arrays.asList(roleName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), tenantId, Arrays.asList(roleName + "x")));
    userService.delete(loginId);
    tenantService.delete(tenantId);
    roleService.delete(roleName);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() throws Exception {
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
    authInfo = authenticationService.refreshSessionWithTokenAuthenticationInfo(authInfo.getRefreshToken().getJwt());
    assertThat(authInfo.getRefreshToken()).isNotNull();
    assertThat(authInfo.getRefreshToken().getJwt()).isNotBlank();
    UserResponse u = authenticationService.me(authInfo.getRefreshToken().getJwt());
    assertThat(u.getEmail()).isEqualTo(loginId);
    authenticationService.logout(authInfo.getRefreshToken().getJwt());
    userService.delete(loginId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalExchangeToken() throws Exception {
    String name = TestUtils.getRandomName("ak-");
    AccessKeyResponse resp = accessKeyService.create(name, 0, null, null, null, mapOf("K1", "V1"));
    Token token = authenticationService.exchangeAccessKey(resp.getCleartext(),
        new AccessKeyLoginOptions(mapOf("kuku", "kiki")));

    // temporary
    @SuppressWarnings("unchecked")
    // Validate the nsec claims (passed through the exchange method)
    Map<String, Object> nsecClaims = Map.class.cast(token.getClaims().get("nsec"));
    assertEquals("kiki", nsecClaims.get("kuku"));
    // Validate the secured claims (passed through the Create method)
    assertEquals("V1", token.getClaims().get("K1"));
    accessKeyService.delete(resp.getKey().getId());
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalSelectTenant() {
    // setup
    String roleAName = TestUtils.getRandomName("r-").substring(0, 20);
    String roleBName = TestUtils.getRandomName("r-").substring(0, 20);
    roleService.create(roleAName, "ttt", null);
    roleService.create(roleBName, "ttt", null);
    String tenantAName = TestUtils.getRandomName("t-");
    String tenantAId = tenantService.create(tenantAName, Arrays.asList(tenantAName + ".com"));
    String tenantBName = TestUtils.getRandomName("t-");
    String tenantBId = tenantService.create(tenantBName, Arrays.asList(tenantBName + ".com"));
    String loginId = TestUtils.getRandomName("u-") + "@descope.com";
    userService.createTestUser(loginId,
            UserRequest.builder()
                    .email(loginId)
                    .verifiedEmail(true)
                    .build());
    userService.addTenant(loginId, tenantAId);
    userService.setTenantRoles(loginId, tenantAId, Arrays.asList(roleAName));
    userService.addTenant(loginId, tenantBId);
    userService.setTenantRoles(loginId, tenantBId, Arrays.asList(roleBName));
    // login
    OTPTestUserResponse code = userService.generateOtpForTestUser(loginId, DeliveryMethod.EMAIL);
    AuthenticationInfo authInfo = otpService.verifyCode(DeliveryMethod.EMAIL, loginId, code.getCode());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    // go time
    assertFalse(authInfo.getToken().getClaims().containsKey("dct"));
    assertTrue(authenticationService.validateRoles(authInfo.getToken(), tenantAId, Arrays.asList(roleAName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), tenantAId, Arrays.asList(roleBName)));
    // root shouldn't have any of the tenants' roles
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), Arrays.asList(roleAName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), Arrays.asList(roleBName)));

    authInfo = authenticationService.selectTenant(tenantAId, authInfo.getRefreshToken().getJwt());
    assertNotNull(authInfo.getToken());
    assertThat(authInfo.getToken().getJwt()).isNotBlank();
    assertThat(authInfo.getRefreshToken().getJwt()).isNotBlank();
    assertEquals(tenantAId, authInfo.getToken().getClaims().get("dct"));

    // no tenant roles without a tenant
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), Arrays.asList(roleAName)));
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), Arrays.asList(roleBName)));
    // tenant a gives role a
    assertTrue(authenticationService.validateRoles(authInfo.getToken(), tenantAId, Arrays.asList(roleAName)));
    // tenant a doesn't give tenant b's roles
    assertFalse(authenticationService.validateRoles(authInfo.getToken(), tenantAId, Arrays.asList(roleBName)));

    userService.delete(loginId);
    tenantService.delete(tenantAId);
    roleService.delete(roleAName);
    tenantService.delete(tenantBId);
    roleService.delete(roleBName);
  }

}
