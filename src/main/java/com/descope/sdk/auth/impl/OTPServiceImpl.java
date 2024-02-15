package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.enums.DeliveryMethod.SMS;
import static com.descope.enums.DeliveryMethod.WHATSAPP;
import static com.descope.literals.Routes.AuthEndPoints.OTP_UPDATE_EMAIL_LINK;
import static com.descope.literals.Routes.AuthEndPoints.OTP_UPDATE_PHONE_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_OTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_OTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_OTP_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.response.Masked;
import com.descope.model.otp.AuthenticationVerifyRequestBody;
import com.descope.model.otp.SignInRequest;
import com.descope.model.otp.SignUpRequest;
import com.descope.model.otp.UpdateEmailRequestBody;
import com.descope.model.otp.UpdatePhoneRequestBody;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OTPService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;

class OTPServiceImpl extends AuthenticationServiceImpl implements OTPService {

  OTPServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions) {
    return signIn(deliveryMethod, loginId, loginOptions, null);
  }

  @Override
  public String signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions, String refreshToken)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    ApiProxy apiProxy;
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI otpSignInURL = composeSignInURL(deliveryMethod);
    SignInRequest signInRequest = new SignInRequest(loginId, loginOptions);
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    Masked masked = apiProxy.post(otpSignInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, User user) {
    return signUp(deliveryMethod, loginId, user, null);
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signUpOptions)
      throws DescopeException {
    if (user == null) {
      user = new User();
    }
    verifyDeliveryMethod(deliveryMethod, loginId, user);
    SignUpRequest signUpRequest = newSignUpRequest(deliveryMethod, user);
    signUpRequest.setLoginId(loginId);
    signUpRequest.setUser(user);
    if (signUpOptions != null) {
      signUpRequest.setLoginOptions(signUpOptions);
    }
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI otpSignUpURL = composeSignUpURI(deliveryMethod);
    ApiProxy apiProxy = getApiProxy();
    Masked masked = apiProxy.post(otpSignUpURL, signUpRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId) throws DescopeException {

    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI otpSignUpOrInURL = composeSignUpOrInURL(deliveryMethod);
    SignInRequest signInRequest = new SignInRequest(loginId, null);
    ApiProxy apiProxy = getApiProxy();
    Masked masked = apiProxy.post(otpSignUpOrInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public AuthenticationInfo verifyCode(DeliveryMethod deliveryMethod, String loginId, String code)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (PHONE_PATTERN.matcher(loginId).matches()) {
      deliveryMethod = SMS;
    } else if (EMAIL_PATTERN.matcher(loginId).matches()) {
      deliveryMethod = EMAIL;
    }
    if (deliveryMethod == null) {
      throw ServerCommonException.invalidArgument("Method");
    }
    AuthenticationVerifyRequestBody authenticationVerifyRequestBody =
        new AuthenticationVerifyRequestBody(loginId, code, null);
    URI otpVerifyCode = composeVerifyCodeURL(deliveryMethod);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(otpVerifyCode, authenticationVerifyRequestBody, JWTResponse.class);

    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public String updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    Class<? extends Masked> maskedClass = getMaskedValue(EMAIL);
    URI otpUpdateUserEmail = composeUpdateUserEmailOTP();
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    UpdateEmailRequestBody updateEmailRequest = UpdateEmailRequestBody
        .builder()
        .email(email)
        .loginId(loginId)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    Masked masked = apiProxy.post(otpUpdateUserEmail, updateEmailRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (deliveryMethod != DeliveryMethod.WHATSAPP && deliveryMethod != DeliveryMethod.SMS) {
      throw ServerCommonException.invalidArgument("Method");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    Class<? extends Masked> maskedClass = getMaskedValue(SMS);
    URI otpUpdateUserPhone = composeUpdateUserPhoneOTP(deliveryMethod);
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    UpdatePhoneRequestBody updatePhoneRequestBody = UpdatePhoneRequestBody
        .builder()
        .phone(phone)
        .loginId(loginId)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    Masked masked = apiProxy.post(otpUpdateUserPhone, updatePhoneRequestBody, maskedClass);
    return masked.getMasked();
  }

  private URI composeUpdateUserPhoneOTP(DeliveryMethod deliveryMethod) {
    return composeURI(OTP_UPDATE_PHONE_LINK, deliveryMethod.getValue());
  }

  private URI composeUpdateUserEmailOTP() {
    return getUri(OTP_UPDATE_EMAIL_LINK);
  }

  private URI composeVerifyCodeURL(DeliveryMethod deliveryMethod) {
    return composeURI(VERIFY_OTP_LINK, deliveryMethod.getValue());
  }

  private URI composeSignUpOrInURL(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_OR_IN_OTP_LINK, deliveryMethod.getValue());
  }

  private SignUpRequest newSignUpRequest(DeliveryMethod deliveryMethod, User user) {
    if (SMS.equals(deliveryMethod)) {
      return SignUpRequest.builder().phone(user.getPhone()).build();
    } else if (WHATSAPP.equals(deliveryMethod)) {
      return SignUpRequest.builder().whatsApp(user.getPhone()).build();
    } else {
      return SignUpRequest.builder().email(user.getEmail()).build();
    }
  }

  private URI composeSignUpURI(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_OTP_LINK, deliveryMethod.getValue());
  }

  private URI composeSignInURL(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_IN_OTP_LINK, deliveryMethod.getValue());
  }
}
