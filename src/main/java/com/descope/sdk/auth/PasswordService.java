package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.user.User;
import java.util.Map;

public interface PasswordService {

  /**
   * Use to create a new user that authenticates with a password.
   *
   * @param loginId - new user login ID
   * @param user - new user details
   * @param password - new user cleartext password
   * @return {@link AuthenticationInfo} the authentication info created from the sign up
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo signUp(String loginId, User user, String password) throws DescopeException;

  /**
   * Use to login a user by authenticating with a password.
   *
   * @param loginId - the user login ID
   * @param password - the cleartext password for the user
   * @return {@link AuthenticationInfo} the authentication info created from the sign in
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo signIn(String loginId, String password) throws DescopeException;

  /**
   * Sends a password reset prompt to the user with the given login ID according to the password settings
   * defined in the Descope console.
   * The user must be verified according to the configured password reset method.
   * Once verified, use UpdateUserPassword to change the user's password.
   *
   * @param loginId - the user login ID
   * @param redirectURL - an optional parameter that is used by Magic Link or Enchanted Link if those are the chosen
   *                      reset methods. See the Magic Link and Enchanted Link sections for more details.
   * @throws DescopeException - error upon failure
   */
  void sendPasswordReset(String loginId, String redirectURL) throws DescopeException;

  /**
   * Sends a password reset prompt to the user with the given login ID according to the password settings
   * defined in the Descope console.
   * The user must be verified according to the configured password reset method.
   * Once verified, use UpdateUserPassword to change the user's password.
   *
   * @param loginId - the user login ID
   * @param redirectURL - an optional parameter that is used by Magic Link or Enchanted Link if those are the chosen
   *                      reset methods. See the Magic Link and Enchanted Link sections for more details.
   * @param templateOptions - used to pass dynamic options for the messaging (Email / SMS / Voice call / WhatsApp)
   *                          template
   * @throws DescopeException - error upon failure
   */
  void sendPasswordReset(String loginId, String redirectURL, Map<String, String> templateOptions)
      throws DescopeException;

  /**
   * Updates a user's password according to the given login ID.
   * This function requires the user to have an active session.
   *
   * @param loginId - the user login ID
   * @param newPassword - the new cleartext password that must conform to the password policy defined
   *                      in the password settings in the Descope console.
   * @param refreshToken - a valid refresh token
   * @throws DescopeException - error upon failure
   */
  void updateUserPassword(String loginId, String newPassword, String refreshToken)
      throws DescopeException;

  /**
   * Updates a user's password according to the given login ID.
   * This function requires the current or 'oldPassword' to be active.
   * If the user can be successfully authenticated using the oldPassword, the user's
   * password will be updated to newPassword.
   *
   * @param loginId - the user login ID
   * @param oldPassword - the old valid password for the user
   * @param newPassword - the new cleartext password that must conform to the password policy defined
   *                      in the password settings in the Descope console.
   * @return {@link AuthenticationInfo} the authentication info created from the sign in with the new password
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo replaceUserPassword(String loginId, String oldPassword, String newPassword)
      throws DescopeException;

  /**
   * Fetch the rules for valid passwords configured in the policy in the Descope console.
   * This can be used to implement client-side validation of new user passwords for a better user experience.
   * Either way, the comprehensive policy is always enforced by Descope on the server side.
   *
   * @return {@link PasswordPolicy} the passwird policy for the project
   * @throws DescopeException - error upon failure
   */
  PasswordPolicy getPasswordPolicy() throws DescopeException;
}
