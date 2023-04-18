package com.descope.sdk.auth;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.User;
import com.descope.model.auth.AuthenticationInfo;

public interface MagicLinkService {

  /**
   * SignIn - Use to login a user based on a magic link that will be sent either email, sms or
   * whatsapp and choose the selected delivery method for verification (see auth/DeliveryMethod).
   * returns the masked address where the link was sent (email, sms or whatsapp) or an error upon
   * failure.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param uri - Base URI
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signIn(DeliveryMethod deliveryMethod, String loginId, String uri) throws DescopeException;

  /**
   * Use to create a new user based on the given loginID either email, sms or whatsapp. Choose the
   * selected delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param uri - Base URI
   * @param user - {@link com.descope.model.User User}
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user)
      throws DescopeException;

  /**
   * Verify - Use to verify a SignIn/SignUp request, based on the magic link token generated. if the
   * link was generated with crossDevice, the authentication info will be nil, and should returned
   * with GetSession.
   *
   * @param token - Token
   * @return
   * @throws DescopeException
   */
  AuthenticationInfo verify(String token) throws DescopeException;
}
