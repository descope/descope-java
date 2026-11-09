package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.REFRESH_TOKEN_LINK;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.RateLimitExceededException;
import com.descope.model.auth.AccessKeyLoginOptions;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
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

  private static final String REFRESH_JWT = "some-refresh-jwt";
  private static final String REFRESH_RESPONSE_JSON = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\","
      + "\"sessionExpiration\":11,\"externalToken\":\"ext\",\"claims\":{\"c\":\"v\"},"
      + "\"tenantSSOID\":\"t1\",\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":12,"
      + "\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\","
      + "\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\",\"ssoAuthenticationOnly\":true,"
      + "\"userCreationDeferred\":true,\"user\":{\"userId\":\"u1\",\"externalIds\":[\"e1\"],"
      + "\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,"
      + "\"permissions\":[\"p1\"],\"OIDC\":true,\"consentExpiration\":13,\"recoveryEmail\":\"re\","
      + "\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"rp\",\"verifiedRecoveryPhone\":true,"
      + "\"modifiedTime\":14,\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
      + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
      + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sat\","
      + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":15}}";

  @Test
  void testRefreshSessionWithExternalToken() throws Exception {
    JWTResponse response = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(REFRESH_RESPONSE_JSON, JWTResponse.class);
    AtomicReference<Supplier<String>> header = new AtomicReference<>();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(inv.getArgument(0));
        return apiProxy;
      });
      jwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      info = refreshAuthService().refreshSessionWithTokenAuthenticationInfo(REFRESH_JWT, "ext");
    }

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(mapOf("externalToken", "ext")), eq(JWTResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo(REFRESH_TOKEN_LINK);
    assertThat(header.get().get()).isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_JWT);

    assertThat(info.getToken()).isEqualTo(MOCK_TOKEN);
    assertThat(info.getRefreshToken()).isEqualTo(MOCK_TOKEN);
    assertEquals(11, info.getSessionExpiration());
    assertEquals("ext", info.getExternalToken());
    assertEquals(mapOf("c", "v"), info.getClaims());
    assertEquals("t1", info.getTenantSSOID());
    assertEquals("tdj", info.getTrustedDeviceJwt());
    assertEquals(12, info.getNextRefreshSeconds());
    assertEquals("cn", info.getCookieName());
    assertEquals("scn", info.getSessionCookieName());
    assertEquals("scd", info.getSessionCookieDomain());
    assertTrue(info.getUnsavedSSO());
    assertEquals("kdj", info.getKnownDeviceJwt());
    assertTrue(info.getSsoAuthenticationOnly());
    assertTrue(info.getUserCreationDeferred());

    UserResponse user = info.getUser();
    assertEquals("u1", user.getUserId());
    assertEquals(Arrays.asList("e1"), user.getExternalIds());
    assertTrue(user.getWebauthn());
    assertTrue(user.getPassword());
    assertTrue(user.getEditable());
    assertTrue(user.getScim());
    assertTrue(user.getPush());
    assertEquals(Arrays.asList("p1"), user.getPermissions());
    assertTrue(user.getOidc());
    assertEquals(13, user.getConsentExpiration());
    assertEquals("re", user.getRecoveryEmail());
    assertTrue(user.getVerifiedRecoveryEmail());
    assertEquals("rp", user.getRecoveryPhone());
    assertTrue(user.getVerifiedRecoveryPhone());
    assertEquals(14, user.getModifiedTime());
    assertEquals(Arrays.asList("r1"), user.getRoleIds());
    assertTrue(user.getRecoveryCodes());
    assertTrue(user.getDependent());
    assertEquals(Arrays.asList(new UserFamily("f1", Arrays.asList("fr"), Arrays.asList("fp"), mapOf("a", "b"))),
        user.getUserFamilies());
    assertEquals("st", user.getScimTenantId());
    assertEquals("sat", user.getSamlTenantId());
    assertEquals("ot", user.getOidcTenantId());
    assertEquals("ct", user.getConsentTenantId());
    assertEquals(15, user.getLastLoginTime());
  }

  @Test
  void testRefreshSessionWithoutExternalTokenSendsNoBody() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      jwtUtils.when(() -> JwtUtils.getToken(any(), any())).thenReturn(MOCK_TOKEN);
      refreshAuthService().refreshSessionWithTokenAuthenticationInfo(REFRESH_JWT);
    }
    verify(apiProxy).post(any(), isNull(), eq(JWTResponse.class));
  }

  @Test
  void testRefreshSessionWithExternalTokenMissingRefreshToken() {
    DescopeException thrown = assertThrows(DescopeException.class,
        () -> refreshAuthService().refreshSessionWithTokenAuthenticationInfo("", "ext"));
    assertEquals("refresh token", thrown.getMessage());
  }

  @Test
  void testSsoAuthenticationOnlyWithoutSession() {
    JWTResponse response = new JWTResponse();
    response.setSsoAuthenticationOnly(true);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      jwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      info = refreshAuthService().refreshSessionWithTokenAuthenticationInfo(REFRESH_JWT);

      // Without the flag a missing session JWT still fails
      response.setSsoAuthenticationOnly(null);
      assertThrows(DescopeException.class,
          () -> refreshAuthService().refreshSessionWithTokenAuthenticationInfo(REFRESH_JWT));
    }
    assertThat(info.getToken()).isNull();
    assertTrue(info.getSsoAuthenticationOnly());
  }

  private static AuthenticationService refreshAuthService() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return AuthenticationServiceBuilder.buildServices(client).getAuthService();
  }

}
