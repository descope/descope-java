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
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdateEmailRequest;
import com.descope.model.magiclink.request.UpdatePhoneRequest;
import com.descope.model.magiclink.request.VerifyRequest;
import com.descope.model.magiclink.response.Masked;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.MagicLinkService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

class MagicLinkServiceImpl extends AuthenticationServiceImpl
    implements MagicLinkService {

  MagicLinkServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String signIn(
      DeliveryMethod deliveryMethod,
      String loginId,
      String uri,
      HttpRequest request,
      LoginOptions loginOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    ApiProxy apiProxy;
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignInURL = composeMagicLinkSignInURI(deliveryMethod);
    var signInRequest = new SignInRequest(uri, loginId, loginOptions);
    if (JwtUtils.isJWTRequired(loginOptions)) {
      var pwd = getValidRefreshToken(request);
      apiProxy = getApiProxy(pwd);
    } else {
      apiProxy = getApiProxy();
    }
    var masked = apiProxy.post(magicLinkSignInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String signUp(DeliveryMethod deliveryMethod, String loginId, String uri, User user)
      throws DescopeException {
    if (Objects.isNull(user)) {
      user = new User();
    }

    verifyDeliveryMethod(deliveryMethod, loginId, user);
    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignUpURL = composeMagicLinkSignUpURI(deliveryMethod);

    var signUpRequestBuilder = SignUpRequest.builder().loginId(loginId).uri(uri);
    if (EMAIL.equals(deliveryMethod)) {
      signUpRequestBuilder.email(user.getEmail());
    }

    var signUpRequest = signUpRequestBuilder.user(user).build();
    var apiProxy = getApiProxy();
    var masked = apiProxy.post(magicLinkSignUpURL, signUpRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public AuthenticationInfo verify(String token) {
    URI verifyMagicLinkURL = composeVerifyMagicLinkURL();
    var verifyRequest = new VerifyRequest(token);
    var apiProxy = getApiProxy();
    var jwtResponse = apiProxy.post(verifyMagicLinkURL, verifyRequest, JWTResponse.class);

    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public String signUpOrIn(DeliveryMethod deliveryMethod, String loginId, String uri)
      throws DescopeException {

    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    Class<? extends Masked> maskedClass = getMaskedValue(deliveryMethod);
    URI magicLinkSignUpOrInURL = composeMagicLinkSignUpOrInURI(deliveryMethod);
    var signInRequest = new SignInRequest(uri, loginId, null);

    var apiProxy = getApiProxy();
    var masked = apiProxy.post(magicLinkSignUpOrInURL, signInRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String updateUserEmail(String loginId, String email, String uri, HttpRequest request)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }

    String refreshToken = getValidRefreshToken(request);
    Class<? extends Masked> maskedClass = getMaskedValue(EMAIL);
    URI magicLinkUpdateUserEmail = composeUpdateUserEmailMagiclink();
    UpdateEmailRequest updateEmailRequest =
        UpdateEmailRequest.builder()
            .email(email)
            .uri(uri)
            .loginId(loginId)
            .crossDevice(false)
            .build();

    var apiProxy = getApiProxy(refreshToken);
    var masked = apiProxy.post(magicLinkUpdateUserEmail, updateEmailRequest, maskedClass);
    return masked.getMasked();
  }

  @Override
  public String updateUserPhone(
      DeliveryMethod deliveryMethod, String loginId, String phone, String uri, HttpRequest request)
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

    String refreshToken = getValidRefreshToken(request);
    Class<? extends Masked> maskedClass = getMaskedValue(SMS);
    URI magicLinkUpdateUserPhone = composeUpdateUserPhoneMagiclink(deliveryMethod);
    UpdatePhoneRequest updatePhoneRequest =
        UpdatePhoneRequest.builder()
            .phone(phone)
            .uri(uri)
            .loginId(loginId)
            .crossDevice(false)
            .build();

    var apiProxy = getApiProxy(refreshToken);
    var masked = apiProxy.post(magicLinkUpdateUserPhone, updatePhoneRequest, maskedClass);
    return masked.getMasked();
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
    return composeURI(UPDATE_EMAIL_MAGIC_LINK, EMAIL.getValue());
  }

  private URI composeUpdateUserPhoneMagiclink(DeliveryMethod deliveryMethod) {
    return composeURI(UPDATE_USER_PHONE_MAGIC_LINK, deliveryMethod.getValue());
  }
}
