package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.User;
import com.descope.model.webauthn.WebAuthnFinishRequest;
import com.descope.model.webauthn.WebAuthnTransactionResponse;

/**
 * Implements WebAuthn server side authentication as a mid-layer between client WebAuthn and Descope.
 */
public interface WebAuthnService {
  /**
   * Use to start an authentication process with webauthn for the new user argument.
   *
   * @param loginId the end-user login id
   * @param user the user details
   * @param origin the origin of the URL for the web page where the webauthn operation is taking place, as returned
   *               by calling document.location.origin via javascript.
   * @return transaction id response on success.
   * @throws DescopeException on failure of any kind
   */
  WebAuthnTransactionResponse signUpStart(String loginId, User user, String origin) throws DescopeException;

  /**
   * Use to finish an authentication process with a given transaction id and credentials after been signed
   * by the credentials navigator.
   *
   * @param finishRequest the browser finish response
   * @return {@link AuthenticationInfo} for a successful response
   * @throws DescopeException on failure of any kind
   */
  AuthenticationInfo signUpFinish(WebAuthnFinishRequest finishRequest) throws DescopeException;

  /**
   * Use to start an authentication validation with webauthn for an existing user with the given loginID.
   *
   * @param loginId the end-user login id
   * @param origin the origin of the URL for the web page where the webauthn operation is taking place, as returned
   *               by calling document.location.origin via javascript.
   * @param token when doing step-up or mfa then we need current session token
   * @param loginOptions {@link LoginOptions LoginOptions}
   * @return transaction id response on success
   * @throws DescopeException on failure of any kind
   */
  WebAuthnTransactionResponse signInStart(String loginId, String origin, String token, LoginOptions loginOptions)
      throws DescopeException;

  /**
   * Use to finish an authentication process with a given transaction id and credentials after been signed
   * by the credentials navigator.
   *
   * @param finishRequest the browser finish response
   * @return {@link AuthenticationInfo} for a successful response
   * @throws DescopeException on failure of any kind
   */
  AuthenticationInfo signInFinish(WebAuthnFinishRequest finishRequest) throws DescopeException;

  /**
   * Use to start an authentication validation with webauthn.
   * If user does not exist, a new user will be created with the given login ID.
   * The create field in the response object determines which browser API should be called,
   * either navigator.credentials.create or navigator.credentials.get as well as whether to call signUpFinish
   * (if create is true) or signInFinish (if create is false) later to finalize the operation.
   *
   * @param loginId the end-user login id
   * @param origin the origin of the URL for the web page where the webauthn operation is taking place, as returned
   *               by calling document.location.origin via javascript.
   * @return transaction id response on success
   * @throws DescopeException on failure of any kind
   */
  WebAuthnTransactionResponse signUpOrInStart(String loginId, String origin) throws DescopeException;

  /**
   * Use to start an add webauthn device process for an existing user with the given loginId.
   * Token is required to send it to Descope, for verification.
   *
   * @param loginId the end-user login id
   * @param origin the origin of the URL for the web page where the webauthn operation is taking place, as returned
   *               by calling document.location.origin via javascript.
   * @param token an existing refresh token must be established and the token sent to Descope
   * @return transaction id response on success
   * @throws DescopeException on failure of any kind
   */
  WebAuthnTransactionResponse updateUserDeviceStart(String loginId, String origin, String token)
      throws DescopeException;

  /**
   * Use to finish an add webauthn device process with a given transaction id and credentials after been signed
   * by the credentials navigator.
   *
   * @param finishRequest the browser finish response
   * @throws DescopeException on failure of any kind
   */
  void updateUserDeviceFinish(WebAuthnFinishRequest finishRequest) throws DescopeException;
}
