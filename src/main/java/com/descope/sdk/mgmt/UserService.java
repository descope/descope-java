package com.descope.sdk.mgmt;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AssociatedFamily;
import com.descope.model.auth.InviteOptions;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.User;
import com.descope.model.user.request.BatchUserRequest;
import com.descope.model.user.request.PatchUserBatchRequest;
import com.descope.model.user.request.PatchUserRequest;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.AllUsersResponseDetails;
import com.descope.model.user.response.EnchantedLinkTestUserResponse;
import com.descope.model.user.response.ImportUsersResponse;
import com.descope.model.user.response.LogoutSessionRange;
import com.descope.model.user.response.MagicLinkTestUserResponse;
import com.descope.model.user.response.OTPTestUserResponse;
import com.descope.model.user.response.ProviderTokenResponse;
import com.descope.model.user.response.TrustedDevice;
import com.descope.model.user.response.UserHistoryResponse;
import com.descope.model.user.response.UserPasskey;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.model.user.response.UsersBatchResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Provides functions for managing users in a project. */
public interface UserService {

  /**
   * Create a new user. The roles parameter is an optional list of the user's roles for users that
   * aren't associated with a tenant, while the tenants parameter can be used to specify which
   * tenants to associate the user with and what roles the user has in each one.
   *
   * @param loginId The loginID is required and will determine what the user will use to sign in.
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails create(String loginId, UserRequest request) throws DescopeException;

  /**
   * Create users in batch.
   *
   * @param users the {@link List} of users
   * @return {@link UsersBatchResponse} with successfully created users and failed users
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UsersBatchResponse createBatch(List<BatchUserRequest> users) throws DescopeException;

  /**
   * Create users in batch.
   *
   * @param users the {@link List} of users
   * @param strict Optional (nullable). When true, a user that sets custom attributes the project does not
   *     declare fails, and the other users are saved. When false or null, those attributes are ignored.
   * @return {@link UsersBatchResponse} with successfully created users and failed users
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UsersBatchResponse createBatch(List<BatchUserRequest> users, Boolean strict) throws DescopeException;

  /**
   * Create a new test user. You can later generate OTP, Magic link and enchanted link to use in the
   * test without the need of 3rd party messaging services. Those users are not counted as part of
   * the monthly active users
   *
   * @param loginId The loginID is required and will determine what the user will use to sign in,
   *     make sure the login id is unique for test.
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails createTestUser(String loginId, UserRequest request) throws DescopeException;

  /**
   * Create a new user and invite them via an email message. Functions exactly the same as the
   * Create function with the additional invitation behavior. See the documentation above for the
   * general creation behavior.
   *
   * <p>IMPORTANT: Since the invitation is sent by email, make sure either the email is explicitly
   * set, or the loginID itself is an email address. You must configure the invitation URL in the
   * Descope console prior to calling the method.
   *
   * @param loginId The loginID is required and will determine what the user will use to sign in,
   *     make sure the login id is unique for test.
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @param options Additional options for the invitation, such as invite URL.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails invite(String loginId, UserRequest request, InviteOptions options) throws DescopeException;

  /**
   * Create users in batch and invite them via an email / text message.
   *
   * @param users the {@link List} of users
   * @param options Additional options for the invitation, such as invite URL.
   * @return {@link UsersBatchResponse} with successfully created users and failed users
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UsersBatchResponse inviteBatch(List<BatchUserRequest> users, InviteOptions options) throws DescopeException;

  /**
   * Create users in batch and invite them via an email / text message.
   *
   * @param users the {@link List} of users
   * @param options Additional options for the invitation, such as invite URL.
   * @param strict Optional (nullable). When true, a user that sets custom attributes the project does not
   *     declare fails, and the other users are saved. When false or null, those attributes are ignored.
   * @return {@link UsersBatchResponse} with successfully created users and failed users
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UsersBatchResponse inviteBatch(List<BatchUserRequest> users, InviteOptions options, Boolean strict)
      throws DescopeException;

  /**
   * Update an existing user.
   *
   * <p>IMPORTANT: All parameters will override whatever values are currently set in the existing
   * user. Use carefully.
   * Instead, use Patch if you don't want to pass all parameters.
   *
   * @param loginIdOrUserId The loginID or userID is required and will determine what the user will use to sign in,
   *     make sure the login id is unique for test.
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails update(String loginIdOrUserId, UserRequest request) throws DescopeException;

  /**
   * Patches an existing user.
   * 
   * <p>Only the fields that are set in the request will be updated.
   *
   * @param loginIdOrUserId The loginID or userID is required and will determine which user to update.
   * @param request The request containing the fields to be updated. Fields not set will remain unchanged.
   * @return {@link UserResponseDetails UserResponseDetails} containing the updated user details.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UserResponseDetails patch(String loginIdOrUserId, PatchUserRequest request) throws DescopeException;

  /**
   * Patches existing users in batch. Only the fields that are set on each user are updated.
   *
   * @param users the {@link List} of users to patch, each identified by its login ID or user ID.
   * @return {@link UsersBatchResponse} with the patched users and failed users
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UsersBatchResponse patchBatch(List<PatchUserBatchRequest> users) throws DescopeException;

  /**
   * Patches existing users in batch. Only the fields that are set on each user are updated.
   *
   * @param users the {@link List} of users to patch, each identified by its login ID or user ID.
   * @param strict Optional (nullable). When true, a user that sets custom attributes the project does not
   *     declare fails, and the other users are saved. When false or null, those attributes are ignored.
   * @return {@link UsersBatchResponse} with the patched users and failed users
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UsersBatchResponse patchBatch(List<PatchUserBatchRequest> users, Boolean strict) throws DescopeException;

  /**
   * Logout user from all devices.
   *
   *
   * @param loginId The loginID is required.
   * @param sessionTypes Optional list of session types that should be expired, leaving empty, will expire all sessions
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   *                          
   */
  void logoutUser(String loginId, String... sessionTypes) throws DescopeException;

