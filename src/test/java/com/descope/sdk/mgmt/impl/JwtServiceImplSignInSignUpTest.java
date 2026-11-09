package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ANONYMOUS_USER;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SIGN_IN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SIGN_UP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SIGN_UP_OR_IN;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.sdk.TestUtils.TENANTS_AUTHZ;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.ClientFunctionalException;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.MgmtSignInOptions;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.request.ManagementSignInRequest;
import com.descope.model.jwt.request.ManagementSignUpRequest;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.JwtService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class JwtServiceImplSignInSignUpTest {

  private static final Token MOCK_SESSION_TOKEN = Token.builder()
      .id("1")
      .projectId(PROJECT_ID)
      .jwt("someJwtToken")
      .claims(mapOf("someClaim", 1,
          "tenants", mapOf("someTenant", TENANTS_AUTHZ),
          "permissions", Arrays.asList("p1", "p2"), "roles", Arrays.asList("r1", "r2")))
      .build();
  private static final Token MOCK_REFRESH_TOKEN = Token.builder()
      .id("1")
      .projectId(PROJECT_ID)
      .jwt("someRefreshJwtToken")
      .claims(mapOf("someClaim", 1,
          "tenants", mapOf("someTenant", TENANTS_AUTHZ),
          "permissions", Arrays.asList("p1", "p2"), "roles", Arrays.asList("r1", "r2")))
      .build();

  private static final String MANAGEMENT_KEY = "some-management-key";

  private ApiProxy apiProxy;
  private JwtService jwtService;
  private MockedStatic<JwtUtils> mockedJwtUtils;
  private MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.jwtService = mgmtServices.getJwtService();
    this.apiProxy = mock(ApiProxy.class);
    mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
    mockedJwtUtils = mockStatic(JwtUtils.class);
  }

  @AfterEach
  void tearDown() {
    mockedApiProxyBuilder.close();
    mockedJwtUtils.close();
  }

  @Test
  void signIn_shouldReturnAuthenticationInfo_whenValidJWT() throws DescopeException {
    // Given
    String validJwt = MOCK_JWT_RESPONSE.getSessionJwt();
    String validRefreshJwt = MOCK_JWT_RESPONSE.getRefreshJwt();
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq(validJwt), any())).thenReturn(MOCK_SESSION_TOKEN);
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq(validRefreshJwt), any())).thenReturn(MOCK_REFRESH_TOKEN);
    JWTResponse mockResponse = new JWTResponse();
    mockResponse.setSessionJwt(validJwt);
    mockResponse.setRefreshJwt(validRefreshJwt);
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    // When
    String loginId = "mario@descope.com";
    LoginOptions loginOptions = new LoginOptions();
    AuthenticationInfo authInfo = jwtService.signIn(loginId, loginOptions);
    // Then
    assertNotNull(authInfo);
    assertNotNull(authInfo.getToken());
    assertNotNull(authInfo.getRefreshToken());
    assertEquals(MOCK_SESSION_TOKEN.getJwt(), authInfo.getToken().getJwt());
    assertEquals(MOCK_REFRESH_TOKEN.getJwt(), authInfo.getRefreshToken().getJwt());
    // Verify API call
    ArgumentCaptor<ManagementSignInRequest> captor = ArgumentCaptor.forClass(ManagementSignInRequest.class);
    verify(apiProxy).post(any(URI.class), captor.capture(), eq(JWTResponse.class));
    assertEquals(loginId, captor.getValue().getLoginId());
  }

  static Stream<String> signUpMethods() {
    return Stream.of("signUp", "signUpOrIn");
  }

  static Stream<String> signInAndUpMethods() {
    return Stream.of("signIn", "signUp", "signUpOrIn");
  }

  @ParameterizedTest
  @MethodSource("signUpMethods")
  void signUp_shouldReturnAuthenticationInfo_whenValidJWT(String method) throws DescopeException {
    // Given
    String validJwt = MOCK_JWT_RESPONSE.getSessionJwt();
    String validRefreshJwt = MOCK_JWT_RESPONSE.getRefreshJwt();

    mockedJwtUtils.when(() -> JwtUtils.getToken(eq(validJwt), any())).thenReturn(MOCK_SESSION_TOKEN);
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq(validRefreshJwt), any())).thenReturn(MOCK_REFRESH_TOKEN);

    JWTResponse mockResponse = new JWTResponse();
    mockResponse.setSessionJwt(validJwt);
    mockResponse.setRefreshJwt(validRefreshJwt);

    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);

    // When
    String loginId = "mario@descope.com";
    AuthenticationInfo authInfo;
    if ("signUp".equals(method)) {
      authInfo = jwtService.signUp(loginId, new MgmtSignUpUser());
    } else {
      authInfo = jwtService.signUpOrIn(loginId, new MgmtSignUpUser());
    }
    // Then
    assertNotNull(authInfo);
    assertNotNull(authInfo.getToken());
    assertNotNull(authInfo.getRefreshToken());
    assertEquals(MOCK_SESSION_TOKEN.getJwt(), authInfo.getToken().getJwt());
    assertEquals(MOCK_REFRESH_TOKEN.getJwt(), authInfo.getRefreshToken().getJwt());
    // Verify API call
    ArgumentCaptor<ManagementSignUpRequest> captor = ArgumentCaptor.forClass(ManagementSignUpRequest.class);
    verify(apiProxy).post(any(URI.class), captor.capture(), eq(JWTResponse.class));
    assertEquals(loginId, captor.getValue().getLoginId());
  }

  @ParameterizedTest
  @MethodSource("signInAndUpMethods")
  void signIn_shouldThrowException_whenInvalidJWT(String method) {
    // Given
    JWTResponse mockResponse = new JWTResponse();
    mockResponse.setSessionJwt(null); // Simulating an invalid JWT response
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    // When & Then
    final String loginId = "user@example.com";
    DescopeException exception = assertThrows(ClientFunctionalException.class, () -> {
      switch (method) {
        case "signIn":
          jwtService.signIn(loginId, new LoginOptions());
          verify(apiProxy).post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class));
          break;
        case "signUp":
          jwtService.signUp(loginId, new MgmtSignUpUser());
          verify(apiProxy).post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class));
          break;
        case "signUpOrIn":
          jwtService.signUpOrIn(loginId, new MgmtSignUpUser());
          verify(apiProxy).post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class));
          break;
        default:
          throw new NotImplementedException("Method not implemented: " + method);
      }

    });
    assertEquals("Invalid Token", exception.getMessage());
  }

  @SuppressWarnings("unchecked")
  private JwtService jwtServiceWithManagementKey(AtomicReference<String> authHeader) {
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return new JwtServiceImpl(client);
  }

  private JWTResponse fullJwtResponse() {
    String validJwt = MOCK_JWT_RESPONSE.getSessionJwt();
    String validRefreshJwt = MOCK_JWT_RESPONSE.getRefreshJwt();
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq(validJwt), any())).thenReturn(MOCK_SESSION_TOKEN);
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq(validRefreshJwt), any())).thenReturn(MOCK_REFRESH_TOKEN);
    JWTResponse response = new JWTResponse();
    response.setSessionJwt(validJwt);
    response.setRefreshJwt(validRefreshJwt);
    response.setFirstSeen(true);
    response.setSessionExpiration(100);
    response.setExternalToken("external-token");
    response.setClaims(mapOf("claim", "value"));
    response.setTenantSSOID("tenant-sso-id");
    response.setTrustedDeviceJwt("trusted-device-jwt");
    response.setNextRefreshSeconds(200);
    response.setCookieName("cookie-name");
    response.setSessionCookieName("session-cookie-name");
    response.setSessionCookieDomain("session-cookie-domain");
    response.setUnsavedSSO(true);
    response.setKnownDeviceJwt("known-device-jwt");
    response.setSsoAuthenticationOnly(false);
    response.setUserCreationDeferred(true);
    return response;
  }

  private void assertFullAuthInfo(AuthenticationInfo authInfo) {
    assertEquals(MOCK_SESSION_TOKEN.getJwt(), authInfo.getToken().getJwt());
    assertEquals(MOCK_REFRESH_TOKEN.getJwt(), authInfo.getRefreshToken().getJwt());
    assertTrue(authInfo.getFirstSeen());
    assertEquals(100, authInfo.getSessionExpiration());
    assertEquals("external-token", authInfo.getExternalToken());
    assertEquals(mapOf("claim", "value"), authInfo.getClaims());
    assertEquals("tenant-sso-id", authInfo.getTenantSSOID());
    assertEquals("trusted-device-jwt", authInfo.getTrustedDeviceJwt());
    assertEquals(200, authInfo.getNextRefreshSeconds());
    assertEquals("cookie-name", authInfo.getCookieName());
    assertEquals("session-cookie-name", authInfo.getSessionCookieName());
    assertEquals("session-cookie-domain", authInfo.getSessionCookieDomain());
    assertTrue(authInfo.getUnsavedSSO());
    assertEquals("known-device-jwt", authInfo.getKnownDeviceJwt());
    assertFalse(authInfo.getSsoAuthenticationOnly());
    assertTrue(authInfo.getUserCreationDeferred());
  }

  @Test
  void signIn_withSignInOptions_shouldSendOptionsAndReturnFullResponse() throws DescopeException {
    AtomicReference<String> authHeader = new AtomicReference<>();
    JwtService service = jwtServiceWithManagementKey(authHeader);
    JWTResponse mockResponse = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);

    LoginOptions loginOptions = LoginOptions.builder().customClaims(mapOf("k", "v")).build();
    MgmtSignInOptions signInOptions = MgmtSignInOptions.builder()
        .ssoAppId("sso-app")
        .refreshDuration(3600)
        .selectedTenantId("tenant-1")
        .ignoreEnforceSSO(true)
        .build();
    AuthenticationInfo authInfo = service.signIn("mario@descope.com", loginOptions, signInOptions);

    assertFullAuthInfo(authInfo);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<ManagementSignInRequest> captor = ArgumentCaptor.forClass(ManagementSignInRequest.class);
    verify(apiProxy).post(uriCaptor.capture(), captor.capture(), eq(JWTResponse.class));
    assertEquals(MANAGEMENT_SIGN_IN, uriCaptor.getValue().getPath());
    ManagementSignInRequest request = captor.getValue();
    assertEquals("mario@descope.com", request.getLoginId());
    assertEquals(mapOf("k", "v"), request.getCustomClaims());
    assertEquals("sso-app", request.getSsoAppId());
    assertEquals(3600, request.getRefreshDuration());
    assertEquals("tenant-1", request.getSelectedTenantId());
    assertTrue(request.getIgnoreEnforceSSO());
  }

  @Test
  void signIn_withoutSignInOptions_shouldOmitOptions() throws DescopeException {
    JWTResponse mockResponse = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    jwtService.signIn("mario@descope.com", null, null);
    ArgumentCaptor<ManagementSignInRequest> captor = ArgumentCaptor.forClass(ManagementSignInRequest.class);
    verify(apiProxy).post(any(URI.class), captor.capture(), eq(JWTResponse.class));
    ManagementSignInRequest request = captor.getValue();
    assertNull(request.getSsoAppId());
    assertNull(request.getRefreshDuration());
    assertNull(request.getSelectedTenantId());
    assertNull(request.getIgnoreEnforceSSO());
  }

  @Test
  void signIn_withSignInOptions_shouldFailOnEmptyLoginId() {
    assertThrows(ServerCommonException.class,
        () -> jwtService.signIn("", null, MgmtSignInOptions.builder().ssoAppId("sso-app").build()));
    verify(apiProxy, never()).post(any(URI.class), any(), any());
  }

  @ParameterizedTest
  @MethodSource("signUpMethods")
  void signUp_withNewFields_shouldSendFieldsAndReturnFullResponse(String method) throws DescopeException {
    AtomicReference<String> authHeader = new AtomicReference<>();
    JwtService service = jwtServiceWithManagementKey(authHeader);
    JWTResponse mockResponse = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);

    User user = User.builder()
        .name("Mario")
        .email("mario@descope.com")
        .recoveryEmail("recovery@descope.com")
        .recoveryPhone("+15555555555")
        .build();
    MgmtSignUpUser details = MgmtSignUpUser.builder()
        .user(user)
        .refreshDuration(3600)
        .selectedTenantId("tenant-1")
        .userTenants(Arrays.asList(
            AssociatedTenant.builder().tenantId("tenant-1").tenantName("ignored")
                .roleNames(Arrays.asList("admin")).build(),
            AssociatedTenant.builder().tenantId("tenant-2").build()))
        .ignoreEnforceSSO(true)
        .build();
    AuthenticationInfo authInfo = "signUp".equals(method)
        ? service.signUp("mario@descope.com", details)
        : service.signUpOrIn("mario@descope.com", details);

    assertFullAuthInfo(authInfo);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<ManagementSignUpRequest> captor = ArgumentCaptor.forClass(ManagementSignUpRequest.class);
    verify(apiProxy).post(uriCaptor.capture(), captor.capture(), eq(JWTResponse.class));
    assertEquals("signUp".equals(method) ? MANAGEMENT_SIGN_UP : MANAGEMENT_SIGN_UP_OR_IN,
        uriCaptor.getValue().getPath());
    ManagementSignUpRequest request = captor.getValue();
    assertEquals("mario@descope.com", request.getLoginId());
    assertEquals("recovery@descope.com", request.getUser().getRecoveryEmail());
    assertEquals("+15555555555", request.getUser().getRecoveryPhone());
    assertEquals(3600, request.getRefreshDuration());
    assertEquals("tenant-1", request.getSelectedTenantId());
    assertTrue(request.getIgnoreEnforceSSO());
    List<Map<String, Object>> userTenants = request.getUserTenants();
    assertEquals(2, userTenants.size());
    assertEquals(mapOf("tenantId", "tenant-1", "roleNames", Arrays.asList("admin")), userTenants.get(0));
    assertEquals(mapOf("tenantId", "tenant-2"), userTenants.get(1));
  }

  @ParameterizedTest
  @MethodSource("signUpMethods")
  void signUp_withoutNewFields_shouldOmitThem(String method) throws DescopeException {
    JWTResponse mockResponse = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    if ("signUp".equals(method)) {
      jwtService.signUp("mario@descope.com", null);
    } else {
      jwtService.signUpOrIn("mario@descope.com", null);
    }
    ArgumentCaptor<ManagementSignUpRequest> captor = ArgumentCaptor.forClass(ManagementSignUpRequest.class);
    verify(apiProxy).post(any(URI.class), captor.capture(), eq(JWTResponse.class));
    ManagementSignUpRequest request = captor.getValue();
    assertNull(request.getRefreshDuration());
    assertNull(request.getSelectedTenantId());
    assertNull(request.getUserTenants());
    assertNull(request.getIgnoreEnforceSSO());
  }

  @Test
  void anonymous_withRoles_shouldSendRoles() throws DescopeException {
    AtomicReference<String> authHeader = new AtomicReference<>();
    JwtService service = jwtServiceWithManagementKey(authHeader);
    JWTResponse mockResponse = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(AnonymousUserRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);

    AuthenticationInfo authInfo = service.anonymous(AnonymousUserRequest.builder()
        .selectedTenant("tenant-1")
        .roles(Arrays.asList("viewer", "editor"))
        .build());

    assertEquals(MOCK_SESSION_TOKEN.getJwt(), authInfo.getToken().getJwt());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<AnonymousUserRequest> captor = ArgumentCaptor.forClass(AnonymousUserRequest.class);
    verify(apiProxy).post(uriCaptor.capture(), captor.capture(), eq(JWTResponse.class));
    assertEquals(MANAGEMENT_ANONYMOUS_USER, uriCaptor.getValue().getPath());
    assertEquals("tenant-1", captor.getValue().getSelectedTenant());
    assertEquals(Arrays.asList("viewer", "editor"), captor.getValue().getRoles());
  }

  @Test
  void jwtResponse_shouldDeserializeNewFields() throws Exception {
    String json = "{\"sessionExpiration\":1,\"externalToken\":\"et\",\"claims\":{\"c\":\"v\"},"
        + "\"tenantSSOID\":\"ts\",\"trustedDeviceJwt\":\"td\",\"nextRefreshSeconds\":2,"
        + "\"cookieName\":\"cn\",\"sessionCookieName\":\"scn\",\"sessionCookieDomain\":\"scd\","
        + "\"unsavedSSO\":true,\"knownDeviceJwt\":\"kd\",\"ssoAuthenticationOnly\":true,"
        + "\"userCreationDeferred\":true,\"unknownField\":null}";
    JWTResponse response = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    assertEquals(1, response.getSessionExpiration());
    assertEquals("et", response.getExternalToken());
    assertEquals(mapOf("c", "v"), response.getClaims());
    assertEquals("ts", response.getTenantSSOID());
    assertEquals("td", response.getTrustedDeviceJwt());
    assertEquals(2, response.getNextRefreshSeconds());
    assertEquals("cn", response.getCookieName());
    assertEquals("scn", response.getSessionCookieName());
    assertEquals("scd", response.getSessionCookieDomain());
    assertTrue(response.getUnsavedSSO());
    assertEquals("kd", response.getKnownDeviceJwt());
    assertTrue(response.getSsoAuthenticationOnly());
    assertTrue(response.getUserCreationDeferred());
  }

}
