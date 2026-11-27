package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.enums.DeliveryMethod.SMS;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_EMAIL_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_PHONE_MAGIC_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_MAGIC_LINK;
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
import com.descope.model.magiclink.MagicLinkSignInOptions;
import com.descope.model.magiclink.MagicLinkSignUpOptions;
import com.descope.model.magiclink.MagicLinkUpdateOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdateEmailMagicLinkRequest;
import com.descope.model.magiclink.request.UpdateEmailRequest;
import com.descope.model.magiclink.request.UpdatePhoneMagicLinkRequest;
import com.descope.model.magiclink.request.UpdatePhoneRequest;
import com.descope.model.magiclink.request.VerifyRequest;
import com.descope.model.magiclink.response.Masked;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class MagicLinkServiceImpl extends AuthenticationServiceImpl implements MagicLinkService {

  MagicLinkServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String signIn(
      DeliveryMethod deliveryMethod,
      String loginId,
      String uri,
      String token,
      LoginOptions loginOptions)
      throws DescopeException {
    return signIn(deliveryMethod, loginId, uri, token, loginOptions, null);
  }

  @Override
  public String signIn(
      DeliveryMethod deliveryMethod,
      String loginId,
      String uri,
      String token,
      LoginOptions loginOptions,
      MagicLinkSignInOptions signInOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    ApiProxy apiProxy;
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignInURL = composeMagicLinkSignInURI(deliveryMethod);
    SignInRequest signInRequest = buildSignInRequest(uri, loginId, loginOptions, signInOptions);
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(token)) {
        throw ServerCommonException.invalidArgument("token");
      }
      apiProxy = getApiProxy(token);
    } else {
      apiProxy = getApiProxy();
    }
    Masked masked = apiProxy.post(magicLinkSignInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user) {
    return signUp(deliveryMethod, loginId, uri, user, null);
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user,
      SignUpOptions signupOptions) throws DescopeException {
    return signUp(deliveryMethod, loginId, uri, user, signupOptions, null);
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user,
      SignUpOptions signupOptions, MagicLinkSignUpOptions magicLinkSignUpOptions) throws DescopeException {
    if (user == null) {
      user = new User();
    }

    verifyDeliveryMethod(deliveryMethod, loginId, user);
    final Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    final URI magicLinkSignUpURL = composeMagicLinkSignUpURI(deliveryMethod);

    SignUpRequest.SignUpRequestBuilder signUpRequestBuilder = SignUpRequest.builder().loginId(loginId).uri(uri);
    if (EMAIL.equals(deliveryMethod)) {
      signUpRequestBuilder.email(user.getEmail());
    }
    if (signupOptions != null) {
      signUpRequestBuilder.loginOptions(signupOptions);
    }
    // With options the body uses the spec's redirectUrl key
    if (magicLinkSignUpOptions != null) {
      signUpRequestBuilder.uri(null).redirectUrl(uri)
          .providerId(magicLinkSignUpOptions.getProviderId())
          .templateId(magicLinkSignUpOptions.getTemplateId());
    }
    SignUpRequest signUpRequest = signUpRequestBuilder.user(user).build();
    ApiProxy apiProxy = getApiProxy();
    Masked masked = apiProxy.post(magicLinkSignUpURL, signUpRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public AuthenticationInfo verify(String token) {
    URI verifyMagicLinkURL = composeVerifyMagicLinkURL();
    VerifyRequest verifyRequest = new VerifyRequest(token);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(verifyMagicLinkURL, verifyRequest, JWTResponse.class);

    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId, String uri)
      throws DescopeException {
    return signUpOrIn(deliveryMethod, loginId, uri, null);
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId, String uri,
      MagicLinkSignInOptions signInOptions) throws DescopeException {

    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignUpOrInURL = composeMagicLinkSignUpOrInURI(deliveryMethod);
    SignInRequest signInRequest = buildSignInRequest(uri, loginId, null, signInOptions);

    ApiProxy apiProxy = getApiProxy();
    Masked masked = apiProxy.post(magicLinkSignUpOrInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String updateUserEmail(String loginId, String email, String uri, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserEmail(loginId, email, uri, refreshToken, updateOptions, null);
  }

  @Override
  public String updateUserEmail(String loginId, String email, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    return updateUserEmail(loginId, email, uri, refreshToken, updateOptions, templateOptions, null);
  }

  @Override
  public String updateUserEmail(String loginId, String email, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, MagicLinkUpdateOptions magicLinkUpdateOptions)
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
    URI magicLinkUpdateUserEmail = composeUpdateUserEmailMagiclink();
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    Object updateEmailRequest;
    if (magicLinkUpdateOptions == null) {
      updateEmailRequest =
          UpdateEmailRequest.builder()
              .email(email)
              .uri(uri)
              .loginId(loginId)
              .crossDevice(false)
              .addToLoginIds(updateOptions.isAddToLoginIds())
              .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
              .templateOptions(templateOptions)
              .build();
    } else {
      updateEmailRequest =
          UpdateEmailMagicLinkRequest.builder()
              .email(email)
              .redirectUrl(uri)
              .loginId(loginId)
              .addToLoginIds(updateOptions.isAddToLoginIds())
              .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
              .templateOptions(templateOptions)
              .providerId(magicLinkUpdateOptions.getProviderId())
              .templateId(magicLinkUpdateOptions.getTemplateId())
              .locale(magicLinkUpdateOptions.getLocale())
              .failOnConflict(magicLinkUpdateOptions.getFailOnConflict())
              .build();
    }

    ApiProxy apiProxy = getApiProxy(refreshToken);
    Masked masked = apiProxy.post(magicLinkUpdateUserEmail, updateEmailRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String updateUserPhone(
      DeliveryMethod deliveryMethod, String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserPhone(deliveryMethod, loginId, phone, uri, refreshToken, updateOptions, null);
  }

  @Override
  public String updateUserPhone(
      DeliveryMethod deliveryMethod, String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    return updateUserPhone(deliveryMethod, loginId, phone, uri, refreshToken, updateOptions, templateOptions, null);
  }

  @Override
  public String updateUserPhone(
      DeliveryMethod deliveryMethod, String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, MagicLinkUpdateOptions magicLinkUpdateOptions)
      throws DescopeException {

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
    URI magicLinkUpdateUserPhone = composeUpdateUserPhoneMagiclink(deliveryMethod);
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    Object updatePhoneRequest;
    if (magicLinkUpdateOptions == null) {
      updatePhoneRequest =
          UpdatePhoneRequest.builder()
              .phone(phone)
              .uri(uri)
              .loginId(loginId)
              .crossDevice(false)
              .addToLoginIds(updateOptions.isAddToLoginIds())
              .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
              .templateOptions(templateOptions)
              .build();
    } else {
      updatePhoneRequest =
          UpdatePhoneMagicLinkRequest.builder()
              .phone(phone)
              .redirectUrl(uri)
              .loginId(loginId)
              .addToLoginIds(updateOptions.isAddToLoginIds())
              .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
              .templateOptions(templateOptions)
              .providerId(magicLinkUpdateOptions.getProviderId())
              .templateId(magicLinkUpdateOptions.getTemplateId())
              .locale(magicLinkUpdateOptions.getLocale())
              .failOnConflict(magicLinkUpdateOptions.getFailOnConflict())
              .build();
    }

    ApiProxy apiProxy = getApiProxy(refreshToken);
    Masked masked = apiProxy.post(magicLinkUpdateUserPhone, updatePhoneRequest, maskedClass);
    return masked.getMasked();
  }

  // Without options the body keeps its original URI key
  private static SignInRequest buildSignInRequest(String uri, String loginId, LoginOptions loginOptions,
      MagicLinkSignInOptions signInOptions) {
    if (signInOptions == null) {
      return new SignInRequest(uri, loginId, loginOptions);
    }
    return SignInRequest.builder()
        .loginId(loginId)
        .loginOptions(loginOptions)
        .redirectUrl(uri)
        .providerId(signInOptions.getProviderId())
        .templateId(signInOptions.getTemplateId())
        .ssoAppId(signInOptions.getSsoAppId())
        .useRecovery(signInOptions.getUseRecovery())
        .build();
  }

  private URI composeMagicLinkSignInURI(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_IN_MAGIC_LINK, deliveryMethod.getValue());
  }

  private URI composeMagicLinkSignUpURI(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_MAGIC_LINK, deliveryMethod.getValue());
  }

  private URI composeVerifyMagicLinkURL() {
    return getUri(VERIFY_MAGIC_LINK);
  }

  private URI composeMagicLinkSignUpOrInURI(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_OR_IN_MAGIC_LINK, deliveryMethod.getValue());
  }

  private URI composeUpdateUserEmailMagiclink() {
    return getUri(UPDATE_EMAIL_MAGIC_LINK);
  }

  private URI composeUpdateUserPhoneMagiclink(DeliveryMethod deliveryMethod) {
    return composeURI(UPDATE_USER_PHONE_MAGIC_LINK, deliveryMethod.getValue());
  }
}