  /**
   * Logout user from all devices by user ID.
   *
   *
   * @param userId The userID is required.
   * @param sessionTypes Optional list of session types that should be expired, leaving empty, will expire all sessions
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   *                          
   */
  void logoutUserByUserId(String userId, String... sessionTypes) throws DescopeException;

  /**
   * Logout multiple users from all their devices.
   *
   * @param loginIdsOrUserIds The users' login IDs or user IDs, at least one and at most 100.
   * @param sessionTypes Optional list of session types to log out, null or empty for all session types
   * @return The identifiers that matched no user, and were therefore not logged out.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<String> logoutUsers(List<String> loginIdsOrUserIds, List<String> sessionTypes) throws DescopeException;

  /**
   * Logout a single session of a user, leaving the user's other sessions working.
   *
   * @param loginId The loginID is required.
   * @param sessionId The session to log out is required: the fti claim of the session's session JWT or
   *     refresh JWT, or the refresh JWT's jti when it has no fti.
   * @return true when the session was already logged out before this call
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  boolean logoutUserSession(String loginId, String sessionId) throws DescopeException;

  /**
   * Logout a single session of a user by user ID, leaving the user's other sessions working.
   *
   * @param userId The userID is required.
   * @param sessionId The session to log out is required: the fti claim of the session's session JWT or
   *     refresh JWT, or the refresh JWT's jti when it has no fti.
   * @return true when the session was already logged out before this call
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  boolean logoutUserSessionByUserId(String userId, String sessionId) throws DescopeException;

  /**
   * Logs out sessions created in the given time range (optionally only the given session types).
   * Each session is logged out on its next refresh or validation. before defaults to the server's
   * current time when null; prefer passing null over Instant.now(), since a before in the future is
   * rejected. The range stays active for a year. Deleting it stops it from logging out matching
   * sessions it hasn't logged out yet; sessions already logged out stay logged out.
   *
   * @param before Optional (nullable), sessions created before this time are logged out. Null for the
   *     server's current time. At least one of before and after is required.
   * @param after Optional (nullable), sessions created after this time are logged out. Null for an
   *     open-ended range.
   * @param sessionTypes Optional list of session types to log out, null or empty for all session types
   * @return {@link LogoutSessionRange LogoutSessionRange} the range as stored by the server, which may
   *     store a later after than requested
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  LogoutSessionRange logoutSessionsCreatedBetween(Instant before, Instant after, List<String> sessionTypes)
      throws DescopeException;

  /**
   * List the active logout session ranges.
   *
   * @return The list of {@link LogoutSessionRange LogoutSessionRange}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<LogoutSessionRange> listLogoutSessionRanges() throws DescopeException;

  /**
   * Deletes a logout sessions range. It stops logging out matching sessions it hasn't logged out yet;
   * sessions already logged out stay logged out.
   *
   * @param id The range ID is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteLogoutSessionRange(String id) throws DescopeException;


  /**
   * Delete an existing user.
   *
   * <p>IMPORTANT: This action is irreversible. Use carefully.
   *
   * @param loginId The loginID is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void delete(String loginId) throws DescopeException;

  /**
   * Delete an existing user by user ID.
   *
   * <p>IMPORTANT: This action is irreversible. Use carefully.
   *
   * @param userId The userID is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteByUserId(String userId) throws DescopeException;

  /**
   * Delete all test users in the project.
   *
   * <p>IMPORTANT: This action is irreversible. Use carefully.
   *
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void deleteAllTestUsers() throws DescopeException;

  /**
   * Load an existing user.
   *
   * @param loginId The loginID is required and the user will be fetched according to it.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails load(String loginId) throws DescopeException;

  /**
   * Load an existing user by User ID. The user ID can be found on the user's JWT.
   *
   * @param userId The userID is required and the user will be fetched according to it.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails loadByUserId(String userId) throws DescopeException;

  /**
   * Search all users according to given filters.
   *
   * @param request The options optional parameter allows to fine-tune the search filters and
   *     results. Using nil will result in a filter-less query with a set amount of results.
   * @return {@link AllUsersResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  AllUsersResponseDetails searchAll(UserSearchRequest request) throws DescopeException;

  /**
   * Activate an existing user.
   *
   * @param loginId The loginID is required.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails activate(String loginId) throws DescopeException;

  /**
   * Deactivate an existing user.
   *
   * @param loginId The loginID is required.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails deactivate(String loginId) throws DescopeException;

  /**
   * Update the email address for an existing user.
   *
   * @param loginId The loginID is required.
   * @param email The email parameter can be empty in which case the email will be removed.
   * @param isVerified The isVerified flag must be true for the user to be able to login with the
   *     email address.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateEmail(String loginId, String email, Boolean isVerified)
      throws DescopeException;

  /**
   * Update the email address for an existing user.
   *
   * @param loginId The loginID is required.
   * @param email The email parameter can be empty in which case the email will be removed.
   * @param isVerified The isVerified flag must be true for the user to be able to login with the
   *     email address.
   * @param failOnConflict If true, the call will fail with an error instead of silently deleting
   *     and merging another user that already has this email address.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateEmail(String loginId, String email, Boolean isVerified, Boolean failOnConflict)
      throws DescopeException;

  /**
   * Update the email address for an existing user.
   *
   * @param loginId The loginID is required.
   * @param phone The phone parameter can be empty in which case the phone will be removed.
   * @param isVerified The isVerified flag must be true for the user to be able to login with the
   *     email address.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updatePhone(String loginId, String phone, Boolean isVerified)
      throws DescopeException;

  /**
   * Update the email address for an existing user.
   *
   * @param loginId The loginID is required.
   * @param phone The phone parameter can be empty in which case the phone will be removed.
   * @param isVerified The isVerified flag must be true for the user to be able to login with the
   *     email address.
   * @param failOnConflict If true, the call will fail with an error instead of silently deleting
   *     and merging another user that already has this phone number.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updatePhone(String loginId, String phone, Boolean isVerified, Boolean failOnConflict)
      throws DescopeException;

  /**
   * Update an existing user's display name (i.e., their full name).
   *
   * @param loginId The loginID is required.
   * @param displayName The displayName parameter can be empty in which case the name will be
   *     removed.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateDisplayName(String loginId, String displayName) throws DescopeException;

  /**
   * Update an existing user's display name (i.e., their full name).
   *
   * @param loginId The loginID is required.
   * @param givenName The givenName parameter can be empty in which case the name will be removed.
   * @param middleName The middleName parameter can be empty in which case the name will be removed.
   * @param familyName The familyName parameter can be empty in which case the name will be removed.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  UserResponseDetails updateDisplayNames(String loginId, String givenName, String middleName,
      String familyName) throws DescopeException;

  /**
   * Update an existing user's picture (i.e., url to the avatar).
   *
   * @param loginId The loginID is required.
   * @param picture The picture parameter can be empty in which case the picture will be removed.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updatePicture(String loginId, String picture) throws DescopeException;

  /**
   * Update an existing user's custom attribute.
   *
   * @param loginId The loginID is required.
   * @param key key should be a custom attribute that was already declared in the Descope console
   *     app.
   * @param value value should match the type of the declared attribute
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateCustomAttributes(String loginId, String key, Object value)
      throws DescopeException;

  /**
   * Update an existing user's custom attribute.
   *
   * @param loginId The loginID is required.
   * @param key key should be a custom attribute that was already declared in the Descope console
   *     app.
   * @param value value should match the type of the declared attribute
   * @param strict Optional (nullable). When true, a key the project does not declare fails the request.
   *     When false or null, it is ignored.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateCustomAttributes(String loginId, String key, Object value, Boolean strict)
      throws DescopeException;

  /**
   * Update the email address for an existing user.
   *
   * @param loginId The loginID is required.
   * @param newLoginId The new login ID to be updated. Pass in empty string to just remove the existing loginId.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateLoginId(String loginId, String newLoginId) throws DescopeException;

  /**
   * Set roles for a user without tenant association. Use SetTenantRoles for users that are part of
   * a multi-tenant project.
   *
   * @param loginId The loginID is required.
   * @param roles User Roles
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails setRoles(String loginId, List<String> roles) throws DescopeException;

  /**
   * Add roles for a user without tenant association. Use AddTenantRoles for users that are part of
   * a multi-tenant project.
   *
   * @param loginId The loginID is required.
   * @param roles User Roles
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails addRoles(String loginId, List<String> roles) throws DescopeException;

  /**
   * Remove roles from a user without tenant association.
   *
   * @param loginId The loginID is required.
   * @param roles Use RemoveTenantRoles for users that are part of a multi-tenant project.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails removeRoles(String loginId, List<String> roles) throws DescopeException;

  /**
   * Set (associate) SSO applications for a user.
   *
   * @param loginId The loginID is required.
   * @param ssoAppIds List of SSO apps to associate
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails addSsoApps(String loginId, List<String> ssoAppIds) throws DescopeException;

  /**
   * Remove SSO application association from a user.
   *
   * @param loginId The loginID is required.
   * @param ssoAppIds List of SSO apps to associate
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails removeSsoApps(String loginId, List<String> ssoAppIds) throws DescopeException;

  /**
   * Associate SSO applications for a user.
   *
   * @param loginId The loginID is required.
   * @param ssoAppIds List of SSO apps to associate
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails setSsoApps(String loginId, List<String> ssoAppIds) throws DescopeException;

  /**
   * Add a tenant association for an existing user.
   *
   * @param loginId The loginID is required.
   * @param tenantId Tenant ID
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails addTenant(String loginId, String tenantId) throws DescopeException;

  /**
   * Remove a tenant association from an existing user.
   *
   * @param loginId The loginID is required.
   * @param tenantId Tenant ID
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails removeTenant(String loginId, String tenantId) throws DescopeException;

  /**
   * Set roles for a user in a specific tenant.
   *
   * @param loginId The loginID is required.
   * @param tenantId Tenant ID
   * @param roles Tenant Roles
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails setTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException;

  /**
   * Add roles for a user in a specific tenant.
   *
   * @param loginId The loginID is required.
   * @param tenantId Tenant ID
   * @param roles Tenant Roles
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails addTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException;

  /**
   * Remove roles for a user in a specific tenant.
   *
   * @param loginId The loginID is required.
   * @param tenantId Tenant ID
   * @param roles Tenant Roles
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails removeTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException;

  /**
   * Set a temporary password for the given login ID.
   * Note: The password will automatically be set as expired.
   * The user will not be able to log-in with this password, and will be required to replace it on
   * next login.
   *
   * @param loginId The loginID is required.
   * @param password Password.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void setTemporaryPassword(String loginId, String password) throws DescopeException;

  /**
   * Set a password for the given login ID.
   *
   * @param loginId The loginID is required.
   * @param password Password.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void setActivePassword(String loginId, String password) throws DescopeException;

  /**
   * Deprecated (use setTemporaryPassword() instead.
   * Set a password for the given login ID. Note: The password will automatically be set as expired.
   * The user will not be able to log-in with this password, and will be required to replace it on
   * next login.
   *
   * @param loginId The loginID is required.
   * @param password Password.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  @Deprecated
  void setPassword(String loginId, String password) throws DescopeException;

  /**
   * Expire the password for the given login ID. Note: user sign-in with an expired password, the
   * user will get `Password Expired` error. Use the `ResetPassword` or `ReplacePassword` methods to
   * reset/replace the password.
   *
   * @param loginId The loginID is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void expirePassword(String loginId) throws DescopeException;

  /**
   * Get the provider token for the given login ID.
   * Only users that sign-in using social providers will have token.
   * Note: The 'Manage tokens from provider' setting must be enabled.
   *
   * @param loginId The loginId we are retrieving token for and is required 
   * @param provider The provider for whom we retrieve the token from the list of providers under AppConstants or custom
   * @return The token details
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  ProviderTokenResponse getProviderToken(String loginId, String provider)
      throws DescopeException;

  /**
   * Get the provider token for the given login ID.
   * Only users that sign-in using social providers will have token.
   * Note: The 'Manage tokens from provider' setting must be enabled.
   *
   * @param loginId The loginId we are retrieving token for and is required
   * @param provider The provider for whom we retrieve the token from the list of providers under AppConstants or custom
   * @param withRefreshToken Optional (nullable), true to also return the provider's refresh token
   * @param forceRefresh Optional (nullable), true to refresh the token with the provider before returning it
   * @return The token details
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  ProviderTokenResponse getProviderToken(String loginId, String provider, Boolean withRefreshToken,
      Boolean forceRefresh) throws DescopeException;


  /**
   * Generate OTP for the given login ID of a test user. This is useful when running tests and don't
   * want to use 3rd party messaging services. The redirect URI is optional. If provided however, it
   * will be used instead of any global configuration.
   *
   * @param loginId The loginID is required.
   * @param deliveryMethod Choose the selected delivery method for verification.
   * @return It returns the code for the login (exactly as it sent via Email or SMS)
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  OTPTestUserResponse generateOtpForTestUser(String loginId, DeliveryMethod deliveryMethod)
      throws DescopeException;

  /**
   * Generate Magic Link for the given login ID of a test user. This is useful when running tests
   * and don't want to use 3rd party messaging services. The redirect URI is optional. If provided
   * however, it will be used instead of any global configuration.
   *
   * @param loginId The loginID is required.
   * @param uri An ptional redirect URL which will be used instead of any global configuration.
   * @param deliveryMethod Choose the selected delivery method for verification.
   * @return It returns the link for the login (exactly as it sent via Email)
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  MagicLinkTestUserResponse generateMagicLinkForTestUser(
      String loginId, String uri, DeliveryMethod deliveryMethod) throws DescopeException;

  /**
   * Generate Enchanted Link for the given login ID of a test user. This is useful when running
   * tests and don't want to use 3rd party messaging services The redirect URI is optional. If
   * provided however, it will be used instead of any global configuration.
   *
   * @param loginId loginId The loginID is required.
   * @param uri An ptional redirect URL which will be used instead of any global configuration.
   * @return It returns the link for the login (exactly as it sent via Email) and pendingRef which
   *     is used to poll for a valid session
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  EnchantedLinkTestUserResponse generateEnchantedLinkForTestUser(String loginId, String uri)
        throws DescopeException;

  /**
   * Generate an embedded link token, later can be used to authenticate via magiclink verify method
   * or via flow verify step.
   *
   * @param loginId loginId The loginID is required.
   * @param customClaims additional claims to be added to the verified token JWT
   * @return It returns the token that can then be verified using the magic link 'verify' function,
   *         either directly or through a flow.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  String generateEmbeddedLink(String loginId, Map<String, Object> customClaims)
        throws DescopeException;

  /**
   * Generate an embedded link token, later can be used to authenticate via magiclink verify method
   * or via flow verify step.
   *
   * @param loginId The loginID is required.
   * @param customClaims additional claims to be added to the verified token JWT
   * @param timeout The timeout in seconds for the embedded link token.
   *         If not provided or set to 0, a default timeout (10 minutes) will be used.
   * @return It returns the token that can then be verified using the magic link 'verify' function,
   *         either directly or through a flow.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  String generateEmbeddedLink(String loginId, Map<String, Object> customClaims, int timeout)
        throws DescopeException;

  /**
   * Generate a sign-up embedded link token, later can be used to authenticate via magiclink
   * verify method or via flow verify step.
   *
   * @param loginId The loginID is required.
   * @param user The user object containing user details.
   * @param emailVerified Boolean indicating if the email is verified.
   * @param phoneVerified Boolean indicating if the phone is verified.
   * @param loginOptions Options for the login process.
   * @param timeout The timeout in seconds for the embedded link token.
   *         If not provided or set to 0, a default timeout (10 minutes) will be used.
   * @return It returns the token that can then be verified using the magic link 'verify' function,
   *         either directly or through a flow.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  String generateSignUpEmbeddedLink(String loginId, User user, Boolean emailVerified,
        Boolean phoneVerified, LoginOptions loginOptions, int timeout)
        throws DescopeException;

  /**
   * Use to retrieve users' authentication history, by the given user's ids.
   *
   * @param userIds List of user IDs to retrieve the history for
   * @return {{@link List} of {@link UserHistoryResponse}} of all requested users login history
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  List<UserHistoryResponse> history(List<String> userIds) throws DescopeException;

  /**
   * Delete multiple users, by the given user IDs.
   *
   * @param userIds List of user IDs to delete. The list must not be empty.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void deleteBatch(List<String> userIds) throws DescopeException;

  /**
   * Remove the TOTP seed for the user with the given login ID.
   *
   * @param loginId The loginID is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void removeTOTPSeed(String loginId) throws DescopeException;

  /**
   * Remove all registered passkeys (WebAuthn devices) for the user with the given login ID.
   *
   * @param loginId The loginID is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void removeAllPasskeys(String loginId) throws DescopeException;

  /**
   * Remove a single registered passkey, identified by its credential ID, for the user with the
   * given login ID.
   *
   * @param loginId The loginID is required.
   * @param credentialId The credential ID of the passkey to remove is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  void removePasskey(String loginId, String credentialId) throws DescopeException;

  /**
   * List the registered passkeys (WebAuthn devices) for the user with the given login ID.
   *
   * @param loginId The loginID is required.
   * @return {{@link List} of {@link UserPasskey}} of the user's registered passkeys.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  List<UserPasskey> listPasskeys(String loginId) throws DescopeException;

  /**
   * Update the recovery email for the user with the given login ID.
   *
   * @param loginId The loginID is required.
   * @param email The new recovery email.
   * @param verified Boolean indicating if the recovery email is verified.
   * @return The updated {@link UserResponseDetails}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateRecoveryEmail(String loginId, String email, Boolean verified)
      throws DescopeException;

  /**
   * Update the recovery phone for the user with the given login ID.
   *
   * @param loginId The loginID is required.
   * @param phone The new recovery phone.
   * @param verified Boolean indicating if the recovery phone is verified.
   * @return The updated {@link UserResponseDetails}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails updateRecoveryPhone(String loginId, String phone, Boolean verified)
      throws DescopeException;

  /**
   * List the trusted devices of one or more users.
   *
   * @param loginIdsOrUserIds The users' login IDs or user IDs, at least one and at most 100.
   * @return {{@link List} of {@link TrustedDevice}} of the users' trusted devices.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  List<TrustedDevice> listTrustedDevices(List<String> loginIdsOrUserIds) throws DescopeException;

  /**
   * Add a user to one or more families. Each family may also set the user's roles and family-scoped
   * attributes in that family; leaving them null on a family the user already belongs to keeps them.
   *
   * @param loginIdOrUserId The loginID or userID is required.
   * @param families The families to add the user to, at least one.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails addFamilies(String loginIdOrUserId, List<AssociatedFamily> families)
      throws DescopeException;

  /**
   * Remove a user from one or more families.
   *
   * @param loginIdOrUserId The loginID or userID is required.
   * @param familyIds The IDs of the families to remove the user from, at least one.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails removeFamilies(String loginIdOrUserId, List<String> familyIds) throws DescopeException;

  /**
   * Import users from another identity provider.
   *
   * @param source The source the users are exported from is required.
   * @param users Optional (nullable), the exported users file.
   * @param hashes Optional (nullable), the exported password hashes file.
   * @param dryRun true to validate the import without saving any users.
   * @return {@link ImportUsersResponse} with the imported users and the failures
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  ImportUsersResponse importUsers(String source, byte[] users, byte[] hashes, boolean dryRun)
      throws DescopeException;
}
