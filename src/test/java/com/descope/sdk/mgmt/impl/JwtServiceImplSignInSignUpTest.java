package com.descope.sdk.mgmt.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.sdk.TestUtils.TENANTS_AUTHZ;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.ClientFunctionalException;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.MgmtLoginOptions;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.request.ManagementSignInRequest;
import com.descope.model.jwt.request.ManagementSignUpRequest;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.JwtService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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

  private static final String MANAGEMENT_KEY = "some-management-key";

  private JwtService jwtServiceWithKey(AtomicReference<String> authHeader) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      @SuppressWarnings("unchecked")
      Supplier<String> supplier = (Supplier<String>) inv.getArgument(0);
      authHeader.set(supplier.get());
      return apiProxy;
    });
    return ManagementServiceBuilder.buildServices(client).getJwtService();
  }

  private JWTResponse fullJwtResponse() {
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq("someSessionJwt"), any())).thenReturn(MOCK_SESSION_TOKEN);
    mockedJwtUtils.when(() -> JwtUtils.getToken(eq("someRefreshJwt"), any())).thenReturn(MOCK_REFRESH_TOKEN);
    JWTResponse response = new JWTResponse();
    response.setSessionJwt("someSessionJwt");
    response.setRefreshJwt("someRefreshJwt");
    response.setFirstSeen(true);
    response.setSessionExpiration(3600);
    response.setExternalToken("ext-token");
    response.setClaims(mapOf("c1", "v1"));
    response.setTenantSSOID("sso-id");
    response.setTrustedDeviceJwt("trusted-jwt");
    response.setNextRefreshSeconds(60);
    response.setCookieName("DSR");
    response.setSessionCookieName("DS");
    response.setSessionCookieDomain("example.com");
    response.setUnsavedSSO(true);
    response.setKnownDeviceJwt("known-jwt");
    response.setUserCreationDeferred(true);
    response.setExternalPasskey(true);
    return response;
  }

  private static void assertFullAuthInfo(AuthenticationInfo authInfo) {
    assertEquals(MOCK_SESSION_TOKEN.getJwt(), authInfo.getToken().getJwt());
    assertEquals(MOCK_REFRESH_TOKEN.getJwt(), authInfo.getRefreshToken().getJwt());
    assertEquals(true, authInfo.getFirstSeen());
    assertEquals(3600, authInfo.getSessionExpiration());
    assertEquals("ext-token", authInfo.getExternalToken());
    assertEquals(mapOf("c1", "v1"), authInfo.getClaims());
    assertEquals("sso-id", authInfo.getTenantSSOID());
    assertEquals("trusted-jwt", authInfo.getTrustedDeviceJwt());
    assertEquals(60, authInfo.getNextRefreshSeconds());
    assertEquals("DSR", authInfo.getCookieName());
    assertEquals("DS", authInfo.getSessionCookieName());
    assertEquals("example.com", authInfo.getSessionCookieDomain());
    assertEquals(true, authInfo.getUnsavedSSO());
    assertEquals("known-jwt", authInfo.getKnownDeviceJwt());
    assertEquals(true, authInfo.getUserCreationDeferred());
    assertEquals(true, authInfo.getExternalPasskey());
  }

  @Test
  void signIn_withMgmtLoginOptions_sendsNewFields() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    JwtService service = jwtServiceWithKey(authHeader);
    JWTResponse response = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(response);
    LoginOptions loginOptions = LoginOptions.builder().customClaims(mapOf("k", "v")).build();
    MgmtLoginOptions mgmtLoginOptions = MgmtLoginOptions.builder()
        .ssoAppId("app-id")
        .refreshDuration(3600)
        .selectedTenantId("t1")
        .ignoreEnforceSSO(true)
        .build();
    AuthenticationInfo authInfo = service.signIn("user@example.com", loginOptions, mgmtLoginOptions);
    assertFullAuthInfo(authInfo);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<ManagementSignInRequest> captor = ArgumentCaptor.forClass(ManagementSignInRequest.class);
    verify(apiProxy).post(uriCaptor.capture(), captor.capture(), eq(JWTResponse.class));
    assertEquals("/v1/mgmt/auth/signin", uriCaptor.getValue().getPath());
    ManagementSignInRequest req = captor.getValue();
    assertEquals("user@example.com", req.getLoginId());
    assertEquals(mapOf("k", "v"), req.getCustomClaims());
    assertEquals("app-id", req.getSsoAppId());
    assertEquals(3600, req.getRefreshDuration());
    assertEquals("t1", req.getSelectedTenantId());
    assertEquals(true, req.getIgnoreEnforceSSO());
  }

  @Test
  void signIn_withoutMgmtLoginOptions_omitsNewFields() {
    JWTResponse response = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(response);
    jwtService.signIn("user@example.com", new LoginOptions());
    ArgumentCaptor<ManagementSignInRequest> captor = ArgumentCaptor.forClass(ManagementSignInRequest.class);
    verify(apiProxy).post(any(URI.class), captor.capture(), eq(JWTResponse.class));
    assertNull(captor.getValue().getSsoAppId());
    assertNull(captor.getValue().getRefreshDuration());
    assertNull(captor.getValue().getSelectedTenantId());
    assertNull(captor.getValue().getIgnoreEnforceSSO());
  }

  @Test
  void signIn_withMgmtLoginOptions_failsOnBlankLoginId() {
    assertThrows(ServerCommonException.class,
        () -> jwtService.signIn("", new LoginOptions(), new MgmtLoginOptions()));
  }

  @ParameterizedTest
  @MethodSource("signUpMethods")
  void signUp_withNewFields_sendsThem(String method) {
    AtomicReference<String> authHeader = new AtomicReference<>();
    JwtService service = jwtServiceWithKey(authHeader);
    JWTResponse response = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(response);
    MgmtSignUpUser details = MgmtSignUpUser.builder()
        .ssoAppId("app-id")
        .refreshDuration(3600)
        .selectedTenantId("t1")
        .userTenants(Collections.singletonList(AssociatedTenant.builder()
            .tenantId("t1").tenantName("ignored").roleNames(Arrays.asList("r1")).build()))
        .ignoreEnforceSSO(true)
        .build();
    AuthenticationInfo authInfo = "signUp".equals(method)
        ? service.signUp("user@example.com", details)
        : service.signUpOrIn("user@example.com", details);
    assertFullAuthInfo(authInfo);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<ManagementSignUpRequest> captor = ArgumentCaptor.forClass(ManagementSignUpRequest.class);
    verify(apiProxy).post(uriCaptor.capture(), captor.capture(), eq(JWTResponse.class));
    assertEquals("signUp".equals(method) ? "/v1/mgmt/auth/signup" : "/v1/mgmt/auth/signup-in",
        uriCaptor.getValue().getPath());
    ManagementSignUpRequest req = captor.getValue();
    assertEquals("user@example.com", req.getLoginId());
    assertEquals("app-id", req.getSsoAppId());
    assertEquals(3600, req.getRefreshDuration());
    assertEquals("t1", req.getSelectedTenantId());
    assertEquals(true, req.getIgnoreEnforceSSO());
    assertEquals(1, req.getUserTenants().size());
    assertEquals("t1", req.getUserTenants().get(0).getTenantId());
    assertNull(req.getUserTenants().get(0).getTenantName());
    assertEquals(Arrays.asList("r1"), req.getUserTenants().get(0).getRoleNames());
  }

  @ParameterizedTest
  @MethodSource("signUpMethods")
  void signUp_withoutNewFields_omitsThem(String method) {
    JWTResponse response = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(response);
    if ("signUp".equals(method)) {
      jwtService.signUp("user@example.com", null);
    } else {
      jwtService.signUpOrIn("user@example.com", null);
    }
    ArgumentCaptor<ManagementSignUpRequest> captor = ArgumentCaptor.forClass(ManagementSignUpRequest.class);
    verify(apiProxy).post(any(URI.class), captor.capture(), eq(JWTResponse.class));
    assertNull(captor.getValue().getRefreshDuration());
    assertNull(captor.getValue().getSelectedTenantId());
    assertNull(captor.getValue().getUserTenants());
    assertNull(captor.getValue().getIgnoreEnforceSSO());
  }

  @Test
  void anonymous_withRoles_sendsThem() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    JwtService service = jwtServiceWithKey(authHeader);
    JWTResponse response = fullJwtResponse();
    when(apiProxy.post(any(URI.class), any(AnonymousUserRequest.class), eq(JWTResponse.class)))
        .thenReturn(response);
    AnonymousUserRequest request = AnonymousUserRequest.builder()
        .customClaims(mapOf("k", "v"))
        .selectedTenant("t1")
        .refreshDuration(3600)
        .roles(Arrays.asList("r1", "r2"))
        .build();
    AuthenticationInfo authInfo = service.anonymous(request);
    assertEquals(MOCK_SESSION_TOKEN.getJwt(), authInfo.getToken().getJwt());
    assertEquals(MOCK_REFRESH_TOKEN.getJwt(), authInfo.getRefreshToken().getJwt());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<AnonymousUserRequest> captor = ArgumentCaptor.forClass(AnonymousUserRequest.class);
    verify(apiProxy).post(uriCaptor.capture(), captor.capture(), eq(JWTResponse.class));
    assertEquals("/v1/mgmt/auth/anonymous", uriCaptor.getValue().getPath());
    assertEquals(Arrays.asList("r1", "r2"), captor.getValue().getRoles());
    assertEquals("t1", captor.getValue().getSelectedTenant());
    assertEquals(3600, captor.getValue().getRefreshDuration());
  }

  @Test
  void existingConstructors_leaveNewFieldsUnset() {
    assertNull(new AnonymousUserRequest(null, "t1", 0).getRoles());
    ManagementSignInRequest signIn = new ManagementSignInRequest("u", false, false, false,
        new String[] {"s"}, null, null);
    assertArrayEquals(new String[] {"s"}, signIn.getRevokeOtherSessionsTypes());
    assertNull(signIn.getRefreshDuration());
    assertNull(new ManagementSignUpRequest("u", null, false, false, null, null).getUserTenants());
    assertNull(new MgmtSignUpUser(null, false, false, null, null).getRefreshDuration());
  }
}