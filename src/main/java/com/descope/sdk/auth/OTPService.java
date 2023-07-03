package com.descope.sdk.auth;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.User;

public interface OTPService {
  /**
   * Use to login a user based on the given loginID either email or a phone and choose the selected
   * delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions)
      throws DescopeException;

  /**
   * Use to create a new user based on the given loginID either email or a phone. choose the
   * selected delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param user - {@link User User}
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUp(DeliveryMethod deliveryMethod, String loginId, User user) throws DescopeException;

  /**
   * Use to login in using loginID, if user does not exist, a new user will be created with the
   * given loginID.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUpOrIn(DeliveryMethod deliveryMethod, String loginId) throws DescopeException;

  /**
   * Use to verify a SignIn/SignUp based on the given loginID either an email or a phone followed by
   * the code used to verify and authenticate the user.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param code - code received over email or phone
   * @return {@link AuthenticationInfo AuthenticationInfo}
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo verifyCode(DeliveryMethod deliveryMethod, String loginId, String code)
      throws DescopeException;

  /**
   * Use to a update email, and verify via OTP.
   *
   * @param loginId - User login ID
   * @param email - User email
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String updateUserEmail(String loginId, String email) throws DescopeException;

  /**
   * Use to update phone and validate via OTP.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param phone - User Phone
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone)
      throws DescopeException;
}
