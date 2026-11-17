package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPRequestOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.user.User;

/** Authentication by a message the user sends from a messaging app (NOTP), such as WhatsApp. */
public interface NOTPService {

  /**
   * Use to create a new user, verified by a message the user sends from the messaging app.
   *
   * @param provider       - the messaging provider, for example {@code whatsapp}
   * @param loginId        - User login ID, optional
   * @param user           - {@link User User}, optional; its phone defaults to the login ID
   * @param signUpOptions  - {@link NOTPSignUpOptions NOTPSignUpOptions}, optional
   * @param requestOptions - {@link NOTPRequestOptions NOTPRequestOptions}, optional
   * @return pendingRef, redirectUrl to the messaging app and a QR code image
   * @throws DescopeException - error upon failure
   */
  NOTPResponse signUp(String provider, String loginId, User user, NOTPSignUpOptions signUpOptions,
      NOTPRequestOptions requestOptions) throws DescopeException;

  /**
   * Use to login a user, verified by a message the user sends from the messaging app.
   *
   * @param provider       - the messaging provider, for example {@code whatsapp}
   * @param loginId        - User login ID, optional since it can be taken from the provider
   * @param refreshToken   - the user's refresh token, required for step-up or mfa
   * @param loginOptions   - {@link NOTPLoginOptions NOTPLoginOptions}, optional
   * @param requestOptions - {@link NOTPRequestOptions NOTPRequestOptions}, optional
   * @return pendingRef, redirectUrl to the messaging app and a QR code image
   * @throws DescopeException - error upon failure
   */
  NOTPResponse signIn(String provider, String loginId, String refreshToken, NOTPLoginOptions loginOptions,
      NOTPRequestOptions requestOptions) throws DescopeException;

  /**
   * Use to login a user, creating a new user if one does not exist.
   *
   * @param provider       - the messaging provider, for example {@code whatsapp}
   * @param loginId        - User login ID, optional since it can be taken from the provider
   * @param signUpOptions  - {@link NOTPSignUpOptions NOTPSignUpOptions}, optional
   * @param requestOptions - {@link NOTPRequestOptions NOTPRequestOptions}, optional
   * @return pendingRef, redirectUrl to the messaging app and a QR code image
   * @throws DescopeException - error upon failure
   */
  NOTPResponse signUpOrIn(String provider, String loginId, NOTPSignUpOptions signUpOptions,
      NOTPRequestOptions requestOptions) throws DescopeException;

  /**
   * Use to update a user's phone, verified by a message the user sends from the messaging app.
   *
   * @param provider      - the messaging provider, for example {@code whatsapp}
   * @param loginId       - User login ID
   * @param phone         - the new phone, optional
   * @param refreshToken  - the user's refresh token
   * @param updateOptions - {@link NOTPUpdateOptions NOTPUpdateOptions}, optional
   * @return pendingRef, redirectUrl to the messaging app and a QR code image
   * @throws DescopeException - error upon failure
   */
  NOTPResponse updateUser(String provider, String loginId, String phone, String refreshToken,
      NOTPUpdateOptions updateOptions) throws DescopeException;

  /**
   * Use to get the session of a sign-up or sign-in, once the user has sent the message.
   *
   * @param pendingRef - the pendingRef returned when starting the sign-up or sign-in
   * @return authentication info
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo getSession(String pendingRef) throws DescopeException;
}
