package com.descope.sdk.auth.impl;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.Masked;
import com.descope.model.otp.*;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OTPService;
import com.descope.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

import static com.descope.enums.DeliveryMethod.*;
import static com.descope.literals.Routes.AuthEndPoints.*;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;
import static java.util.Objects.isNull;

class OTPServiceImpl extends AuthenticationServiceImpl
    implements OTPService {

  OTPServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    ApiProxy apiProxy;
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI otpSignInURL = composeSignInURL(deliveryMethod);
    var signInRequest = new SignInRequest(loginId, loginOptions);
    if (JwtUtils.isJWTRequired(loginOptions)) {
      var pwd = "";//getValidRefreshToken(request);
      apiProxy = getApiProxy(pwd);
    } else {
      apiProxy = getApiProxy();
    }
    var masked = apiProxy.post(otpSignInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, User user) throws DescopeException {
    if (isNull(user)) {
      user = new User();
    }
    verifyDeliveryMethod(deliveryMethod, loginId, user);
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI otpSignUpURL = composeSignUpURI(deliveryMethod);

    var signUpRequest = newSignUpRequest(deliveryMethod, user);
    signUpRequest.setLoginId(loginId);
    signUpRequest.setUser(user);

    var apiProxy = getApiProxy();
    var masked = apiProxy.post(otpSignUpURL, signUpRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId) throws DescopeException {

    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI otpSignUpOrInURL = composeSignUpOrInURL(deliveryMethod);
    var signInRequest = new SignInRequest(loginId, null);
    var apiProxy = getApiProxy();
    var masked = apiProxy.post(otpSignUpOrInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public AuthenticationInfo verifyCode(DeliveryMethod deliveryMethod, String loginId, String code) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (PHONE_PATTERN.matcher(loginId).matches()) {
      deliveryMethod = SMS;
    } else if (EMAIL_PATTERN.matcher(loginId).matches()) {
      deliveryMethod = EMAIL;
    }
    if (isNull(deliveryMethod)) {
      throw ServerCommonException.invalidArgument("Method");
    }
    var authenticationVerifyRequestBody = new AuthenticationVerifyRequestBody(loginId, code);
    URI otpVerifyCode = composeVerifyCodeURL(deliveryMethod);
    var apiProxy = getApiProxy();
    var jwtResponse = apiProxy.post(otpVerifyCode, authenticationVerifyRequestBody, JWTResponse.class);

    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public String updateUserEmail(String loginId, String email) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }

    //TODO - Need to check on UpdateOptions which got recently added on go lang
    Class<? extends Masked> maskedClass = getMaskedValue(EMAIL);
    URI otpUpdateUserEmail = composeUpdateUserEmailOTP();

    var updateEmailRequest = new UpdateEmailRequestBody(email, loginId);

    var apiProxy = getApiProxy();
    var masked = apiProxy.post(otpUpdateUserEmail, updateEmailRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (deliveryMethod != DeliveryMethod.WHATSAPP && deliveryMethod != DeliveryMethod.SMS) {
      throw ServerCommonException.invalidArgument("Method");
    }
    Class<? extends Masked> maskedClass = getMaskedValue(SMS);
    URI otpUpdateUserPhone = composeUpdateUserPhoneOTP(deliveryMethod);
    var updatePhoneRequestBody = new UpdatePhoneRequestBody(phone, loginId);
    var apiProxy = getApiProxy();
    var masked = apiProxy.post(otpUpdateUserPhone, updatePhoneRequestBody, maskedClass);
    return masked.getMasked();
  }

  private URI composeUpdateUserPhoneOTP(DeliveryMethod deliveryMethod) {
    return composeURI(OTP_UPDATE_PHONE, deliveryMethod.getValue());
  }

  private URI composeUpdateUserEmailOTP() {
    return getUri(OTP_UPDATE_EMAIL);

  }

  private URI composeVerifyCodeURL(DeliveryMethod deliveryMethod) {
    return composeURI(VERIFY_CODE, deliveryMethod.getValue());
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
