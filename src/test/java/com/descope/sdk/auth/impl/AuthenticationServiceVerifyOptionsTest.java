package com.descope.sdk.auth.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationServices;
import com.descope.model.auth.VerifyOptions;
import com.descope.model.client.Client;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link VerifyOptions} overloads added to
 * {@link AuthenticationService}. These focus on input validation and on the fact that the
 * new overloads forward to the same paths as the existing overloads (so passing {@code null}
 * {@link VerifyOptions} is semantically identical to the legacy call).
 *
 * <p>JWT-verification behavior with {@code aud} is exercised by
 * {@link com.descope.utils.JwtUtilsAudienceTest}.
 */
class AuthenticationServiceVerifyOptionsTest {

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    AuthenticationServices services = AuthenticationServiceBuilder.buildServices(client);
    this.authenticationService = services.getAuthService();
  }

  @Test
  void validateSessionWithTokenBlankRejected() {
    assertThrows(
        ServerCommonException.class,
        () -> authenticationService.validateSessionWithToken("", VerifyOptions.withAudience("a")));
    assertThrows(
        ServerCommonException.class,
        () -> authenticationService.validateSessionWithToken(null, (VerifyOptions) null));
  }

  @Test
  void refreshSessionWithTokenBlankRejected() {
    assertThrows(
        ServerCommonException.class,
        () -> authenticationService.refreshSessionWithToken("", VerifyOptions.withAudience("a")));
    assertThrows(
        ServerCommonException.class,
        () -> authenticationService.refreshSessionWithToken(null, (VerifyOptions) null));
  }

  @Test
  void refreshSessionAuthInfoBlankRejected() {
    assertThrows(
        ServerCommonException.class,
        () ->
            authenticationService.refreshSessionWithTokenAuthenticationInfo(
                "", VerifyOptions.withAudience("a")));
  }

  @Test
  void validateAndRefreshBothBlankRejected() {
    assertThrows(
        ServerCommonException.class,
        () ->
            authenticationService.validateAndRefreshSessionWithTokens(
                "", "", VerifyOptions.withAudience("a")));
    assertThrows(
        ServerCommonException.class,
        () ->
            authenticationService.validateAndRefreshSessionWithTokensAuthenticationInfo(
                "", "", VerifyOptions.withAudience("a")));
  }

  @Test
  void exchangeAccessKeyBlankRejected() {
    assertThrows(
        ServerCommonException.class,
        () -> authenticationService.exchangeAccessKey("", null, VerifyOptions.withAudience("a")));
  }
}
