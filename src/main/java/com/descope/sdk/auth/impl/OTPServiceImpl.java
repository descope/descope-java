package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.enums.DeliveryMethod.IM;
import static com.descope.enums.DeliveryMethod.SMS;
import static com.descope.enums.DeliveryMethod.VOICE;
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
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.otp.AuthenticationVerifyRequestBody;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
import com.descope.model.otp.SignInRequest;
import com.descope.model.otp.SignUpRequest;
import com.descope.model.otp.UpdateEmailRequestBody;
import com.descope.model.otp.UpdatePhoneRequestBody;
import com.descope.model.otp.response.OTPResponse;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OTPService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Map;
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
    return signIn(deliveryMethod, loginId, loginOptions, refreshToken, null, getMaskedClass(deliveryMethod))
        .getMasked();
  }

  @Override
  public OTPResponse signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions,
      String refreshToken, OTPSignInOptions signInOptions) throws DescopeException {
    return signIn(deliveryMethod, loginId, loginOptions, refreshToken, signInOptions, OTPResponse.class);
  }

  private <T> T signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions,
      String refreshToken, OTPSignInOptions signInOptions, Class<T> responseClass) {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    ApiProxy apiProxy;
    URI otpSignInURL = composeSignInURL(deliveryMethod);
    SignInRequest signInRequest = newSignInRequest(loginId, loginOptions, signInOptions);
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(otpSignInURL, signInRequest, responseClass);
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, User user) {
    return signUp(deliveryMethod, loginId, user, null);
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signUpOptions)
      throws DescopeException {
    return signUp(deliveryMethod, loginId, user, signUpOptions, null, getMaskedClass(deliveryMethod)).getMasked();
  }

  @Override
  public OTPResponse signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signUpOptions,
      OTPSignUpOptions otpSignUpOptions) throws DescopeException {
    return signUp(deliveryMethod, loginId, user, signUpOptions, otpSignUpOptions, OTPResponse.class);
  }

  private <T> T signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signUpOptions,
      OTPSignUpOptions otpSignUpOptions, Class<T> responseClass) {
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
    if (otpSignUpOptions != null) {
      signUpRequest.setProviderId(otpSignUpOptions.getProviderId());
      signUpRequest.setTemplateId(otpSignUpOptions.getTemplateId());
    }
    URI otpSignUpURL = composeSignUpURI(deliveryMethod);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(otpSignUpURL, signUpRequest, responseClass);
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId) throws DescopeException {
    return signUpOrIn(deliveryMethod, loginId, null, getMaskedClass(deliveryMethod)).getMasked();
  }

  @Override
  public OTPResponse signUpOrIn(DeliveryMethod deliveryMethod, String loginId, OTPSignInOptions signInOptions)
      throws DescopeException {
    return signUpOrIn(deliveryMethod, loginId, signInOptions, OTPResponse.class);
  }

  private <T> T signUpOrIn(DeliveryMethod deliveryMethod, String loginId, OTPSignInOptions signInOptions,
      Class<T> responseClass) {

    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    URI otpSignUpOrInURL = composeSignUpOrInURL(deliveryMethod);
    SignInRequest signInRequest = newSignInRequest(loginId, null, signInOptions);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(otpSignUpOrInURL, signInRequest, responseClass);
  }

  @Override
  public AuthenticationInfo verifyCode(DeliveryMethod deliveryMethod, String loginId, String code)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (deliveryMethod == null) {
      if (PHONE_PATTERN.matcher(loginId).matches()) {
        deliveryMethod = SMS;
      } else if (EMAIL_PATTERN.matcher(loginId).matches()) {
        deliveryMethod = EMAIL;
      }
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
    return updateUserEmail(loginId, email, refreshToken, updateOptions, null);
  }

  @Override
  public String updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions,
      Map<String, String> templateOptions) throws DescopeException {
    return updateUserEmail(loginId, email, refreshToken, updateOptions, templateOptions, null,
        MaskedEmailRes.class).getMasked();
  }

  @Override
  public OTPResponse updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions,
      Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions) throws DescopeException {
    return updateUserEmail(loginId, email, refreshToken, updateOptions, templateOptions, otpUpdateOptions,
        OTPResponse.class);
  }

  private <T> T updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions,
      Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions, Class<T> responseClass) {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    URI otpUpdateUserEmail = composeUpdateUserEmailOTP();
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    if (otpUpdateOptions == null) {
      otpUpdateOptions = new OTPUpdateOptions();
    }
    UpdateEmailRequestBody updateEmailRequest = UpdateEmailRequestBody
        .builder()
        .email(email)
        .loginId(loginId)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .templateOptions(templateOptions)
        .providerId(otpUpdateOptions.getProviderId())
        .templateId(otpUpdateOptions.getTemplateId())
        .locale(otpUpdateOptions.getLocale())
        .failOnConflict(otpUpdateOptions.getFailOnConflict())
        .mfa(otpUpdateOptions.getMfa())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(otpUpdateUserEmail, updateEmailRequest, responseClass);
  }

  @Override
  public String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserPhone(deliveryMethod, loginId, phone, refreshToken, updateOptions, null);
  }

  @Override
  public String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    return updateUserPhone(deliveryMethod, loginId, phone, refreshToken, updateOptions, templateOptions, null,
        MaskedPhoneRes.class).getMasked();
  }

  @Override
  public OTPResponse updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone,
      String refreshToken, UpdateOptions updateOptions, Map<String, String> templateOptions,
      OTPUpdateOptions otpUpdateOptions) throws DescopeException {
    return updateUserPhone(deliveryMethod, loginId, phone, refreshToken, updateOptions, templateOptions,
        otpUpdateOptions, OTPResponse.class);
  }

  private <T> T updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions,
      Class<T> responseClass) {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (deliveryMethod != DeliveryMethod.WHATSAPP && deliveryMethod != DeliveryMethod.SMS
        && deliveryMethod != DeliveryMethod.VOICE && deliveryMethod != IM) {
      throw ServerCommonException.invalidArgument("Method");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    URI otpUpdateUserPhone = composeUpdateUserPhoneOTP(deliveryMethod);
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    if (otpUpdateOptions == null) {
      otpUpdateOptions = new OTPUpdateOptions();
    }
    UpdatePhoneRequestBody updatePhoneRequestBody = UpdatePhoneRequestBody
        .builder()
        .phone(phone)
        .loginId(loginId)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .templateOptions(templateOptions)
        .providerId(otpUpdateOptions.getProviderId())
        .templateId(otpUpdateOptions.getTemplateId())
        .locale(otpUpdateOptions.getLocale())
        .failOnConflict(otpUpdateOptions.getFailOnConflict())
        .mfa(otpUpdateOptions.getMfa())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(otpUpdateUserPhone, updatePhoneRequestBody, responseClass);
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
    if (SMS.equals(deliveryMethod) || VOICE.equals(deliveryMethod) || IM.equals(deliveryMethod)) {
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

  private SignInRequest newSignInRequest(String loginId, LoginOptions loginOptions, OTPSignInOptions signInOptions) {
    SignInRequest signInRequest = new SignInRequest(loginId, loginOptions);
    if (signInOptions != null) {
      signInRequest.setProviderId(signInOptions.getProviderId());
      signInRequest.setTemplateId(signInOptions.getTemplateId());
      signInRequest.setSsoAppId(signInOptions.getSsoAppId());
      signInRequest.setUseRecovery(signInOptions.getUseRecovery());
    }
    return signInRequest;
  }

  private Class<? extends Masked> getMaskedClass(DeliveryMethod deliveryMethod) {
    return EMAIL.equals(deliveryMethod) ? MaskedEmailRes.class : MaskedPhoneRes.class;
  }
}
