package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.response.ClientAssertionResponse;
import com.descope.model.magiclink.LoginOptions;
import java.util.List;
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
   * Generate a client assertion JWT for OAuth 2.0 client credentials flow.
   * This JWT can be used as a client authentication method when requesting access tokens.
   *
   * @param issuer          - The issuer of the JWT (typically the client ID)
   * @param subject         - The subject of the JWT (typically the client ID)
   * @param audience        - The audience of the JWT (typically the authorization server)
   * @param expiresIn       - Expiration time in seconds
   * @param flattenAudience - Whether to flatten the audience array to a single string (optional)
   * @param algorithm       - The signing algorithm to use: RS256, RS384, or ES384 (optional)
   * @return ClientAssertionResponse containing the generated JWT
   * @throws DescopeException if validation fails or the request cannot be completed
   */
  ClientAssertionResponse generateClientAssertionJwt(String issuer, String subject,
      List<String> audience, Integer expiresIn, Boolean flattenAudience, String algorithm)
      throws DescopeException;
}
