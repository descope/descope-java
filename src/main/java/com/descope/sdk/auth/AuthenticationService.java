package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.jwt.Token;
import com.descope.model.user.response.UserHistoryResponse;
import com.descope.model.user.response.UserResponse;
import java.util.List;
import com.descope.model.auth.AccessKeyLoginOptions;

public interface AuthenticationService {

  /**
   * Use to validate a session token directly. Should be called before any private API call that
   * requires authorization.
   *
   * @param sessionToken - JWT Session Token
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token validateSessionWithToken(String sessionToken) throws DescopeException;

  /**
   * Use to validate a session of a given request. Should be called before any private API call that
   * requires authorization. Use the addCookies to apply the cookies to the httpRequest
   * automatically. Alternatively use ValidateAndRefreshSessionWithTokens with the tokens directly.
   *
   * @param refreshToken - Refresh Token
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token refreshSessionWithToken(String refreshToken) throws DescopeException;

  /**
   * Use to validate a session with the session and refresh tokens. Should be called before any
   * private API call that requires authorization.
   *
   * @param sessionToken - Session Token
   * @param refreshToken - Refresh Token
   * @return {@link Token Token}
   * @throws DescopeException - error upon failure
   */
  Token validateAndRefreshSessionWithTokens(String sessionToken, String refreshToken)
      throws DescopeException;

  /**
   * Use to exchange an access key for a session token.
   *
   * @param accessKey - Access Key
   * @param loginOptions - {@link AccessKeyLoginOptions loginOptions}
   * @return {@link Token Token}
   * @throws DescopeException if there is an error
   */
  Token exchangeAccessKey(String accessKey, AccessKeyLoginOptions loginOptions) throws DescopeException;

  /**
   * Use to ensure that a validated session token has been granted the specified permissions. This
   * is a shortcut for validatePermissions(token, "", permissions)
   *
   * @param token - {@link Token Token}
   * @param permissions - List of permissions.
   * @return is valid permissions.
   * @throws DescopeException if there is an error
   */
  boolean validatePermissions(Token token, List<String> permissions) throws DescopeException;

  /**
   * Use to ensure that a validated session token has been granted the specified permissions for a
   * specific tenant.
   *
   * @param token - {@link Token Token}
   * @param tenant - Tenant ID.
   * @param permissions - List of permissions.
   * @return is valid permissions.
   * @throws DescopeException if there is an error
   */
  boolean validatePermissions(Token token, String tenant, List<String> permissions)
      throws DescopeException;

  /**
   * Use to retrieves the permissions from top level token's claims that match the specified permissions list. This
   * is a shortcut for getMatchedPermissions(token, "", permissions)
   *
   * @param token - {@link Token Token}
   * @param permissions - List of permissions.
   * @return is valid permissions.
   * @throws DescopeException if there is an error
   */
  List<String> getMatchedPermissions(Token token, List<String> permissions) throws DescopeException;

  /**
   * Use to retrieves the permissions from token's claims of a specific tenant
  * that match the specified permissions list.
  * This is a shortcut for getMatchedPermissions(token, "", permissions)
   *
   * @param token - {@link Token Token}
   * @param tenant - Tenant ID.
   * @param permissions - List of permissions.
   * @return is valid permissions.
   * @throws DescopeException if there is an error
   */
  List<String> getMatchedPermissions(Token token, String tenant, List<String> permissions) throws DescopeException;

  /**
   * Use to ensure that a validated session token has been granted the specified roles. This is a
   * shortcut for validateRoles(token, "", roles)
   *
   * @param token - {@link Token Token}
   * @param roles - List of roles.
   * @return is valid roles.
   * @throws DescopeException if there is an error
   */
  boolean validateRoles(Token token, List<String> roles) throws DescopeException;

  /**
   * Use to ensure that a validated session token has been granted the specified roles for a
   * specific tenant.
   *
   * @param token - {@link Token Token}
   * @param tenant - Tenant ID.
   * @param roles - List of roles.
   * @return is valid roles.
   * @throws DescopeException if there is an error
   */
  boolean validateRoles(Token token, String tenant, List<String> roles) throws DescopeException;

  /**
  * Use to retrieves the roles from top level token's claims that match the specified roles list. This
  * is a shortcut for getMatchedRoles(token, "", roles)
  *
  * @param token - {@link Token Token}
  * @param roles - List of roles.
  * @return is valid roles.
  * @throws DescopeException if there is an error
  */
  List<String> getMatchedRoles(Token token, List<String> roles) throws DescopeException;

  /**
  * Use to retrieves the roles from token's claims of a specific tenant
  * that match the specified roles list.
  *
  * @param token - {@link Token Token}
  * @param tenant - Tenant ID.
  * @param roles - List of roles.
  * @return is valid permissions.
  * @throws DescopeException if there is an error
  */
  List<String> getMatchedRoles(Token token, String tenant, List<String> roles) throws DescopeException;

  /**
   * Return the list of roles granted to the validated session token in the given tenant.
   *
   * @param token - {@link Token Token}
   * @param tenant - Tenant ID.
   * @return {@link List} of {@link String} roles the user has in the tenant
   * @throws DescopeException if there is an error
   */
  List<String> getRoles(Token token, String tenant) throws DescopeException;

  /**
   * Return the list of roles granted to the validated session token.
   *
   * @param token - {@link Token Token}
   * @return {@link List} of {@link String} roles the user has globally in the project
   * @throws DescopeException if there is an error
   */
  List<String> getRoles(Token token) throws DescopeException;

  /**
   * Return the list of permissions granted to the validated session token in the given tenant.
   *
   * @param token - {@link Token Token}
   * @param tenant - Tenant ID.
   * @return {@link List} of {@link String} permissions the user has in the tenant
   * @throws DescopeException if there is an error
   */
  List<String> getPermissions(Token token, String tenant) throws DescopeException;

  /**
   * Return the list of permissions granted to the validated session token.
   *
   * @param token - {@link Token Token}
   * @return {@link List} of {@link String} permissions the user has globally in the project
   * @throws DescopeException if there is an error
   */
  List<String> getPermissions(Token token) throws DescopeException;

  /**
   * Return the list of associated tenant IDs in the given token.
   *
   * @param token - {@link Token Token}
   * @return {@link List} of {@link String} tenant IDs that the user is associated with
   * @throws DescopeException if there is an error
   */
  List<String> getTenantIds(Token token) throws DescopeException;

  /**
   * Used to log out of current device session.
   *
   * @param refreshToken - token
   * @throws DescopeException if there is an error
   */
  void logout(String refreshToken) throws DescopeException;

  /**
   * Used to log out of all device sessions.
   *
   * @param refreshToken - token
   * @throws DescopeException if there is an error
   */
  void logoutAll(String refreshToken) throws DescopeException;

  /**
   * Use to retrieve current session user details. The request requires a valid refresh token.
   *
   * @param refreshToken a valid refresh token
   * @return {@link UserResponse} returns the user details.
   * @throws DescopeException if there is an error or token is not valid
   */
  UserResponse me(String refreshToken) throws DescopeException;

  /**
   * Use to retrieve current session user history. The request requires a valid refresh token.
   *
   * @param refreshToken a valid refresh token
   * @return {@link UserHistoryResponse} returns the user authentication history.
   * @throws DescopeException if there is an error or token is not valid
   */
  List<UserHistoryResponse> history(String refreshToken) throws DescopeException;
}
