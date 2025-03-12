package com.descope.sdk.mgmt.impl;

import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static com.descope.sdk.TestUtils.TENANTS_AUTHZ;
import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.ClientFunctionalException;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.Token;
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
    String loginId = "mario@descope.com";
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
    String loginId = "user@example.com";
    JWTResponse mockResponse = new JWTResponse();
    mockResponse.setSessionJwt(null); // Simulating an invalid JWT response
    when(apiProxy.post(any(URI.class), any(ManagementSignInRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    when(apiProxy.post(any(URI.class), any(ManagementSignUpRequest.class), eq(JWTResponse.class)))
        .thenReturn(mockResponse);
    // When & Then
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

}