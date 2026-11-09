package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_NAME;
import static com.descope.sdk.TestUtils.MOCK_PHONE;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.enums.WebAuthnAttestation;
import com.descope.enums.WebAuthnAuthenticatorAttachment;
import com.descope.enums.WebAuthnRequireResidentKey;
import com.descope.enums.WebAuthnResidentKey;
import com.descope.enums.WebAuthnUserVerification;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;
import com.descope.model.user.response.UserFamily;
import com.descope.model.user.response.UserResponse;
import com.descope.model.webauthn.AuthenticatorSelectionCriteria;
import com.descope.model.webauthn.PasskeyOptions;
import com.descope.model.webauthn.WebAuthnFinishRequest;
import com.descope.model.webauthn.WebAuthnTransactionResponse;
import com.descope.model.webauthn.WebAuthnUpdateFinishResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.WebAuthnService;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class WebAuthnServiceImplTest {

  private WebAuthnService webAuthnService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.webAuthnService =
        AuthenticationServiceBuilder.buildServices(client).getWebAuthnService();
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
            () -> webAuthnService.signInStart("x", "x", null, new LoginOptions(true, false, null, null)));
    assertNotNull(thrown);
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInFinishForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
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

  private static PasskeyOptions passkeyOptions() {
    return PasskeyOptions.builder()
        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
            .authenticatorAttachment(WebAuthnAuthenticatorAttachment.CROSS_PLATFORM)
            .requireResidentKey(WebAuthnRequireResidentKey.TRUE)
            .residentKey(WebAuthnResidentKey.REQUIRED)
            .userVerification(WebAuthnUserVerification.PREFERRED)
            .extensionsJSON("{}")
            .build())
        .attestation(WebAuthnAttestation.DIRECT)
        .userVerification(WebAuthnUserVerification.REQUIRED)
        .extensionsJSON("{\"a\":1}")
        .build();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> captureRequest(ApiProxy apiProxy, ArgumentCaptor<URI> uriCaptor) {
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), any());
    return new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
        .convertValue(bodyCaptor.getValue(), Map.class);
  }

  @SuppressWarnings("unchecked")
  private static String captureAuthHeader(MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder) {
    ArgumentCaptor<Supplier<String>> headerCaptor = ArgumentCaptor.forClass(Supplier.class);
    mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(headerCaptor.capture(), any()), atLeastOnce());
    List<Supplier<String>> suppliers = headerCaptor.getAllValues();
    return suppliers.get(suppliers.size() - 1).get();
  }

  @SuppressWarnings("unchecked")
  private static void assertPasskeyOptions(Object value) {
    Map<String, Object> options = (Map<String, Object>) value;
    assertEquals("direct", options.get("attestation"));
    assertEquals("required", options.get("userVerification"));
    assertEquals("{\"a\":1}", options.get("extensionsJSON"));
    Map<String, Object> selection = (Map<String, Object>) options.get("authenticatorSelection");
    assertEquals("crossplatform", selection.get("authenticatorAttachment"));
    assertEquals("true", selection.get("requireResidentKey"));
    assertEquals("required", selection.get("residentKey"));
    assertEquals("preferred", selection.get("userVerification"));
    assertEquals("{}", selection.get("extensionsJSON"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSignUpStartWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SignUpOptions signUpOptions = SignUpOptions.builder()
          .customClaims(Collections.singletonMap("k", "v"))
          .templateOptions(Collections.singletonMap("t", "v"))
          .build();
      WebAuthnTransactionResponse res = webAuthnService.signUpStart(MOCK_EMAIL,
          new User(MOCK_NAME, MOCK_EMAIL, MOCK_PHONE), "origin", signUpOptions, passkeyOptions(), "My key");
      assertEquals("t1", res.getTransactionId());
      assertEquals("o", res.getOptions());
      assertTrue(res.isCreate());
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/webauthn/signup/start", uriCaptor.getValue().getPath());
      assertEquals(MOCK_EMAIL, body.get("loginId"));
      assertEquals("origin", body.get("origin"));
      assertEquals(MOCK_NAME, ((Map<String, Object>) body.get("user")).get("name"));
      assertEquals("My key", body.get("displayName"));
      Map<String, Object> loginOptions = (Map<String, Object>) body.get("loginOptions");
      assertEquals("v", ((Map<String, Object>) loginOptions.get("customClaims")).get("k"));
      assertEquals("v", ((Map<String, Object>) loginOptions.get("templateOptions")).get("t"));
      assertPasskeyOptions(body.get("passkeyOptions"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testSignUpStartWithoutOptionsOmitsNewFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      webAuthnService.signUpStart(MOCK_EMAIL, null, "origin");
      Map<String, Object> body = captureRequest(apiProxy, ArgumentCaptor.forClass(URI.class));
      assertEquals(new HashSet<>(Arrays.asList("loginId", "user", "origin")), body.keySet());
    }
  }

  @Test
  void testSignUpStartWithOptionsEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> webAuthnService.signUpStart("", null, "origin", null, passkeyOptions(), "My key"));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testSignInStartWithPasskeyOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", false)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      WebAuthnTransactionResponse res =
          webAuthnService.signInStart(MOCK_EMAIL, "origin", null, null, passkeyOptions());
      assertEquals("t1", res.getTransactionId());
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/webauthn/signin/start", uriCaptor.getValue().getPath());
      assertEquals(MOCK_EMAIL, body.get("loginId"));
      assertEquals("origin", body.get("origin"));
      assertPasskeyOptions(body.get("passkeyOptions"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testSignInStartWithPasskeyOptionsEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> webAuthnService.signInStart("", "origin", null, null, passkeyOptions()));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSignUpOrInStartWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      LoginOptions loginOptions = LoginOptions.builder().locale("en").build();
      WebAuthnTransactionResponse res = webAuthnService.signUpOrInStart(MOCK_EMAIL, "origin", loginOptions, "app1",
          passkeyOptions(), "My key");
      assertEquals("t1", res.getTransactionId());
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/webauthn/signup-in/start", uriCaptor.getValue().getPath());
      assertEquals(MOCK_EMAIL, body.get("loginId"));
      assertEquals("origin", body.get("origin"));
      assertEquals("app1", body.get("ssoAppId"));
      assertEquals("My key", body.get("displayName"));
      assertEquals("en", ((Map<String, Object>) body.get("loginOptions")).get("locale"));
      assertPasskeyOptions(body.get("passkeyOptions"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testSignUpOrInStartWithoutOptionsOmitsNewFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      webAuthnService.signUpOrInStart(MOCK_EMAIL, "origin");
      Map<String, Object> body = captureRequest(apiProxy, ArgumentCaptor.forClass(URI.class));
      assertEquals(new HashSet<>(Arrays.asList("loginId", "origin")), body.keySet());
    }
  }

  @Test
  void testSignUpOrInStartWithOptionsEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> webAuthnService.signUpOrInStart("", "origin", null, "app1", null, null));
    assertEquals("The Login ID argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserDeviceStartWithOptions() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        WebAuthnTransactionResponse res = webAuthnService.updateUserDeviceStart(MOCK_EMAIL, "origin",
            MOCK_REFRESH_TOKEN, passkeyOptions(), true, "My key");
        assertEquals("t1", res.getTransactionId());
      }
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/webauthn/update/start", uriCaptor.getValue().getPath());
      assertEquals(MOCK_EMAIL, body.get("loginId"));
      assertEquals("origin", body.get("origin"));
      assertEquals(true, body.get("mfa"));
      assertEquals("My key", body.get("displayName"));
      assertPasskeyOptions(body.get("passkeyOptions"));
      assertEquals("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testUpdateUserDeviceStartWithoutOptionsOmitsNewFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnTransactionResponse("t1", "o", true)).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        webAuthnService.updateUserDeviceStart(MOCK_EMAIL, "origin", MOCK_REFRESH_TOKEN);
      }
      Map<String, Object> body = captureRequest(apiProxy, ArgumentCaptor.forClass(URI.class));
      assertEquals(new HashSet<>(Arrays.asList("loginId", "origin")), body.keySet());
    }
  }

  @Test
  void testUpdateUserDeviceStartWithOptionsEmptyToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> webAuthnService.updateUserDeviceStart(MOCK_EMAIL, "origin", null, null, true, null));
    assertEquals("The Token argument is invalid", thrown.getMessage());
  }

  private static JWTResponse fullJwtResponse() {
    UserResponse user = new UserResponse();
    user.setUserId("u1");
    user.setLoginIds(Collections.singletonList(MOCK_EMAIL));
    user.setExternalIds(Collections.singletonList("ext1"));
    user.setWebauthn(true);
    user.setPassword(false);
    user.setEditable(true);
    user.setSCIM(true);
    user.setPush(false);
    user.setPermissions(Collections.singletonList("p1"));
    user.setOIDC(true);
    user.setConsentExpiration(10L);
    user.setRecoveryEmail("r@x.com");
    user.setVerifiedRecoveryEmail(true);
    user.setRecoveryPhone("+1555");
    user.setVerifiedRecoveryPhone(false);
    user.setModifiedTime(11L);
    user.setRoleIds(Collections.singletonList("r1"));
    user.setRecoveryCodes(true);
    user.setDependent(false);
    user.setUserFamilies(Collections.singletonList(new UserFamily("f1", null, null, null)));
    user.setScimTenantId("t1");
    user.setSamlTenantId("t2");
    user.setOidcTenantId("t3");
    user.setConsentTenantId("t4");
    user.setLastLoginTime(12L);
    JWTResponse jwt = new JWTResponse(MOCK_JWT_RESPONSE.getSessionJwt(), MOCK_JWT_RESPONSE.getRefreshJwt(), "d", "/",
        1, 2, user, true, null);
    jwt.setSessionExpiration(3);
    jwt.setExternalToken("ext");
    jwt.setClaims(Collections.singletonMap("c", "v"));
    jwt.setTenantSSOID("sso");
    jwt.setTrustedDeviceJwt("tdj");
    jwt.setNextRefreshSeconds(4);
    jwt.setCookieName("cn");
    jwt.setSessionCookieName("scn");
    jwt.setSessionCookieDomain("scd");
    jwt.setUnsavedSSO(true);
    jwt.setKnownDeviceJwt("kdj");
    jwt.setSsoAuthenticationOnly(false);
    jwt.setUserCreationDeferred(true);
    return jwt;
  }

  private static void assertFullAuthenticationInfo(AuthenticationInfo info) {
    assertNotNull(info.getToken());
    assertNotNull(info.getRefreshToken());
    assertEquals(3, info.getSessionExpiration());
    assertEquals("ext", info.getExternalToken());
    assertEquals("v", info.getClaims().get("c"));
    assertEquals("sso", info.getTenantSSOID());
    assertEquals("tdj", info.getTrustedDeviceJwt());
    assertEquals(4, info.getNextRefreshSeconds());
    assertEquals("cn", info.getCookieName());
    assertEquals("scn", info.getSessionCookieName());
    assertEquals("scd", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("kdj", info.getKnownDeviceJwt());
    assertEquals(false, info.getSsoAuthenticationOnly());
    assertEquals(true, info.getUserCreationDeferred());
    UserResponse user = info.getUser();
    assertEquals("ext1", user.getExternalIds().get(0));
    assertEquals(true, user.getWebauthn());
    assertEquals(false, user.getPassword());
    assertEquals(true, user.getEditable());
    assertEquals(true, user.getSCIM());
    assertEquals(false, user.getPush());
    assertEquals("p1", user.getPermissions().get(0));
    assertEquals(true, user.getOIDC());
    assertEquals(10L, user.getConsentExpiration());
    assertEquals("r@x.com", user.getRecoveryEmail());
    assertEquals(true, user.getVerifiedRecoveryEmail());
    assertEquals("+1555", user.getRecoveryPhone());
    assertEquals(false, user.getVerifiedRecoveryPhone());
    assertEquals(11L, user.getModifiedTime());
    assertEquals("r1", user.getRoleIds().get(0));
    assertEquals(true, user.getRecoveryCodes());
    assertEquals(false, user.getDependent());
    assertEquals("f1", user.getUserFamilies().get(0).getFamilyId());
    assertEquals("t1", user.getScimTenantId());
    assertEquals("t2", user.getSamlTenantId());
    assertEquals("t3", user.getOidcTenantId());
    assertEquals("t4", user.getConsentTenantId());
    assertEquals(12L, user.getLastLoginTime());
  }

  @Test
  void testSignUpAndSignInFinishFullResponse() {
    for (boolean signUp : new boolean[] {true, false}) {
      ApiProxy apiProxy = mock(ApiProxy.class);
      doReturn(fullJwtResponse()).when(apiProxy).post(any(), any(), any());
      try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
        mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
        AuthenticationInfo info;
        try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
          mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
          WebAuthnFinishRequest req = new WebAuthnFinishRequest("t1", "r");
          info = signUp ? webAuthnService.signUpFinish(req) : webAuthnService.signInFinish(req);
        }
        assertFullAuthenticationInfo(info);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
        assertEquals(signUp ? "/v1/auth/webauthn/signup/finish" : "/v1/auth/webauthn/signin/finish",
            uriCaptor.getValue().getPath());
        assertEquals("t1", body.get("transactionId"));
        assertEquals("r", body.get("response"));
        assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
      }
    }
  }

  @Test
  void testUpdateUserDeviceFinishWithSession() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnUpdateFinishResponse(fullJwtResponse())).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AuthenticationInfo info;
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = webAuthnService.updateUserDeviceFinishWithSession(new WebAuthnFinishRequest("t1", "r"));
      }
      assertFullAuthenticationInfo(info);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      Map<String, Object> body = captureRequest(apiProxy, uriCaptor);
      assertEquals("/v1/auth/webauthn/update/finish", uriCaptor.getValue().getPath());
      assertEquals("t1", body.get("transactionId"));
      assertEquals("r", body.get("response"));
      assertEquals("Bearer " + PROJECT_ID, captureAuthHeader(mockedApiProxyBuilder));
    }
  }

  @Test
  void testUpdateUserDeviceFinishWithSessionNoJwt() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WebAuthnUpdateFinishResponse()).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertNull(webAuthnService.updateUserDeviceFinishWithSession(new WebAuthnFinishRequest("t1", "r")));
    }
  }

  @Test
  void testUpdateUserDeviceFinishWithSessionEmptyRequest() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> webAuthnService.updateUserDeviceFinishWithSession(new WebAuthnFinishRequest("t1", null)));
    assertEquals("The Finish Request argument is invalid", thrown.getMessage());
  }
}
