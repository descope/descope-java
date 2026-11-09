package com.descope.sdk.auth;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.otp.OTPResponse;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
import com.descope.model.user.User;
import java.util.Map;

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
   * Use to login a user based on the given loginID either email or a phone and choose the selected
   * delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @param refreshToken - if doing step-up or mfa refresh token is required
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions, String refreshToken)
      throws DescopeException;

  /**
   * Use to login a user based on the given loginID either email or a phone and choose the selected
   * delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}: EMAIL, SMS or VOICE
   * @param loginId - User login ID
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @param refreshToken - if doing step-up or mfa refresh token is required
   * @param signInOptions - optional {@link OTPSignInOptions OTPSignInOptions}
   * @return {@link OTPResponse OTPResponse} with the masked address and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions, String refreshToken,
      OTPSignInOptions signInOptions) throws DescopeException;

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
   * Use to create a new user based on the given loginID either email or a phone. choose the
   * selected delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param user - {@link User User}
   * @param signupOptions - optional claims and template strings
   * @return masked address where the link was sent (email, whatsapp or phone)
   * @throws DescopeException - error upon failure
   */
  String signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signupOptions)
      throws DescopeException;

  /**
   * Use to create a new user based on the given loginID either email or a phone. choose the
   * selected delivery method for verification.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}: EMAIL, SMS or VOICE
   * @param loginId - User login ID
   * @param user - {@link User User}
   * @param signupOptions - optional claims and template strings
   * @param otpSignUpOptions - optional {@link OTPSignUpOptions OTPSignUpOptions}
   * @return {@link OTPResponse OTPResponse} with the masked address and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signupOptions,
      OTPSignUpOptions otpSignUpOptions) throws DescopeException;

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
   * Use to login in using loginID, if user does not exist, a new user will be created with the
   * given loginID. For step-up or mfa use signIn.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}: EMAIL, SMS or VOICE
   * @param loginId - User login ID
   * @param loginOptions - optional {@link LoginOptions LoginOptions}
   * @param signInOptions - optional {@link OTPSignInOptions OTPSignInOptions}
   * @return {@link OTPResponse OTPResponse} with the masked address and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse signUpOrIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions,
      OTPSignInOptions signInOptions) throws DescopeException;

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
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions)
      throws DescopeException;

  /**
   * Use to a update email, and verify via OTP.
   *
   * @param loginId - User login ID
   * @param email - User email
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @param templateOptions - optional parameters for template
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions,
        Map<String, String> templateOptions)
      throws DescopeException;

  /**
   * Use to a update email, and verify via OTP.
   *
   * @param loginId - User login ID
   * @param email - User email
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @param templateOptions - optional parameters for template
   * @param otpUpdateOptions - optional {@link OTPUpdateOptions OTPUpdateOptions}
   * @return {@link OTPResponse OTPResponse} with the masked email and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions,
      Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions) throws DescopeException;

  /**
   * Use to update phone and validate via OTP.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param phone - User Phone
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException;

  /**
   * Use to update phone and validate via OTP.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}
   * @param loginId - User login ID
   * @param phone - User Phone
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @param templateOptions - optional parameters for template
   * @return - masked address where the link was sent (email or phone)
   * @throws DescopeException - error upon failure
   */
  String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException;

  /**
   * Use to update phone and validate via OTP.
   *
   * @param deliveryMethod - {@link com.descope.enums.DeliveryMethod DeliveryMethod}: SMS or VOICE
   * @param loginId - User login ID
   * @param phone - User Phone
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @param templateOptions - optional parameters for template
   * @param otpUpdateOptions - optional {@link OTPUpdateOptions OTPUpdateOptions}
   * @return {@link OTPResponse OTPResponse} with the masked phone and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions)
      throws DescopeException;

  /**
   * Use to login a user and send the code by instant message to the user's phone.
   *
   * @param loginId - User login ID
   * @param loginOptions - {@link LoginOptions LoginOptions}
   * @param refreshToken - if doing step-up or mfa refresh token is required
   * @param signInOptions - optional {@link OTPSignInOptions OTPSignInOptions}
   * @return {@link OTPResponse OTPResponse} with the masked phone and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse signInIM(String loginId, LoginOptions loginOptions, String refreshToken,
      OTPSignInOptions signInOptions) throws DescopeException;

  /**
   * Use to create a new user and send the code by instant message to the user's phone.
   *
   * @param loginId - User login ID, the phone when user.phone is not set
   * @param user - {@link User User}
   * @param signupOptions - optional claims and template strings
   * @param otpSignUpOptions - optional {@link OTPSignUpOptions OTPSignUpOptions}
   * @return {@link OTPResponse OTPResponse} with the masked phone and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse signUpIM(String loginId, User user, SignUpOptions signupOptions, OTPSignUpOptions otpSignUpOptions)
      throws DescopeException;

  /**
   * Use to login in using loginID, if user does not exist, a new user will be created with the
   * given loginID. The code is sent by instant message. For step-up or mfa use signInIM.
   *
   * @param loginId - User login ID
   * @param loginOptions - optional {@link LoginOptions LoginOptions}
   * @param signInOptions - optional {@link OTPSignInOptions OTPSignInOptions}
   * @return {@link OTPResponse OTPResponse} with the masked phone and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse signUpOrInIM(String loginId, LoginOptions loginOptions, OTPSignInOptions signInOptions)
      throws DescopeException;

  /**
   * Use to verify a code sent by instant message and authenticate the user.
   *
   * @param loginId - User login ID
   * @param code - 6 character code received by instant message
   * @return {@link AuthenticationInfo AuthenticationInfo}
   * @throws DescopeException - error upon failure
   */
  AuthenticationInfo verifyCodeIM(String loginId, String code) throws DescopeException;

  /**
   * Use to update phone and validate via OTP sent by instant message.
   *
   * @param loginId - User login ID
   * @param phone - User Phone
   * @param refreshToken - refresh token to perform the update
   * @param updateOptions - update options for the update
   * @param templateOptions - optional parameters for template
   * @param otpUpdateOptions - optional {@link OTPUpdateOptions OTPUpdateOptions}
   * @return {@link OTPResponse OTPResponse} with the masked phone and verification attempts
   * @throws DescopeException - error upon failure
   */
  OTPResponse updateUserPhoneIM(String loginId, String phone, String refreshToken, UpdateOptions updateOptions,
      Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions) throws DescopeException;
}
