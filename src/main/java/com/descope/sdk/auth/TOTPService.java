package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.totp.TOTPResponse;
import com.descope.model.user.User;

public interface TOTPService {

  /**
   * Create a new user, and create a seed for it.
   *
   * @param loginId - User login ID
   * @param user    - {@link User User}
   * @return value will allow to connect it to an authenticator app
   * @throws DescopeException - error upon failure
   */
  TOTPResponse signUp(
      String loginId,
      User user)
      throws DescopeException;

  /**
   * Use to verify a SignIn/SignUp based on the given loginID.
   *
   * @param loginId      - User login ID
   * @param code         - code to verify
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @return a list of cookies
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo signInCode(
      String loginId,
      String code,
      LoginOptions loginOptions)
      throws DescopeException;

  /**
   * Use to verify a SignIn/SignUp based on the given loginID.
   *
   * @param loginId      - User login ID
   * @param code         - code to verify
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @param refreshToken - Refresh Token
   * @return a list of cookies
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo signInCode(
      String loginId,
      String code,
      LoginOptions loginOptions,
      String refreshToken)
      throws DescopeException;

  /**
   * Set a seed to an existing user, so the user can use an authenticator app.
   *
   * @param loginId - User login ID
   * @return TOTPResponse
   * @throws DescopeException - error upon failure
   */
  TOTPResponse updateUser(String loginId)
      throws DescopeException;
}
