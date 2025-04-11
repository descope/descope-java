package com.descope.sdk.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.impl.AuthenticationServiceBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

  @Mock private Client client;
  @Mock private ApiProxy apiProxy;
  @Mock private SdkInfo sdkInfo;

  private OAuthService oauthService;
  private MockedStatic<ApiProxyBuilder> mockedStatic;

  @BeforeEach
  void setUp() {
    mockedStatic = mockStatic(ApiProxyBuilder.class);
    mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(SdkInfo.class))).thenReturn(apiProxy);
    when(client.getSdkInfo()).thenReturn(sdkInfo);
    oauthService = AuthenticationServiceBuilder.buildServices(client).getOauthService();
  }

  @AfterEach
  void tearDown() {
    if (mockedStatic != null) {
      mockedStatic.close();
    }
  }

  @Test
  void testStartWithAuthParams() throws DescopeException {
    // Setup
    String provider = "google";
    String redirectURL = "http://localhost:8080/callback";
    LoginOptions loginOptions = new LoginOptions();
    Map<String, String> authParams = new HashMap<>();
    authParams.put("prompt", "consent");
    authParams.put("access_type", "offline");

    // Mock the API response
    OAuthResponse mockResponse = new OAuthResponse();
    mockResponse.setUrl("https://oauth.google.com/auth?client_id=123&redirect_uri=http://localhost:8080/callback");

    when(apiProxy.post(any(), any(), eq(OAuthResponse.class))).thenReturn(mockResponse);

    // Execute
    String result = oauthService.start(provider, redirectURL, loginOptions, authParams);

    // Verify
    assertNotNull(result);
    assertTrue(result.contains("prompt=consent"));
    assertTrue(result.contains("access_type=offline"));
  }

  @Test
  void testStartSignInWithAuthParams() throws DescopeException {
    // Setup
    String provider = "google";
    String redirectURL = "http://localhost:8080/callback";
    LoginOptions loginOptions = new LoginOptions();
    Map<String, String> authParams = new HashMap<>();
    authParams.put("prompt", "consent");
    authParams.put("access_type", "offline");

    // Mock the API response
    OAuthResponse mockResponse = new OAuthResponse();
    mockResponse.setUrl("https://oauth.google.com/auth?client_id=123&redirect_uri=http://localhost:8080/callback");

    when(apiProxy.post(any(), any(), eq(OAuthResponse.class))).thenReturn(mockResponse);

    // Execute
    String result = oauthService.startSignIn(provider, redirectURL, loginOptions, authParams);

    // Verify
    assertNotNull(result);
    assertTrue(result.contains("prompt=consent"));
    assertTrue(result.contains("access_type=offline"));
  }

  @Test
  void testStartSignUpWithAuthParams() throws DescopeException {
    // Setup
    String provider = "google";
    String redirectURL = "http://localhost:8080/callback";
    LoginOptions loginOptions = new LoginOptions();
    Map<String, String> authParams = new HashMap<>();
    authParams.put("prompt", "consent");
    authParams.put("access_type", "offline");
    authParams.put("flow", "signup");

    // Mock the API response
    OAuthResponse mockResponse = new OAuthResponse();
    mockResponse.setUrl("https://oauth.google.com/auth?client_id=123&redirect_uri=http://localhost:8080/callback");

    when(apiProxy.post(any(), any(), eq(OAuthResponse.class))).thenReturn(mockResponse);

    // Execute
    String result = oauthService.startSignUp(provider, redirectURL, loginOptions, authParams);
    System.out.println("result: " + result);

    // Verify
    assertNotNull(result);
    assertTrue(result.contains("prompt=consent"));
    assertTrue(result.contains("access_type=offline"));
  }
} 