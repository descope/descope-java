package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.Token;
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
}
