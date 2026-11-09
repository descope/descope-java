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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.otp.AuthenticationVerifyRequestBody;
import com.descope.model.totp.TOTPResponse;
import com.descope.model.totp.TotpSignUpRequestBody;
import com.descope.model.user.User;
import com.descope.model.user.response.UserFamily;
import com.descope.model.user.response.UserResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.TOTPService;
import com.descope.sdk.mgmt.UserService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.descope.utils.AuthUtils;
import com.descope.utils.JwtUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
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
  private Client client;

  @BeforeEach
  void setUp() {
    this.client = TestUtils.getClient();
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
    TOTPResponse expected = new TOTPResponse("otpauth://totp/x", "someImage", "someKey");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(expected).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      TOTPResponse response = totpService.signUp(MOCK_EMAIL, user, "someSsoAppId");
      assertEquals(expected, response);

      ArgumentCaptor<Supplier<String>> auth = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(auth.capture(), any()));
      assertEquals(AuthUtils.getBearerHeader(PROJECT_ID, null, client.getAuthManagementKey()), auth.getValue().get());
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<TotpSignUpRequestBody> body = ArgumentCaptor.forClass(TotpSignUpRequestBody.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(TOTPResponse.class));
    assertEquals("/v1/auth/totp/signup", uri.getValue().getPath());
    assertEquals(MOCK_EMAIL, body.getValue().getLoginId());
    assertEquals(user, body.getValue().getUser());
    assertEquals("someSsoAppId", body.getValue().getSsoAppId());
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    assertThat(mapper.writeValueAsString(body.getValue())).contains("\"ssoAppId\":\"someSsoAppId\"");
  }

  @Test
  void signUpOmitsSsoAppIdWhenUnset() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new TOTPResponse()).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      totpService.signUp(MOCK_EMAIL, MOCK_USER);
    }
    ArgumentCaptor<TotpSignUpRequestBody> body = ArgumentCaptor.forClass(TotpSignUpRequestBody.class);
    verify(apiProxy).post(any(), body.capture(), any());
    assertNull(body.getValue().getSsoAppId());
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    assertThat(mapper.writeValueAsString(body.getValue())).doesNotContain("ssoAppId");
  }

  @Test
  void signUpWithSsoAppIdForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> totpService.signUp("", MOCK_USER, "someSsoAppId"));
    assertEquals("The loginId argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void signInCodeReturnsAllResponseFields() throws Exception {
    String json = "{\"sessionJwt\":\"someSessionJwt\",\"refreshJwt\":\"someRefreshJwt\",\"firstSeen\":true,"
        + "\"sessionExpiration\":100,\"externalToken\":\"ext\",\"claims\":{\"k\":\"v\"},\"tenantSSOID\":\"t1\","
        + "\"trustedDeviceJwt\":\"tdj\",\"nextRefreshSeconds\":60,\"cookieName\":\"DSR\","
        + "\"sessionCookieName\":\"DS\",\"sessionCookieDomain\":\"example.com\",\"unsavedSSO\":true,"
        + "\"knownDeviceJwt\":\"kdj\",\"ssoAuthenticationOnly\":true,\"userCreationDeferred\":true,"
        + "\"unknownField\":1,"
        + "\"user\":{\"userId\":\"u1\",\"loginIds\":[\"l1\"],\"externalIds\":[\"e1\"],\"webauthn\":true,"
        + "\"password\":true,\"editable\":true,\"SCIM\":true,\"push\":true,\"permissions\":[\"p1\"],\"OIDC\":true,"
        + "\"consentExpiration\":5,\"recoveryEmail\":\"r@example.com\",\"verifiedRecoveryEmail\":true,"
        + "\"recoveryPhone\":\"+15555555555\",\"verifiedRecoveryPhone\":true,\"modifiedTime\":6,"
        + "\"roleIds\":[\"r1\"],\"recoveryCodes\":true,\"dependent\":true,"
        + "\"userFamilies\":[{\"familyId\":\"f1\",\"roleNames\":[\"fr\"],\"permissions\":[\"fp\"],"
        + "\"familyScopedAttributes\":{\"a\":\"b\"}}],\"scimTenantId\":\"st\",\"samlTenantId\":\"sa\","
        + "\"oidcTenantId\":\"ot\",\"consentTenantId\":\"ct\",\"lastLoginTime\":7}}";
    JWTResponse jwtResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, JWTResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(jwtResponse).when(apiProxy).post(any(), any(), any());
    LoginOptions loginOptions = new LoginOptions();

    AuthenticationInfo info;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN);
        info = totpService.signInCode(MOCK_EMAIL, "123456", loginOptions);
      }
      ArgumentCaptor<Supplier<String>> auth = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(auth.capture(), any()));
      assertEquals(AuthUtils.getBearerHeader(PROJECT_ID, null, client.getAuthManagementKey()), auth.getValue().get());
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<AuthenticationVerifyRequestBody> body =
        ArgumentCaptor.forClass(AuthenticationVerifyRequestBody.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JWTResponse.class));
    assertEquals("/v1/auth/totp/verify", uri.getValue().getPath());
    assertEquals(MOCK_EMAIL, body.getValue().getLoginId());
    assertEquals("123456", body.getValue().getCode());
    assertEquals(loginOptions, body.getValue().getLoginOptions());

    assertEquals(MOCK_TOKEN, info.getToken());
    assertEquals(true, info.getFirstSeen());
    assertEquals(Integer.valueOf(100), info.getSessionExpiration());
    assertEquals("ext", info.getExternalToken());
    assertEquals("v", info.getClaims().get("k"));
    assertEquals("t1", info.getTenantSSOID());
    assertEquals("tdj", info.getTrustedDeviceJwt());
    assertEquals(Integer.valueOf(60), info.getNextRefreshSeconds());
    assertEquals("DSR", info.getCookieName());
    assertEquals("DS", info.getSessionCookieName());
    assertEquals("example.com", info.getSessionCookieDomain());
    assertEquals(true, info.getUnsavedSSO());
    assertEquals("kdj", info.getKnownDeviceJwt());
    assertEquals(true, info.getSsoAuthenticationOnly());
    assertEquals(true, info.getUserCreationDeferred());

    UserResponse user = info.getUser();
    assertEquals("u1", user.getUserId());
    assertEquals(Arrays.asList("e1"), user.getExternalIds());
    assertEquals(true, user.getWebauthn());
    assertEquals(true, user.getPassword());
    assertEquals(true, user.getEditable());
    assertEquals(true, user.getSCIM());
    assertEquals(true, user.getPush());
    assertEquals(Arrays.asList("p1"), user.getPermissions());
    assertEquals(true, user.getOIDC());
    assertEquals(Long.valueOf(5), user.getConsentExpiration());
    assertEquals("r@example.com", user.getRecoveryEmail());
    assertEquals(true, user.getVerifiedRecoveryEmail());
    assertEquals("+15555555555", user.getRecoveryPhone());
    assertEquals(true, user.getVerifiedRecoveryPhone());
    assertEquals(Long.valueOf(6), user.getModifiedTime());
    assertEquals(Arrays.asList("r1"), user.getRoleIds());
    assertEquals(true, user.getRecoveryCodes());
    assertEquals(true, user.getDependent());
    UserFamily family = user.getUserFamilies().get(0);
    assertEquals("f1", family.getFamilyId());
    assertEquals(Arrays.asList("fr"), family.getRoleNames());
    assertEquals(Arrays.asList("fp"), family.getPermissions());
    assertEquals("b", family.getFamilyScopedAttributes().get("a"));
    assertEquals("st", user.getScimTenantId());
    assertEquals("sa", user.getSamlTenantId());
    assertEquals("ot", user.getOidcTenantId());
    assertEquals("ct", user.getConsentTenantId());
    assertEquals(Long.valueOf(7), user.getLastLoginTime());
  }

  @Test
  void signInCodeForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> totpService.signInCode("", "123456", null));
    assertEquals("The loginId argument is invalid", thrown.getMessage());
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
