package com.descope.sdk.auth.impl;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.EnchantedLinkSessionBody;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdateEmailRequest;
import com.descope.model.magiclink.request.VerifyRequest;
import com.descope.model.magiclink.response.Masked;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.EnchantedLinkService;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Objects;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.literals.Routes.AuthEndPoints.ENCHANTED_LINK_SESSION;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_EMAIL_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_ENCHANTED_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static org.apache.logging.log4j.util.Strings.isEmpty;

class EnchantedLinkServiceImpl extends AuthenticationServiceImpl
    implements EnchantedLinkService {


  EnchantedLinkServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public EnchantedLinkResponse signIn(String loginId, String uri, LoginOptions loginOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI enchantedLink = composeEnchantedLinkSignInURL();
    var signInRequest = new SignInRequest(uri, loginId, loginOptions);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(enchantedLink, signInRequest, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse signUp(String loginId, String uri, User user) throws DescopeException {
    if (Objects.isNull(user)) {
      user = new User();
    }

    URI enchantedLinkSignUpURL = composeEnchantedLinkSignUpURL();

    var signUpRequestBuilder = SignUpRequest.builder().loginId(loginId).uri(uri).user(user).email(loginId);
    if (isEmpty(user.getEmail())) {
      user.setEmail(loginId);
    }

    var signUpRequest = signUpRequestBuilder.user(user).build();
    var apiProxy = getApiProxy();
    return apiProxy.post(enchantedLinkSignUpURL, signUpRequest, EnchantedLinkResponse.class);
  }


  @Override
  public EnchantedLinkResponse signUpOrIn(String loginId, String uri) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    URI magicLinkSignUpOrInURL = composeEnchantedLinkSignUpOrInURL();
    var signInRequest = new SignInRequest(uri, loginId, null);

    var apiProxy = getApiProxy();
    return apiProxy.post(magicLinkSignUpOrInURL, signInRequest, EnchantedLinkResponse.class);

  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    //TODO - Functional testing is pending same is not working on decope site also
    URI getSessionURL = composeGetSession();
    var apiProxy = getApiProxy();
    var jwtResponse = apiProxy.post(getSessionURL, EnchantedLinkSessionBody.builder().pendingRef(pendingRef).build(), JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public void verify(String token) throws DescopeException {
    URI verifyEnchantedLinkURL = composeVerifyEnchantedLinkURL();
    var verifyRequest = new VerifyRequest(token);
    var apiProxy = getApiProxy();
    apiProxy.post(verifyEnchantedLinkURL, verifyRequest, EmptyResponse.class);
  }

  @Override
  public String updateUserEmail(String loginId, String email, String uri) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
      throw ServerCommonException.invalidArgument("Email");
    }
    Class<? extends Masked> maskedClass = getMaskedValue(EMAIL);
    URI magicLinkUpdateUserEmail = composeUpdateUserEmailEnchantedLink();
    UpdateEmailRequest updateEmailRequest = UpdateEmailRequest.builder()
        .email(email)
        .uri(uri)
        .loginId(loginId)
        .crossDevice(false)
        .build();

    var apiProxy = getApiProxy();
    var masked = apiProxy.post(magicLinkUpdateUserEmail, updateEmailRequest, maskedClass);
    return masked.getMasked();
  }

  private URI composeUpdateUserEmailEnchantedLink() {
    return getUri(UPDATE_EMAIL_ENCHANTED_LINK);
  }

  private URI composeEnchantedLinkSignInURL() {
    return composeURI(SIGN_IN_ENCHANTED_LINK, EMAIL.getValue());
  }

  private URI composeEnchantedLinkSignUpURL() {
    return composeURI(SIGN_UP_ENCHANTED_LINK, EMAIL.getValue());
  }

  private URI composeEnchantedLinkSignUpOrInURL() {
    return composeURI(SIGN_UP_OR_IN_ENCHANTED_LINK, EMAIL.getValue());
  }

  private URI composeVerifyEnchantedLinkURL() {
    return getUri(VERIFY_ENCHANTED_LINK);
  }

  private URI composeGetSession() {
    return getUri(ENCHANTED_LINK_SESSION);
  }

}
