package com.descope.sdk.auth;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;

public interface MagicLinkService {

  /**
   * SignIn - Use to login a user based on a magic link that will be sent either email, sms or
   * whatsapp and choose the selected delivery method for verification (see auth/DeliveryMethod).
   * returns the masked address where the link was sent (email, sms or whatsapp) or an error upon
   * failure.
   *
   * @param deliveryMethod - {@link DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param uri - Base URI
   * @param token - when doing step-up or mfa then we need current session token
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signIn(
      DeliveryMethod deliveryMethod,
      String loginId,
      String uri,
      String token,
      LoginOptions loginOptions)
      throws DescopeException;

  /**
   * Use to create a new user based on the given loginID either email, sms or whatsapp. Choose the
   * selected delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param uri - Base URI
   * @param user - {@link User User}
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user)
      throws DescopeException;

  /**
   * Use to create a new user based on the given loginID either email, sms or whatsapp. Choose the
   * selected delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param uri - Base URI
   * @param user - {@link User User}
   * @param signupOptions - optional claims and template strings
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user, SignUpOptions signupOptions)
      throws DescopeException;

  /**
   * Verify - Use to verify a SignIn/SignUp request, based on the magic link token generated. if the
   * link was generated with crossDevice, the authentication info will be nil, and should returned
   * with GetSession.
   *
   * @param token - Token
   * @return {@link AuthenticationInfo}
   * @throws DescopeException when error occurs
   */
  AuthenticationInfo verify(String token) throws DescopeException;

  /**
   * Use to login in using loginID, if user does not exist, a new user will be created with the
   * given loginID. Choose the selected delivery method for verification
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param uri - Base URI
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUpOrIn(DeliveryMethod deliveryMethod, String loginId, String uri)
      throws DescopeException;

  /**
   * Use to update email and validate via magiclink.
   *
   * @param loginId - User login ID
   * @param email - User email
   * @param uri - Base URI
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @return masked address where the link was sent (email)
   * @throws DescopeException - error upon failure
   */
  String updateUserEmail(String loginId, String email, String uri, String refreshToken, UpdateOptions updateOptions)
      throws DescopeException;

  /**
   * Use to update phone and validate via magiclink. Allowed methods are phone based methods -
   * whatsapp and SMS
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param phone - User phone
   * @param uri - Base URI
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @return masked address where the link was sent (whatsapp or sms)
   * @throws DescopeException - error upon failure
   */
  String updateUserPhone(
      DeliveryMethod deliveryMethod, String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException;
}
