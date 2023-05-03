package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.jwt.Token;
import java.net.http.HttpRequest;

public interface AuthenticationService {
  /**
   * Use to validate a session of a given request. Should be called before any private API call that
   * requires authorization. Alternatively use ValidateSessionWithToken with the token directly.
   *
   * @param httpRequest - {@link HttpRequest HttpRequest}
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token validateSessionWithRequest(HttpRequest httpRequest) throws DescopeException;

  /**
   * Use to validate a session token directly. Should be called before any private API call that
   * requires authorization. Alternatively use ValidateSessionWithRequest with the incoming request.
   *
   * @param sessionToken - JWT Session Token
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token validateSessionWithToken(String sessionToken) throws DescopeException;

  /**
   * Use to refresh an expired session of a given request. Should be called when a session has
   * expired (failed validation) to renew it. Use the addCookies to apply the cookies to the
   * httpRequest automatically. Alternatively use RefreshSessionWithToken with the refresh token
   * directly.
   *
   * @param httpRequest - {@link HttpRequest HttpRequest}
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token refreshSessionWithRequest(HttpRequest httpRequest, boolean addCookies)
      throws DescopeException;

  /**
   * Use to validate a session of a given request. Should be called before any private API call that
   * requires authorization. Use the addCookies to apply the cookies to the httpRequest
   * automatically. Alternatively use ValidateAndRefreshSessionWithTokens with the tokens directly.
   *
   * @param refreshToken - Refresh Token
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token refreshSessionWithToken(String refreshToken, boolean addCookies) throws DescopeException;

  /**
   * Use to validate a session with the session and refresh tokens. Should be called before any
   * private API call that requires authorization. Alternatively use
   * ValidateAndRefreshSessionWithRequest with the incoming request.
   *
   * @param sessionToken - Session Token
   * @param refreshToken - Refresh Token
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token validateAndRefreshSessionWithTokens(String sessionToken, String refreshToken)
      throws DescopeException;
}
