package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.jwt.MgmtSignInOptions;
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

  /**
   * Sign up a new user and generate a JWT for them, without going through an authentication method.
   *
   * @param loginId           - The login ID of the user (required)
   * @param signUpUserDetails - Optional user details and JWT options. In {@code userTenants}, each tenant
   *                          needs a {@code tenantId}; only {@code tenantId} and {@code roleNames} are sent.
   * @return - The session and refresh tokens, the user and the rest of the JWT response
   */
  AuthenticationInfo signUp(String loginId, MgmtSignUpUser signUpUserDetails)
          throws DescopeException;

  /**
   * Sign up a new user, or sign in an existing one, and generate a JWT for them.
   *
   * @param loginId           - The login ID of the user (required)
   * @param signUpUserDetails - Optional user details and JWT options, as in {@link #signUp}
   * @return - The session and refresh tokens, the user and the rest of the JWT response
   */
  AuthenticationInfo signUpOrIn(String loginId, MgmtSignUpUser signUpUserDetails)
          throws DescopeException;

  AuthenticationInfo signIn(String loginId, LoginOptions loginOptions) throws DescopeException;

  /**
   * Sign in an existing user and generate a JWT for them, without going through an authentication method.
   *
   * @param loginId           - The login ID of the user (required)
   * @param loginOptions      - Optional step-up, MFA, custom claims and session revocation options
   * @param signInOptions     - Optional SSO app, refresh duration, selected tenant and enforce SSO options
   * @return - The session and refresh tokens, the user and the rest of the JWT response
   */
  AuthenticationInfo signIn(String loginId, LoginOptions loginOptions, MgmtSignInOptions signInOptions)
      throws DescopeException;

  /**
   * Generate a JWT for an anonymous user. Each of the request's {@code roles} must be non-empty.
   *
   * @param request - Optional custom claims, selected tenant, refresh duration and roles
   * @return - The session and refresh tokens
   */
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

  /**
   * Impersonate another user. The impersonator user must have the requested permission
   * otherwise the operation will fail. A new JWT for the impersonated user will be returned.
   *
   * @param impersonatorId  - The ID of the impersonator (a valid login ID or user ID)
   * @param loginId         - The login ID of the user to impersonate
   * @param validateConsent - Whether to validate that consent to impersonation was given
   * @param customClaims    - Custom claims to add to the impersonated JWT (optional)
   * @param tenantId        - The tenant to select for the impersonated user (optional)
   * @return - New JWT for the impersonated user
   */
  String impersonate(String impersonatorId, String loginId, boolean validateConsent,
      Map<String, Object> customClaims, String tenantId) throws DescopeException;

  /**
   * Impersonate another user with a step-up JWT. The impersonator user must have the requested
   * permission otherwise the operation will fail. A new JWT for the impersonated user will be
   * returned.
   *
   * @param impersonatorId  - The ID of the impersonator (a valid login ID or user ID)
   * @param loginId         - The login ID of the user to impersonate
   * @param validateConsent - Whether to validate that consent to impersonation was given
   * @param customClaims    - Custom claims to add to the impersonated JWT (optional)
   * @param tenantId        - The tenant to select for the impersonated user (optional)
   * @return - New step-up JWT for the impersonated user
   */
  String impersonateStepup(String impersonatorId, String loginId, boolean validateConsent,
      Map<String, Object> customClaims, String tenantId) throws DescopeException;

  /**
   * Stop an active impersonation given the impersonated user's JWT. A new JWT for the original
   * (impersonator) user will be returned.
   *
   * @param jwt          - The impersonated user's JWT
   * @param customClaims - Custom claims to add to the returned JWT (optional)
   * @param tenantId     - The tenant to select for the returned JWT (optional)
   * @return - New JWT for the original impersonator user
   */
  String stopImpersonation(String jwt, Map<String, Object> customClaims, String tenantId)
      throws DescopeException;
}
