package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.enums.DeliveryMethod.SMS;
import static com.descope.literals.Routes.AuthEndPoints.ENCHANTED_LINK_SESSION;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_EMAIL_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_PHONE_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_ENCHANTED_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.EnchantedLinkSessionBody;
import com.descope.model.enchantedlink.EnchantedLinkSignInOptions;
import com.descope.model.enchantedlink.EnchantedLinkSignInRequest;
import com.descope.model.enchantedlink.EnchantedLinkSignUpOptions;
import com.descope.model.enchantedlink.EnchantedLinkSignUpRequest;
import com.descope.model.enchantedlink.EnchantedLinkUpdateOptions;
import com.descope.model.enchantedlink.EnchantedLinkUpdateRequest;
import com.descope.model.enchantedlink.EnchantedLinkVerifyRequest;
import com.descope.model.enchantedlink.PhoneEnchantedLinkResponse;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdateEmailRequest;
import com.descope.model.magiclink.request.UpdatePhoneRequest;
import com.descope.model.magiclink.request.VerifyRequest;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.EnchantedLinkService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class EnchantedLinkServiceImpl extends AuthenticationServiceImpl implements EnchantedLinkService {

  EnchantedLinkServiceImpl(Client client) {
    super(client);
  }

  @Override
  public EnchantedLinkResponse signIn(String loginId, String uri, String token, LoginOptions loginOptions)
      throws DescopeException {
    return signInByDeliveryMethod(EMAIL, loginId, uri, token, loginOptions, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse signIn(String loginId, String uri, String token, LoginOptions loginOptions,
      EnchantedLinkSignInOptions options) throws DescopeException {
    return signInWithOptions(composeEnchantedLinkSignInURL(EMAIL), loginId, uri, token, loginOptions, options,
        EnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse signInWithPhone(String loginId, String uri, String token,
      LoginOptions loginOptions) throws DescopeException {
    return signInByDeliveryMethod(SMS, loginId, uri, token, loginOptions, PhoneEnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse signInWithPhone(String loginId, String uri, String token,
      LoginOptions loginOptions, EnchantedLinkSignInOptions options) throws DescopeException {
    return signInWithOptions(composeEnchantedLinkSignInURL(SMS), loginId, uri, token, loginOptions, options,
        PhoneEnchantedLinkResponse.class);
  }

  private <R> R signInWithOptions(URI url, String loginId, String uri, String token, LoginOptions loginOptions,
      EnchantedLinkSignInOptions options, Class<R> responseClass) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(token)) {
        throw ServerCommonException.invalidArgument("token");
      }
      apiProxy = getApiProxy(token);
    } else {
      apiProxy = getApiProxy();
    }
    if (options == null) {
      options = new EnchantedLinkSignInOptions();
    }
    EnchantedLinkSignInRequest request = EnchantedLinkSignInRequest.builder()
        .loginId(loginId)
        .redirectUrl(uri)
        .loginOptions(loginOptions)
        .providerId(options.getProviderId())
        .templateId(options.getTemplateId())
        .ssoAppId(options.getSsoAppId())
        .useRecovery(options.getUseRecovery())
        .build();
    return apiProxy.post(url, request, responseClass);
  }

  private <R> R signInByDeliveryMethod(DeliveryMethod deliveryMethod, String loginId, String uri, String token,
      LoginOptions loginOptions, Class<R> responseClass) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI enchantedLink = composeEnchantedLinkSignInURL(deliveryMethod);
    SignInRequest signInRequest = new SignInRequest(uri, loginId, loginOptions);
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(token)) {
        throw ServerCommonException.invalidArgument("token");
      }
      apiProxy = getApiProxy(token);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(enchantedLink, signInRequest, responseClass);
  }

  @Override
  public EnchantedLinkResponse signUp(String loginId, String uri, User user)
      throws DescopeException {
    return signUp(loginId, uri, user, null);
  }

  @Override
  public EnchantedLinkResponse signUp(String loginId, String uri, User user, SignUpOptions signupOptions)
      throws DescopeException {
    return signUpByDeliveryMethod(EMAIL, loginId, uri, user, signupOptions, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse signUp(String loginId, String uri, User user, SignUpOptions signupOptions,
      EnchantedLinkSignUpOptions options) throws DescopeException {
    return signUpWithOptions(EMAIL, loginId, uri, user, signupOptions, options, EnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse signUpWithPhone(String loginId, String uri, User user)
      throws DescopeException {
    return signUpWithPhone(loginId, uri, user, null);
  }

  @Override
  public PhoneEnchantedLinkResponse signUpWithPhone(String loginId, String uri, User user,
      SignUpOptions signupOptions) throws DescopeException {
    return signUpByDeliveryMethod(SMS, loginId, uri, user, signupOptions, PhoneEnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse signUpWithPhone(String loginId, String uri, User user,
      SignUpOptions signupOptions, EnchantedLinkSignUpOptions options) throws DescopeException {
    return signUpWithOptions(SMS, loginId, uri, user, signupOptions, options, PhoneEnchantedLinkResponse.class);
  }

  private <R> R signUpWithOptions(DeliveryMethod deliveryMethod, String loginId, String uri, User user,
      SignUpOptions signupOptions, EnchantedLinkSignUpOptions options, Class<R> responseClass)
      throws DescopeException {
    if (user == null) {
      user = new User();
    }
    if (options == null) {
      options = new EnchantedLinkSignUpOptions();
    }
    EnchantedLinkSignUpRequest.EnchantedLinkSignUpRequestBuilder builder = EnchantedLinkSignUpRequest.builder()
        .loginId(loginId)
        .redirectUrl(uri)
        .loginOptions(signupOptions)
        .providerId(options.getProviderId())
        .templateId(options.getTemplateId());
    if (deliveryMethod == SMS) {
      builder.phone(loginId);
      if (StringUtils.isBlank(user.getPhone())) {
        user.setPhone(loginId);
      }
    } else {
      builder.email(loginId);
      if (StringUtils.isBlank(user.getEmail())) {
        user.setEmail(loginId);
      }
    }
    EnchantedLinkSignUpRequest request = builder.user(user).build();
    return getApiProxy().post(composeEnchantedLinkSignUpURL(deliveryMethod), request, responseClass);
  }

  private <R> R signUpByDeliveryMethod(DeliveryMethod deliveryMethod, String loginId, String uri, User user,
      SignUpOptions signupOptions, Class<R> responseClass) throws DescopeException {
    if (user == null) {
      user = new User();
    }
    URI enchantedLinkSignUpURL = composeEnchantedLinkSignUpURL(deliveryMethod);
    SignUpRequest.SignUpRequestBuilder signUpRequestBuilder =
        SignUpRequest.builder().loginId(loginId).uri(uri);
    if (deliveryMethod == SMS) {
      signUpRequestBuilder.phone(loginId);
      if (StringUtils.isBlank(user.getPhone())) {
        user.setPhone(loginId);
      }
    } else {
      signUpRequestBuilder.email(loginId);
      if (StringUtils.isBlank(user.getEmail())) {
        user.setEmail(loginId);
      }
    }
    if (signupOptions != null) {
      signUpRequestBuilder.loginOptions(signupOptions);
    }
    SignUpRequest signUpRequest = signUpRequestBuilder.user(user).build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(enchantedLinkSignUpURL, signUpRequest, responseClass);
  }

  @Override
  public EnchantedLinkResponse signUpOrIn(String loginId, String uri) throws DescopeException {
    return signUpOrInByDeliveryMethod(EMAIL, loginId, uri, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse signUpOrIn(String loginId, String uri, String token, LoginOptions loginOptions,
      EnchantedLinkSignInOptions options) throws DescopeException {
    return signInWithOptions(composeEnchantedLinkSignUpOrInURL(EMAIL), loginId, uri, token, loginOptions, options,
        EnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse signUpOrInWithPhone(String loginId, String uri) throws DescopeException {
    return signUpOrInByDeliveryMethod(SMS, loginId, uri, PhoneEnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse signUpOrInWithPhone(String loginId, String uri, String token,
      LoginOptions loginOptions, EnchantedLinkSignInOptions options) throws DescopeException {
    return signInWithOptions(composeEnchantedLinkSignUpOrInURL(SMS), loginId, uri, token, loginOptions, options,
        PhoneEnchantedLinkResponse.class);
  }

  private <R> R signUpOrInByDeliveryMethod(DeliveryMethod deliveryMethod, String loginId, String uri,
      Class<R> responseClass) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI enchantedLinkSignUpOrInURL = composeEnchantedLinkSignUpOrInURL(deliveryMethod);
    SignInRequest signInRequest = new SignInRequest(uri, loginId, null);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(enchantedLinkSignUpOrInURL, signInRequest, responseClass);
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    URI getSessionURL = composeGetSession();
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(
            getSessionURL,
            EnchantedLinkSessionBody.builder().pendingRef(pendingRef).build(),
            JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public void verify(String token) throws DescopeException {
    URI verifyEnchantedLinkURL = composeVerifyEnchantedLinkURL();
    VerifyRequest verifyRequest = new VerifyRequest(token);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(verifyEnchantedLinkURL, verifyRequest, EmptyResponse.class);
  }

  @Override
  public void verify(String token, String pkceVerifier) throws DescopeException {
    EnchantedLinkVerifyRequest verifyRequest = new EnchantedLinkVerifyRequest(token, pkceVerifier);
    getApiProxy().post(composeVerifyEnchantedLinkURL(), verifyRequest, EmptyResponse.class);
  }

  @Override
  public EnchantedLinkResponse updateUserEmail(String loginId, String email, String uri, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserEmail(loginId, email, uri, refreshToken, updateOptions, null);
  }

  @Override
  public EnchantedLinkResponse updateUserEmail(String loginId, String email, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    URI magicLinkUpdateUserEmail = composeUpdateUserEmailEnchantedLink();
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    UpdateEmailRequest updateEmailRequest =
        UpdateEmailRequest.builder()
            .email(email)
            .uri(uri)
            .loginId(loginId)
            .crossDevice(false)
            .addToLoginIds(updateOptions.isAddToLoginIds())
            .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
            .templateOptions(templateOptions)
            .build();

    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(magicLinkUpdateUserEmail, updateEmailRequest, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse updateUserEmail(String loginId, String email, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, EnchantedLinkUpdateOptions options)
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
    EnchantedLinkUpdateRequest request = updateRequest(loginId, uri, updateOptions, templateOptions, options);
    request.setEmail(email);
    return getApiProxy(refreshToken).post(composeUpdateUserEmailEnchantedLink(), request,
        EnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse updateUserPhone(String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserPhone(loginId, phone, uri, refreshToken, updateOptions, null);
  }

  @Override
  public PhoneEnchantedLinkResponse updateUserPhone(String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    URI enchantedLinkUpdateUserPhone = composeUpdateUserPhoneEnchantedLink();
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    UpdatePhoneRequest updatePhoneRequest =
        UpdatePhoneRequest.builder()
            .phone(phone)
            .uri(uri)
            .loginId(loginId)
            .crossDevice(false)
            .addToLoginIds(updateOptions.isAddToLoginIds())
            .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
            .templateOptions(templateOptions)
            .build();

    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(enchantedLinkUpdateUserPhone, updatePhoneRequest, PhoneEnchantedLinkResponse.class);
  }

  @Override
  public PhoneEnchantedLinkResponse updateUserPhone(String loginId, String phone, String uri, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions, EnchantedLinkUpdateOptions options)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
      throw ServerCommonException.invalidArgument("Phone");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    EnchantedLinkUpdateRequest request = updateRequest(loginId, uri, updateOptions, templateOptions, options);
    request.setPhone(phone);
    return getApiProxy(refreshToken).post(composeUpdateUserPhoneEnchantedLink(), request,
        PhoneEnchantedLinkResponse.class);
  }

  private static EnchantedLinkUpdateRequest updateRequest(String loginId,
      String uri, UpdateOptions updateOptions, Map<String, String> templateOptions,
      EnchantedLinkUpdateOptions options) {
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    if (options == null) {
      options = new EnchantedLinkUpdateOptions();
    }
    return EnchantedLinkUpdateRequest.builder()
        .loginId(loginId)
        .redirectUrl(uri)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .templateOptions(templateOptions)
        .providerId(options.getProviderId())
        .templateId(options.getTemplateId())
        .locale(options.getLocale())
        .failOnConflict(options.getFailOnConflict())
        .build();
  }

  private URI composeUpdateUserEmailEnchantedLink() {
    return getUri(UPDATE_EMAIL_ENCHANTED_LINK);
  }

  private URI composeUpdateUserPhoneEnchantedLink() {
    return composeURI(UPDATE_PHONE_ENCHANTED_LINK, SMS.getValue());
  }

  private URI composeEnchantedLinkSignInURL(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_IN_ENCHANTED_LINK, deliveryMethod.getValue());
  }

  private URI composeEnchantedLinkSignUpURL(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_ENCHANTED_LINK, deliveryMethod.getValue());
  }

  private URI composeEnchantedLinkSignUpOrInURL(DeliveryMethod deliveryMethod) {
    return composeURI(SIGN_UP_OR_IN_ENCHANTED_LINK, deliveryMethod.getValue());
  }

  private URI composeVerifyEnchantedLinkURL() {
    return getUri(VERIFY_ENCHANTED_LINK);
  }

  private URI composeGetSession() {
    return getUri(ENCHANTED_LINK_SESSION);
  }
}
