package com.descope.sdk.mgmt;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.AllUsersResponseDetails;
import com.descope.model.user.response.UserResponseDetails;
import java.net.URI;
import java.util.List;

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
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails invite(String loginId, UserRequest request) throws DescopeException;

  /**
   * Update an existing user.
   *
   * <p>IMPORTANT: All parameters will override whatever values are currently set in the existing
   * user. Use carefully.
   *
   * @param loginId The loginID is required and will determine what the user will use to sign in,
   *     make sure the login id is unique for test.
   * @param request request is optional, and if provided, all attributes within it are optional.
   * @return {@link UserResponseDetails UserResponseDetails}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  UserResponseDetails update(String loginId, UserRequest request) throws DescopeException;

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
   * Set a password for the given login ID. Note: The password will automatically be set as expired.
   * The user will not be able to log-in with this password, and will be required to replace it on
   * next login.
   *
   * @param loginId The loginID is required.
   * @param password Password.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
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
  String generateOtpForTestUser(String loginId, DeliveryMethod deliveryMethod)
      throws DescopeException;

  /**
   * Generate Magic Link for the given login ID of a test user. This is useful when running tests
   * and don't want to use 3rd party messaging services. The redirect URI is optional. If provided
   * however, it will be used instead of any global configuration.
   *
   * @param loginId The loginID is required.
   * @param deliveryMethod Choose the selected delivery method for verification.
   * @return It returns the link for the login (exactly as it sent via Email)
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  String generateMagicLinkForTestUser(String loginId, URI uri, DeliveryMethod deliveryMethod)
      throws DescopeException;

  /**
   * Generate Enchanted Link for the given login ID of a test user. This is useful when running
   * tests and don't want to use 3rd party messaging services The redirect URI is optional. If
   * provided however, it will be used instead of any global configuration.
   *
   * @param loginId loginId The loginID is required.
   * @return It returns the link for the login (exactly as it sent via Email) and pendingRef which
   *     is used to poll for a valid session
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be
   *     thrown.
   */
  String generateEnchantedLinkForTestUser(String loginId, URI uri) throws DescopeException;
}
