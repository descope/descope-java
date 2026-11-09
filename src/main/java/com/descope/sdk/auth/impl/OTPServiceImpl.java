package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.enums.DeliveryMethod.SMS;
import static com.descope.enums.DeliveryMethod.VOICE;
import static com.descope.enums.DeliveryMethod.WHATSAPP;
import static com.descope.literals.Routes.AuthEndPoints.OTP_UPDATE_EMAIL_LINK;
import static com.descope.literals.Routes.AuthEndPoints.OTP_UPDATE_PHONE_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_OTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_OTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_OTP_LINK;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;
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
import com.descope.model.otp.OTPResponse;
import com.descope.model.otp.OTPSignInOptions;
import com.descope.model.otp.OTPSignUpOptions;
import com.descope.model.otp.OTPUpdateOptions;
import com.descope.model.otp.SignInRequest;
import com.descope.model.otp.SignUpRequest;
import com.descope.model.otp.UpdateEmailRequestBody;
import com.descope.model.otp.UpdatePhoneRequestBody;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OTPService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class OTPServiceImpl extends AuthenticationServiceImpl implements OTPService {

  private static final String IM_METHOD = "im";

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
  public OTPResponse signIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions,
      String refreshToken, OTPSignInOptions signInOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    return signInWith(otpMethod(deliveryMethod), loginId, loginOptions, refreshToken, signInOptions);
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
  public OTPResponse signUp(DeliveryMethod deliveryMethod, String loginId, User user, SignUpOptions signUpOptions,
      OTPSignUpOptions otpSignUpOptions) throws DescopeException {
    String method = otpMethod(deliveryMethod);
    return signUpWith(method, EMAIL.equals(deliveryMethod), loginId, user, signUpOptions, otpSignUpOptions);
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
  public OTPResponse signUpOrIn(DeliveryMethod deliveryMethod, String loginId, LoginOptions loginOptions,
      OTPSignInOptions signInOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    return signUpOrInWith(otpMethod(deliveryMethod), loginId, loginOptions, signInOptions);
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
        .templateOptions(templateOptions)
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    Masked masked = apiProxy.post(otpUpdateUserEmail, updateEmailRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public OTPResponse updateUserEmail(String loginId, String email, String refreshToken, UpdateOptions updateOptions,
      Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
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
        .locale(otpUpdateOptions.getLocale())
        .failOnConflict(otpUpdateOptions.getFailOnConflict())
        .mfa(otpUpdateOptions.getMfa())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(composeUpdateUserEmailOTP(), updateEmailRequest, OTPResponse.class);
  }

  @Override
  public String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserPhone(deliveryMethod, loginId, phone, refreshToken, updateOptions, null);
  }

  @Override
  public String updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (deliveryMethod != DeliveryMethod.WHATSAPP && deliveryMethod != DeliveryMethod.SMS
        && deliveryMethod != DeliveryMethod.VOICE) {
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
        .templateOptions(templateOptions)
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    Masked masked = apiProxy.post(otpUpdateUserPhone, updatePhoneRequestBody, maskedClass);
    return masked.getMasked();
  }

  @Override
  public OTPResponse updateUserPhone(DeliveryMethod deliveryMethod, String loginId, String phone,
      String refreshToken, UpdateOptions updateOptions, Map<String, String> templateOptions,
      OTPUpdateOptions otpUpdateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (deliveryMethod != SMS && deliveryMethod != VOICE) {
      throw ServerCommonException.invalidArgument("Method");
    }
    return updateUserPhoneWith(deliveryMethod.getValue(), loginId, phone, refreshToken, updateOptions,
        templateOptions, otpUpdateOptions);
  }

  @Override
  public OTPResponse signInIM(String loginId, LoginOptions loginOptions, String refreshToken,
      OTPSignInOptions signInOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    return signInWith(IM_METHOD, loginId, loginOptions, refreshToken, signInOptions);
  }

  @Override
  public OTPResponse signUpIM(String loginId, User user, SignUpOptions signUpOptions,
      OTPSignUpOptions otpSignUpOptions) throws DescopeException {
    return signUpWith(IM_METHOD, false, loginId, user, signUpOptions, otpSignUpOptions);
  }

  @Override
  public OTPResponse signUpOrInIM(String loginId, LoginOptions loginOptions, OTPSignInOptions signInOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    return signUpOrInWith(IM_METHOD, loginId, loginOptions, signInOptions);
  }

  @Override
  public AuthenticationInfo verifyCodeIM(String loginId, String code) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    AuthenticationVerifyRequestBody authenticationVerifyRequestBody =
        new AuthenticationVerifyRequestBody(loginId, code, null);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(composeURI(VERIFY_OTP_LINK, IM_METHOD), authenticationVerifyRequestBody, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public OTPResponse updateUserPhoneIM(String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    return updateUserPhoneWith(IM_METHOD, loginId, phone, refreshToken, updateOptions, templateOptions,
        otpUpdateOptions);
  }

  // The OTP routes in the API spec, WhatsApp has none
  private String otpMethod(DeliveryMethod deliveryMethod) {
    if (deliveryMethod != EMAIL && deliveryMethod != SMS && deliveryMethod != VOICE) {
      throw ServerCommonException.invalidArgument("Method");
    }
    return deliveryMethod.getValue();
  }

  private OTPResponse signInWith(String method, String loginId, LoginOptions loginOptions, String refreshToken,
      OTPSignInOptions signInOptions) {
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    Map<String, Object> request = newSignInRequest(loginId, loginOptions, signInOptions);
    return apiProxy.post(composeURI(SIGN_IN_OTP_LINK, method), request, OTPResponse.class);
  }

  private OTPResponse signUpWith(String method, boolean byEmail, String loginId, User user,
      SignUpOptions signUpOptions, OTPSignUpOptions otpSignUpOptions) {
    if (user == null) {
      user = new User();
    }
    verifyDeliveryMethod(byEmail ? EMAIL : SMS, loginId, user);
    // The spec's sign-up user has only the name and the other address
    Map<String, Object> signUpUser = new HashMap<>();
    addIfNotNull(signUpUser, "name", user.getName());
    addIfNotNull(signUpUser, byEmail ? "phone" : "email", byEmail ? user.getPhone() : user.getEmail());
    Map<String, Object> request = mapOf("loginId", loginId, "user", signUpUser);
    addIfNotNull(request, byEmail ? "email" : "phone",
        byEmail ? user.getEmail() : StringUtils.defaultIfBlank(user.getPhone(), loginId));
    if (signUpOptions != null) {
      Map<String, Object> options = new HashMap<>();
      addIfNotNull(options, "customClaims", signUpOptions.getCustomClaims());
      addIfNotNull(options, "templateOptions", signUpOptions.getTemplateOptions());
      request.put("loginOptions", options);
    }
    if (otpSignUpOptions != null) {
      addIfNotNull(request, "providerId", otpSignUpOptions.getProviderId());
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(composeURI(SIGN_UP_OTP_LINK, method), request, OTPResponse.class);
  }

  private OTPResponse signUpOrInWith(String method, String loginId, LoginOptions loginOptions,
      OTPSignInOptions signInOptions) {
    Map<String, Object> request = newSignInRequest(loginId, loginOptions, signInOptions);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(composeURI(SIGN_UP_OR_IN_OTP_LINK, method), request, OTPResponse.class);
  }

  private OTPResponse updateUserPhoneWith(String method, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, OTPUpdateOptions otpUpdateOptions) {
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
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
        .locale(otpUpdateOptions.getLocale())
        .failOnConflict(otpUpdateOptions.getFailOnConflict())
        .mfa(otpUpdateOptions.getMfa())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(composeURI(OTP_UPDATE_PHONE_LINK, method), updatePhoneRequestBody, OTPResponse.class);
  }

  private Map<String, Object> newSignInRequest(String loginId, LoginOptions loginOptions,
      OTPSignInOptions options) {
    Map<String, Object> request = mapOf("loginId", loginId);
    if (loginOptions != null) {
      // Only the spec's loginOptions fields
      Map<String, Object> spec = new HashMap<>();
      spec.put("stepup", loginOptions.isStepup());
      spec.put("mfa", loginOptions.isMfa());
      addIfNotNull(spec, "customClaims", loginOptions.getCustomClaims());
      addIfNotNull(spec, "templateOptions", loginOptions.getTemplateOptions());
      addIfNotNull(spec, "locale", loginOptions.getLocale());
      spec.put("revokeOtherSessions", loginOptions.isRevokeOtherSessions());
      addIfNotNull(spec, "revokeOtherSessionsTypes", loginOptions.getRevokeOtherSessionsTypes());
      request.put("loginOptions", spec);
    }
    if (options != null) {
      addIfNotNull(request, "providerId", options.getProviderId());
      addIfNotNull(request, "ssoAppId", options.getSsoAppId());
      addIfNotNull(request, "useRecovery", options.getUseRecovery());
    }
    return request;
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
    if (SMS.equals(deliveryMethod) || VOICE.equals(deliveryMethod)) {
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
