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
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.Token;
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
import java.security.Key;
import java.time.Instant;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class TotpServiceImplTest {

  private TOTPService totpService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getAuthParams();
    var client = TestUtils.getClient();
    this.totpService =
        AuthenticationServiceBuilder.buildServices(client, authParams).getTotpService();
    var mgmtParams = TestUtils.getManagementParams();
    this.userService = ManagementServiceBuilder.buildServices(client, mgmtParams).getUserService();
  }

  @Test
  void signUp() {
    User user = new User("someUserName", MOCK_EMAIL, "+910000000000");

    var apiProxy = mock(ApiProxy.class);
    doReturn(mock(TOTPResponse.class)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = totpService.signUp(MOCK_EMAIL, user);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void signInCode() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any());
    doReturn(new SigningKeysResponse(List.of(MOCK_SIGNING_KEY)))
      .when(apiProxy).get(any(), eq(SigningKeysResponse.class));

    var provider = mock(Provider.class);
    when(provider.getProvidedKey()).thenReturn(mock(Key.class));

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
  void testUpdateUserForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> totpService.updateUser(null));

    assertNotNull(thrown);
    assertEquals("The loginId argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateUserForSuccess() {
    var apiProxy = mock(ApiProxy.class);
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
    var response = totpService.signUp(loginId, MOCK_USER);
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
