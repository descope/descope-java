package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_JWT_RESPONSE;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static com.descope.sdk.TestUtils.MOCK_TOKEN;
import static com.descope.sdk.TestUtils.MOCK_USER;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.totp.TOTPResponse;
import com.descope.model.user.User;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.TOTPService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class TotpServiceImplTest {

  private TOTPService totpService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.totpService =
        AuthenticationServiceBuilder.buildServices(client).getTotpService();
    this.userService = ManagementServiceBuilder.buildServices(client).getUserService();
  }

  @Test
  void signUp() {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(TOTPResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      TOTPResponse response = totpService.signUp(MOCK_EMAIL, user);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void signInCode() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    AuthenticationInfo authenticationInfo;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        authenticationInfo = totpService.signInCode(MOCK_EMAIL, "123456", new LoginOptions());
      }
    }

    Assertions.assertThat(authenticationInfo).isNotNull();

    Token sessionToken = authenticationInfo.getToken();
    Assertions.assertThat(sessionToken).isNotNull();
    Assertions.assertThat(sessionToken.getJwt()).isNotBlank();
    Assertions.assertThat(sessionToken.getClaims()).isNotEmpty();
    Assertions.assertThat(sessionToken.getProjectId()).isEqualTo(PROJECT_ID);

    Token refreshToken = authenticationInfo.getRefreshToken();
    Assertions.assertThat(refreshToken).isNotNull();
    Assertions.assertThat(refreshToken.getJwt()).isNotBlank();
    Assertions.assertThat(refreshToken.getClaims()).isNotEmpty();
    Assertions.assertThat(refreshToken.getProjectId()).isEqualTo(PROJECT_ID);

    UserResponse user = authenticationInfo.getUser();
    Assertions.assertThat(user).isNotNull();
    Assertions.assertThat(user.getUserId()).isNotBlank();
    Assertions.assertThat(user.getLoginIds()).isNotEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void signUpWithSsoAppId() throws Exception {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");
    ApiProxy apiProxy = mock(ApiProxy.class);
    TOTPResponse expected = new TOTPResponse("otpauth://totp/x", "someImage", "someKey");
    doReturn(expected).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    TOTPResponse response;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      response = totpService.signUp(MOCK_EMAIL, user, "someSsoAppId");
    }
    Assertions.assertThat(response).isEqualTo(expected);
    Assertions.assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(TOTPResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/totp/signup");
    Map<String, Object> json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
        .convertValue(body.getValue(), Map.class);
    Assertions.assertThat(json).containsEntry("loginId", MOCK_EMAIL).containsEntry("ssoAppId", "someSsoAppId");
    Assertions.assertThat((Map<String, Object>) json.get("user")).containsEntry("name", "someUserName");
  }

  @Test
  @SuppressWarnings("unchecked")
  void signUpWithoutSsoAppIdOmitsIt() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(TOTPResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      totpService.signUp(MOCK_EMAIL, null);
      totpService.signUp(MOCK_EMAIL, null, " ");
    }
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(2)).post(any(), body.capture(), eq(TOTPResponse.class));
    for (Object b : body.getAllValues()) {
      Map<String, Object> json = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
          .convertValue(b, Map.class);
      Assertions.assertThat(json).containsOnlyKeys("loginId");
    }
  }

  @Test
  void signUpWithSsoAppIdForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> totpService.signUp("", null, "someSsoAppId"));
    assertEquals("The loginId argument is invalid", thrown.getMessage());
  }

  @Test
  void signInCodeReturnsAllResponseFields() throws Exception {
    String responseJson = "{\"sessionJwt\":\"someSessionJwt\",\"refreshJwt\":\"someRefreshJwt\","
        + "\"sessionExpiration\":1234,\"externalToken\":\"someExternalToken\",\"claims\":{\"a\":\"b\"},"
        + "\"tenantSSOID\":\"someTenantSsoId\",\"trustedDeviceJwt\":\"someTrustedDeviceJwt\","
        + "\"nextRefreshSeconds\":60,\"cookieName\":\"DSR\",\"sessionCookieName\":\"DS\","
        + "\"sessionCookieDomain\":\"example.com\",\"unsavedSSO\":true,\"knownDeviceJwt\":\"someKnownDeviceJwt\","
        + "\"userCreationDeferred\":true,\"externalPasskey\":true,\"unknownField\":1}";
    JWTResponse jwtResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(responseJson, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = totpService.signInCode(MOCK_EMAIL, "123456", null);
      }
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), any(), eq(JWTResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/totp/verify");
    Assertions.assertThat(info.getSessionExpiration()).isEqualTo(1234);
    Assertions.assertThat(info.getExternalToken()).isEqualTo("someExternalToken");
    Assertions.assertThat(info.getClaims()).containsEntry("a", "b");
    Assertions.assertThat(info.getTenantSSOID()).isEqualTo("someTenantSsoId");
    Assertions.assertThat(info.getTrustedDeviceJwt()).isEqualTo("someTrustedDeviceJwt");
    Assertions.assertThat(info.getNextRefreshSeconds()).isEqualTo(60);
    Assertions.assertThat(info.getCookieName()).isEqualTo("DSR");
    Assertions.assertThat(info.getSessionCookieName()).isEqualTo("DS");
    Assertions.assertThat(info.getSessionCookieDomain()).isEqualTo("example.com");
    Assertions.assertThat(info.getUnsavedSSO()).isTrue();
    Assertions.assertThat(info.getKnownDeviceJwt()).isEqualTo("someKnownDeviceJwt");
    Assertions.assertThat(info.getUserCreationDeferred()).isTrue();
    Assertions.assertThat(info.getExternalPasskey()).isTrue();
  }

  @Test
  void testUpdateUserForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> totpService.updateUser(null));

    assertNotNull(thrown);
    assertEquals("The loginId argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mock(TOTPResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      TOTPResponse updateUserEmail = totpService.updateUser(MOCK_EMAIL);
      assertNotNull(updateUserEmail);
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() throws Exception {
    String loginId = TestUtils.getRandomName("u-");
    TOTPResponse response = totpService.signUp(loginId, MOCK_USER);
    assertNotNull(response);
    assertThat(response.getKey()).isNotBlank();
    assertThat(response.getImage()).isNotBlank();
    assertThat(response.getProvisioningURL()).isNotBlank();
    String code = generate(response.getKey(), Math.floorDiv(Instant.now().getEpochSecond(), 30));
    assertNotNull(totpService.signInCode(loginId, code, null));
    userService.delete(loginId);
  }

  private String generate(String key, long counter) throws Exception {
    byte[] hash = generateHash(key, counter);
    return getDigitsFromHash(hash);
  }

  private byte[] generateHash(String key, long counter) throws Exception {
    byte[] data = new byte[8];
    long value = counter;
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }
    // Create a HMAC-SHA1 signing key from the shared key
    Base32 base32 = new Base32();
    byte[] decodedKey = base32.decode(key);
    SecretKeySpec signKey = new SecretKeySpec(decodedKey, "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(signKey);

    // Create a hash of the counter value
    return mac.doFinal(data);
  }

  private String getDigitsFromHash(byte[] hash) {
    int digits = 6;
    int offset = hash[hash.length - 1] & 0xF;
    long truncatedHash = 0;
    for (int i = 0; i < 4; ++i) {
      truncatedHash <<= 8;
      truncatedHash |= (hash[offset + i] & 0xFF);
    }
    truncatedHash &= 0x7FFFFFFF;
    truncatedHash %= Math.pow(10, digits);
    // Left pad with 0s for a n-digit code
    return String.format("%0" + digits + "d", truncatedHash);
  }
}
