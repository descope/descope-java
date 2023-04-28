package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.jwt.Token;

import java.net.http.HttpRequest;

public interface AuthenticationService {
  /**
   * Use to validate a session of a given request.
   * Should be called before any private API call that requires authorization.
   * Alternatively use ValidateSessionWithToken with the token directly.
   *
   * @param httpRequest - {@link HttpRequest HttpRequest}
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token validateSessionWithRequest(HttpRequest httpRequest) throws DescopeException;

}
