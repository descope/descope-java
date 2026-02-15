package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.request.ClientAssertionRequest;
import com.descope.model.magiclink.LoginOptions;
import java.util.Map;

/** Provide functions for manipulating valid JWT. */
public interface JwtService {

  /**
   * Update a valid JWT with the custom claims provided. The new JWT will be
   * returned.
   *
   * @param jwt          - Old JWT
   * @param customClaims - Custom Claims
   * @return - New JWT
   */
  Token updateJWTWithCustomClaims(String jwt, Map<String, Object> customClaims)
      throws DescopeException;

  AuthenticationInfo signUp(String loginId, MgmtSignUpUser signUpUserDetails)
          throws DescopeException;

  AuthenticationInfo signUpOrIn(String loginId, MgmtSignUpUser signUpUserDetails)
          throws DescopeException;

  AuthenticationInfo signIn(String loginId, LoginOptions loginOptions) throws DescopeException;

  AuthenticationInfo anonymous(AnonymousUserRequest request) throws DescopeException;

  /**
   * Create an OAuth 2.0 client assertion JWT for client authentication.
   * This JWT can be used with OAuth token endpoints that support RFC 7523.
   *
   * @param request - ClientAssertionRequest containing clientId, tokenEndpoint, privateKey, and signing algorithm
   * @return - The signed JWT string that can be used as client_assertion parameter
   * @throws DescopeException if JWT creation fails
   */
  String createClientAssertion(ClientAssertionRequest request) throws DescopeException;
}
