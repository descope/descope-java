package com.descope.sdk.auth;

import com.descope.enums.NOTPProvider;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPSignUpUser;
import com.descope.model.notp.NOTPUpdateOptions;

/** Authentication by a message the user sends from a messaging app (NOTP), such as WhatsApp. */
public interface NOTPService {
  /**
   * Use to login a user by a message the user sends from the messaging app.
   *
   * @param provider     - the messaging provider, such as {@link NOTPProvider#WHATSAPP}
   * @param loginId      - User login ID, a phone number, or empty to use the phone number the
   *                     message is sent from
   * @param loginOptions - optional {@link NOTPLoginOptions NOTPLoginOptions}
   * @param options      - optional {@link NOTPOptions NOTPOptions}
   * @param refreshToken - the user's refresh token, required only for step-up or mfa
   * @return {@link NOTPResponse} with the link to the app, a QR code image and the pending ref
   * @throws DescopeException - error upon failure
   */
  NOTPResponse signIn(NOTPProvider provider, String loginId, NOTPLoginOptions loginOptions, NOTPOptions options,
      String refreshToken) throws DescopeException;

  /**
   * Use to create a new user, verified by a message the user sends from the messaging app.
   *
   * @param provider      - the messaging provider, such as {@link NOTPProvider#WHATSAPP}
   * @param loginId       - User login ID, a phone number, or empty to use the phone number the
   *                      message is sent from
   * @param user          - optional {@link NOTPSignUpUser NOTPSignUpUser}; its phone defaults to the login ID
   * @param signUpOptions - optional {@link NOTPSignUpOptions NOTPSignUpOptions}
   * @param options       - optional {@link NOTPOptions NOTPOptions}
   * @return {@link NOTPResponse} with the link to the app, a QR code image and the pending ref
   * @throws DescopeException - error upon failure
   */
  NOTPResponse signUp(NOTPProvider provider, String loginId, NOTPSignUpUser user, NOTPSignUpOptions signUpOptions,
      NOTPOptions options) throws DescopeException;

  /**
   * Use to login a user by a message the user sends from the messaging app. If the user does
   * not exist, a new user will be created.
   *
   * @param provider     - the messaging provider, such as {@link NOTPProvider#WHATSAPP}
   * @param loginId      - User login ID, a phone number, or empty to use the phone number the
   *                     message is sent from
   * @param loginOptions - optional {@link NOTPLoginOptions NOTPLoginOptions}
   * @param options      - optional {@link NOTPOptions NOTPOptions}
   * @param refreshToken - the user's refresh token, required only for step-up or mfa
   * @return {@link NOTPResponse} with the link to the app, a QR code image and the pending ref
   * @throws DescopeException - error upon failure
   */
  NOTPResponse signUpOrIn(NOTPProvider provider, String loginId, NOTPLoginOptions loginOptions, NOTPOptions options,
      String refreshToken) throws DescopeException;

  /**
   * Use to update the user's phone, verified by a message the user sends from the messaging app.
   *
   * @param provider      - the messaging provider, such as {@link NOTPProvider#WHATSAPP}
   * @param loginId       - User login ID
   * @param phone         - the new phone, or empty to use the phone number the message is sent from
   * @param refreshToken  - the user's refresh token
   * @param updateOptions - optional {@link NOTPUpdateOptions NOTPUpdateOptions}
   * @return {@link NOTPResponse} with the link to the app, a QR code image and the pending ref
   * @throws DescopeException - error upon failure
   */
  NOTPResponse updateUserPhone(NOTPProvider provider, String loginId, String phone, String refreshToken,
      NOTPUpdateOptions updateOptions) throws DescopeException;

  /**
   * Use to get the session of a sign in, sign up or update, once the user has sent the message.
   * Poll it with the pending ref the call returned.
   *
   * @param pendingRef - the pending ref
   * @return authentication info
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo getSession(String pendingRef) throws DescopeException;
}
