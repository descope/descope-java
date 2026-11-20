package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_TOKEN;
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
import static org.mockito.Mockito.mockStatic;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AccessKeyLoginOptions;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.auth.MyTenantsResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.Token;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.UserFamily;
import com.descope.model.user.response.UserResponse;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class AuthenticationServiceImplTest {

  private static final String MOCK_PROJECT_ID = "P123456789012345678901234567";
  private static final ObjectMapper JSON = new ObjectMapper();

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

  @Test
  void testMe() throws IOException {
    String body = "{\"userId\":\"u1\",\"email\":\"e@x.com\",\"OAuth\":{\"google\":true},\"webauthn\":true,"
        + "\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
        + "\"externalIds\":[\"x1\"],\"consentExpiration\":11,\"recoveryEmail\":\"r@x.com\","
        + "\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,"
        + "\"modifiedTime\":\"12\",\"roleIds\":[\"ri1\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr1\"],\"permissions\":[\"fp1\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"t1\",\"samlTenantId\":\"t2\","
        + "\"oidcTenantId\":\"t3\",\"consentTenantId\":\"t4\",\"lastLoginTime\":13,"
        + "\"userTenants\":[{\"tenantId\":\"t1\",\"tenantName\":\"T1\",\"roleNames\":[\"r1\"],"
        + "\"permissions\":[\"tp1\"],\"roleIds\":[\"tri1\"]}],\"unknownField\":1}";
    AtomicReference<String[]> req = new AtomicReference<>();
    HttpServer server = serverReturning("/v1/auth/me", body, req);
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      final UserResponse u = authService(server).me("refresh-jwt");
      assertEquals("GET", req.get()[0]);
      assertEquals("/v1/auth/me", req.get()[1]);
      assertEquals("Bearer " + MOCK_PROJECT_ID + ":refresh-jwt", req.get()[2]);
      assertEquals("u1", u.getUserId());
      assertEquals(Collections.singletonMap("google", true), u.getOAuth());
      assertTrue(u.getWebauthn());
      assertTrue(u.getPassword());
      assertTrue(u.getEditable());
      assertTrue(u.getScim());
      assertTrue(u.getPush());
      assertEquals(Arrays.asList("p1"), u.getPermissions());
      assertTrue(u.getOidc());
      assertEquals(Arrays.asList("x1"), u.getExternalIds());
      assertEquals(11L, u.getConsentExpiration());
      assertEquals("r@x.com", u.getRecoveryEmail());
      assertTrue(u.getVerifiedRecoveryEmail());
      assertEquals("+15555555555", u.getRecoveryPhone());
      assertTrue(u.getVerifiedRecoveryPhone());
      assertEquals(12L, u.getModifiedTime());
      assertEquals(Arrays.asList("ri1"), u.getRoleIds());
      assertTrue(u.getRecoveryCodes());
      assertTrue(u.getDependent());
      UserFamily family = u.getUserFamilies().get(0);
      assertEquals("f1", family.getFamilyId());
      assertEquals(Arrays.asList("fr1"), family.getRoleNames());
      assertEquals(Arrays.asList("fp1"), family.getPermissions());
      assertEquals(mapOf("a", "b"), family.getFamilyScopedAttributes());
      assertEquals("t1", u.getScimTenantId());
      assertEquals("t2", u.getSamlTenantId());
      assertEquals("t3", u.getOidcTenantId());
      assertEquals("t4", u.getConsentTenantId());
      assertEquals(13L, u.getLastLoginTime());
      assertEquals("T1", u.getUserTenants().get(0).getTenantName());
      assertEquals(Arrays.asList("tp1"), u.getUserTenants().get(0).getPermissions());
      assertEquals(Arrays.asList("tri1"), u.getUserTenants().get(0).getRoleIds());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testMyTenants() throws IOException {
    String body = "{\"tenants\":[{\"id\":\"t1\",\"name\":\"T1\",\"customAttributes\":{\"a\":\"b\"}}]}";
    AtomicReference<String[]> req = new AtomicReference<>();
    HttpServer server = serverReturning("/v1/auth/me/tenants", body, req);
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      final MyTenantsResponse res = authService(server).myTenants("refresh-jwt", false, Arrays.asList("t1"));
      assertEquals("POST", req.get()[0]);
      assertEquals("/v1/auth/me/tenants", req.get()[1]);
      assertEquals("Bearer " + MOCK_PROJECT_ID + ":refresh-jwt", req.get()[2]);
      assertEquals(JSON.readTree("{\"dct\":false,\"ids\":[\"t1\"]}"), JSON.readTree(req.get()[3]));
      assertEquals(1, res.getTenants().size());
      assertEquals("t1", res.getTenants().get(0).getId());
      assertEquals("T1", res.getTenants().get(0).getName());
      assertEquals(mapOf("a", "b"), res.getTenants().get(0).getCustomAttributes());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testMyTenantsDct() throws IOException {
    AtomicReference<String[]> req = new AtomicReference<>();
    HttpServer server = serverReturning("/v1/auth/me/tenants", "{\"tenants\":[{\"id\":\"t1\"}]}", req);
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      MyTenantsResponse res = authService(server).myTenants("refresh-jwt", true, null);
      assertEquals(JSON.readTree("{\"dct\":true}"), JSON.readTree(req.get()[3]));
      assertEquals("t1", res.getTenants().get(0).getId());
      assertNull(res.getTenants().get(0).getName());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testMyTenantsMissingRefreshToken() throws IOException {
    AtomicReference<String[]> req = new AtomicReference<>();
    HttpServer server = serverReturning("/v1/auth/me/tenants", "{}", req);
    try {
      AuthenticationService as = authService(server);
      ServerCommonException ex =
          assertThrows(ServerCommonException.class, () -> as.myTenants("", true, null));
      assertEquals("refresh token", ex.getMessage());
      assertNull(req.get());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testMyTenantsDctAndTenantIds() throws IOException {
    AtomicReference<String[]> req = new AtomicReference<>();
    HttpServer server = serverReturning("/v1/auth/me/tenants", "{}", req);
    try {
      AuthenticationService as = authService(server);
      ServerCommonException ex = assertThrows(ServerCommonException.class,
          () -> as.myTenants("refresh-jwt", true, Arrays.asList("t1")));
      assertThat(ex.getMessage()).contains("tenantIds");
      assertNull(req.get());
    } finally {
      server.stop(0);
    }
  }

  private static AuthenticationService authService(HttpServer server) {
    return AuthenticationServiceBuilder.buildServices(Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId(MOCK_PROJECT_ID)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build()).getAuthService();
  }

  /** Serves the body at path and records the method, path, Authorization header and request body. */
  private static HttpServer serverReturning(String path, String body, AtomicReference<String[]> req)
      throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(path, exchange -> {
      ByteArrayOutputStream reqBody = new ByteArrayOutputStream();
      try (InputStream is = exchange.getRequestBody()) {
        byte[] buf = new byte[1024];
        for (int n = is.read(buf); n > 0; n = is.read(buf)) {
          reqBody.write(buf, 0, n);
        }
      }
      req.set(new String[] {exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
          exchange.getRequestHeaders().getFirst("Authorization"), reqBody.toString("UTF-8")});
      byte[] payload = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    return server;
  }
}
