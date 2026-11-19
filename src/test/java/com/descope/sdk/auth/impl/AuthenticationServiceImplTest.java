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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
import com.descope.model.user.response.UserResponse;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.sdk.auth.OTPService;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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


  @Test
  void testLogoutAllWithLogoutTypeForEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> authenticationService.logoutAll("", Arrays.asList("web")));
    assertNotNull(thrown);
    assertEquals("refresh token", thrown.getMessage());
  }

  @Test
  void testLogoutAllWithLogoutTypeForSuccess() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, 200,
        "{\"sessionJwt\":\"s\",\"refreshJwt\":\"r\",\"cookieDomain\":\"example.com\",\"cookiePath\":\"/\","
            + "\"cookieMaxAge\":1,\"cookieExpiration\":2,\"user\":{\"userId\":\"u1\"},\"firstSeen\":true,"
            + "\"idpResponse\":{\"idpGroups\":[\"g\"]},\"sessionExpiration\":3,\"externalToken\":\"e\","
            + "\"claims\":{\"c\":\"v\"},\"tenantSSOID\":\"t1\",\"trustedDeviceJwt\":\"td\","
            + "\"nextRefreshSeconds\":4,\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\","
            + "\"sessionCookieDomain\":\"s.example.com\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"kd\","
            + "\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,\"unknownField\":1}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      final JWTResponse response =
          authenticationService.logoutAll("someRefreshToken", Arrays.asList("web", "mobile"));
      ClassicHttpRequest request = captor.getValue();
      assertEquals("POST", request.getMethod());
      assertTrue(request.getRequestUri().endsWith("/v1/auth/logoutall"));
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":someRefreshToken",
          request.getHeader("Authorization").getValue());
      Map<?, ?> body = requestBody(request);
      assertEquals(1, body.size());
      assertEquals(Arrays.asList("web", "mobile"), body.get("logoutType"));
      assertEquals("s", response.getSessionJwt());
      assertEquals("r", response.getRefreshJwt());
      assertEquals("example.com", response.getCookieDomain());
      assertEquals("/", response.getCookiePath());
      assertEquals(1, response.getCookieMaxAge());
      assertEquals(2, response.getCookieExpiration());
      assertEquals("u1", response.getUser().getUserId());
      assertTrue(response.getFirstSeen());
      assertEquals(Arrays.asList("g"), response.getIdpResponse().getIdpGroups());
      assertEquals(3, response.getSessionExpiration());
      assertEquals("e", response.getExternalToken());
      assertEquals("v", response.getClaims().get("c"));
      assertEquals("t1", response.getTenantSSOID());
      assertEquals("td", response.getTrustedDeviceJwt());
      assertEquals(4, response.getNextRefreshSeconds());
      assertEquals("DSR", response.getCookieName());
      assertEquals("DS", response.getSessionCookieName());
      assertEquals("s.example.com", response.getSessionCookieDomain());
      assertTrue(response.getUnsavedSSO());
      assertEquals("kd", response.getKnownDeviceJwt());
      assertTrue(response.getSsoAuthenticationOnly());
      assertTrue(response.getUserCreationDeferred());
    }
  }

  @Test
  void testLogoutAllWithoutLogoutType() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, 200, "{\"cookieName\":null,\"user\":null}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      JWTResponse response = authenticationService.logoutAll("someRefreshToken", null);
      assertTrue(requestBody(captor.getValue()).isEmpty());
      assertNotNull(response);
      assertNull(response.getUser());
    }
  }

  private CloseableHttpClient mockHttpClient(ArgumentCaptor<ClassicHttpRequest> captor, int statusCode,
      String responseBody) throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(statusCode);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    return httpClient;
  }

  private Map<?, ?> requestBody(ClassicHttpRequest request) throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }
}
