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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AccessKeyLoginOptions;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.client.Client;
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

  private static final String REFRESH_RESPONSE = "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\","
      + "\"cookieDomain\":\"cd\",\"cookiePath\":\"/p\",\"cookieMaxAge\":10,\"cookieExpiration\":20,"
      + "\"firstSeen\":true,\"sessionExpiration\":30,\"externalToken\":\"et\",\"claims\":{\"a\":\"b\"},"
      + "\"tenantSSOID\":\"tsso\",\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":40,"
      + "\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\","
      + "\"unsavedSSO\":true,\"knownDeviceJwt\":\"kdj\",\"ssoAuthenticationOnly\":true,"
      + "\"userCreationDeferred\":true,"
      + "\"idpResponse\":{\"samlResponse\":\"sr\",\"samlGeneratedUser\":\"sgu\",\"samlGeneratedRoles\":\"sgr\","
      + "\"oidcResponse\":\"or\",\"oidcGeneratedUser\":\"ogu\",\"oidcGeneratedRoles\":\"ogr\"},"
      + "\"user\":{\"userId\":\"u1\",\"TOTP\":true,\"SAML\":true,\"OAuth\":{\"google\":true},"
      + "\"externalIds\":[\"e1\"],\"webauthn\":true,\"password\":true,\"editable\":true,\"SCIM\":true,"
      + "\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,\"consentExpiration\":50,"
      + "\"recoveryEmail\":\"re@example.com\",\"verifiedRecoveryEmail\":true,\"recoveryPhone\":\"+15555555555\","
      + "\"verifiedRecoveryPhone\":true,\"modifiedTime\":60,\"roleIds\":[\"ri1\"],\"recoveryCodes\":true,"
      + "\"dependent\":true,\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],"
      + "\"permissions\":[\"fp\"],\"familyScopedAttributes\":{\"k\":\"v\"}}],\"scimTenantId\":\"st\","
      + "\"samlTenantId\":\"sat\",\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":70,"
      + "\"userTenants\":[{\"tenantId\":\"t1\",\"permissions\":[\"tp\"],\"roleIds\":[\"tri\"]}]}}";

  @Test
  @SuppressWarnings("unchecked")
  void testRefreshSessionWithTokenAuthenticationInfo() throws Exception {
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(REFRESH_RESPONSE, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      info = authenticationService.refreshSessionWithTokenAuthenticationInfo(MOCK_REFRESH_TOKEN, "ext");
    }

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(JWTResponse.class));
    assertEquals("/v1/auth/refresh", uriCaptor.getValue().getPath());
    assertEquals(mapOf("externalToken", "ext"), bodyCaptor.getValue());
    assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, authHeader.get());

    assertNotNull(info.getToken());
    assertNotNull(info.getRefreshToken());
    assertTrue(info.getFirstSeen());
    assertEquals("cd", info.getCookieDomain());
    assertEquals("/p", info.getCookiePath());
    assertEquals(10, info.getCookieMaxAge());
    assertEquals(20, info.getCookieExpiration());
    assertEquals(30, info.getSessionExpiration());
    assertEquals("et", info.getExternalToken());
    assertEquals(mapOf("a", "b"), info.getClaims());
    assertEquals("tsso", info.getTenantSSOID());
    assertEquals("tdj", info.getTrustedDeviceJwt());
    assertEquals(40, info.getNextRefreshSeconds());
    assertEquals("cn", info.getCookieName());
    assertEquals("scn", info.getSessionCookieName());
    assertEquals("scd", info.getSessionCookieDomain());
    assertTrue(info.getUnsavedSSO());
    assertEquals("kdj", info.getKnownDeviceJwt());
    assertTrue(info.getSsoAuthenticationOnly());
    assertTrue(info.getUserCreationDeferred());
    assertEquals("sr", info.getIdpResponse().getSamlResponse());
    assertEquals("sgu", info.getIdpResponse().getSamlGeneratedUser());
    assertEquals("sgr", info.getIdpResponse().getSamlGeneratedRoles());
    assertEquals("or", info.getIdpResponse().getOidcResponse());
    assertEquals("ogu", info.getIdpResponse().getOidcGeneratedUser());
    assertEquals("ogr", info.getIdpResponse().getOidcGeneratedRoles());

    UserResponse user = info.getUser();
    assertTrue(user.getTOTP());
    assertTrue(user.getSAML());
    assertEquals(mapOf("google", true), user.getOAuth());
    assertEquals(Arrays.asList("e1"), user.getExternalIds());
    assertTrue(user.getWebauthn());
    assertTrue(user.getPassword());
    assertTrue(user.getEditable());
    assertTrue(user.getSCIM());
    assertTrue(user.getPush());
    assertEquals(Arrays.asList("p1"), user.getPermissions());
    assertTrue(user.getOIDC());
    assertEquals(50L, user.getConsentExpiration());
    assertEquals("re@example.com", user.getRecoveryEmail());
    assertTrue(user.getVerifiedRecoveryEmail());
    assertEquals("+15555555555", user.getRecoveryPhone());
    assertTrue(user.getVerifiedRecoveryPhone());
    assertEquals(60L, user.getModifiedTime());
    assertEquals(Arrays.asList("ri1"), user.getRoleIds());
    assertTrue(user.getRecoveryCodes());
    assertTrue(user.getDependent());
    UserFamily family = user.getUserFamilies().get(0);
    assertEquals("f1", family.getFamilyId());
    assertEquals(Arrays.asList("fr"), family.getRoleNames());
    assertEquals(Arrays.asList("fp"), family.getPermissions());
    assertEquals(mapOf("k", "v"), family.getFamilyScopedAttributes());
    assertEquals("st", user.getScimTenantId());
    assertEquals("sat", user.getSamlTenantId());
    assertEquals("ot", user.getOidcTenantId());
    assertEquals("ct", user.getConsentTenantId());
    assertEquals(70L, user.getLastLoginTime());
    assertEquals(Arrays.asList("tp"), user.getUserTenants().get(0).getPermissions());
    assertEquals(Arrays.asList("tri"), user.getUserTenants().get(0).getRoleIds());
  }

  @Test
  void testRefreshSessionWithTokenAuthenticationInfoWithoutExternalToken() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(TestUtils.MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
        MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
      authenticationService.refreshSessionWithTokenAuthenticationInfo(MOCK_REFRESH_TOKEN);
      authenticationService.refreshSessionWithTokenAuthenticationInfo(MOCK_REFRESH_TOKEN, "");
    }
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(2)).post(any(), bodyCaptor.capture(), eq(JWTResponse.class));
    bodyCaptor.getAllValues().forEach(body -> assertNull(body));
  }

  @Test
  void testRefreshSessionWithTokenAuthenticationInfoMissingRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> authenticationService.refreshSessionWithTokenAuthenticationInfo("", "ext"));
    assertEquals("refresh token", thrown.getMessage());
  }

}
