package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.model.webauthn.WebAuthnFinishRequest;
import com.descope.model.webauthn.WebAuthnTransactionResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.WebAuthnService;
import com.descope.utils.JwtUtils;
import java.security.Key;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class WebAuthnServiceImplTest {

  private WebAuthnService webAuthnService;

  @BeforeEach
  void setUp() {
    AuthParams authParams = TestUtils.getAuthParams();
    Client client = TestUtils.getClient();
    this.webAuthnService =
        AuthenticationServiceBuilder.buildServices(client, authParams).getWebAuthnService();
  }

  @Test
  void testSignUpStartForSuccess() {
    User user = new User(MOCK_NAME, MOCK_EMAIL, MOCK_PHONE);
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse signUp = webAuthnService.signUpStart(MOCK_EMAIL, user, "kuku");
      assertThat(signUp).isNotNull();
      assertEquals("t1", signUp.getTransactionId());
    }
  }

  @Test
  void testSignUpStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpStart(null, null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpStart("x", null, null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    Provider provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        AuthenticationInfo authenticationInfo = webAuthnService.signUpFinish(new WebAuthnFinishRequest("t1", "r"));
        assertThat(authenticationInfo).isNotNull();
        Token sessionToken = authenticationInfo.getToken();
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getJwt()).isNotBlank();
        assertThat(sessionToken.getClaims()).isNotEmpty();
        assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);
        Token refreshToken = authenticationInfo.getRefreshToken();
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getJwt()).isNotBlank();
        assertThat(refreshToken.getClaims()).isNotEmpty();
        assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);
        UserResponse user = authenticationInfo.getUser();
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isNotBlank();
        assertThat(user.getLoginIds()).isNotEmpty();
      }
    }
  }

  @Test
  void testSignUpFinishEmptyRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpFinish(null));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpFinish(new WebAuthnFinishRequest(null, null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpFinish(new WebAuthnFinishRequest("kuku", null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse signUp = webAuthnService.signInStart(MOCK_EMAIL, "kuku", null, null);
      assertThat(signUp).isNotNull();
      assertEquals("t1", signUp.getTransactionId());
    }
  }

  @Test
  void testSignInStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInStart(null, null, null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInStart("x", null, null, null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartEmptyToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInStart("x", "x", null, new LoginOptions(true, false, null)));
    assertNotNull(thrown);
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    Provider provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        AuthenticationInfo authenticationInfo = webAuthnService.signInFinish(new WebAuthnFinishRequest("t1", "r"));
        assertThat(authenticationInfo).isNotNull();
        Token sessionToken = authenticationInfo.getToken();
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getJwt()).isNotBlank();
        assertThat(sessionToken.getClaims()).isNotEmpty();
        assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);
        Token refreshToken = authenticationInfo.getRefreshToken();
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getJwt()).isNotBlank();
        assertThat(refreshToken.getClaims()).isNotEmpty();
        assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);
        UserResponse user = authenticationInfo.getUser();
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isNotBlank();
        assertThat(user.getLoginIds()).isNotEmpty();
      }
    }
  }

  @Test
  void testSignInFinishEmptyRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInFinish(null));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInFinish(new WebAuthnFinishRequest(null, null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signInFinish(new WebAuthnFinishRequest("kuku", null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse signUp = webAuthnService.signUpOrInStart(MOCK_EMAIL, "kuku");
      assertThat(signUp).isNotNull();
      assertEquals("t1", signUp.getTransactionId());
    }
  }

  @Test
  void testSignUpOrInStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart(null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignUpOrInStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.signUpOrInStart("x", null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    WebAuthnTransactionResponse response = new WebAuthnTransactionResponse("t1", "o", false);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    Provider provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        WebAuthnTransactionResponse signUp = webAuthnService.updateUserDeviceStart(MOCK_EMAIL, "kuku", "kiki");
        assertThat(signUp).isNotNull();
        assertEquals("t1", signUp.getTransactionId());
      }
    }
  }

  @Test
  void testUpdateUserDeviceStartEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart(null, null, null));
    assertNotNull(thrown);
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartEmptyOrigin() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart("x", null, null));
    assertNotNull(thrown);
    assertEquals("The Origin argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartEmptyToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceStart("x", "x", null));
    assertNotNull(thrown);
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    Provider provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      webAuthnService.updateUserDeviceFinish(new WebAuthnFinishRequest("t1", "r"));
    }
  }

  @Test
  void testUpdateUserDeviceFinishEmptyRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceFinish(null));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceFinish(new WebAuthnFinishRequest(null, null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(ServerCommonException.class,
          () -> webAuthnService.updateUserDeviceFinish(new WebAuthnFinishRequest("kuku", null)));
    assertNotNull(thrown);
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }

}
