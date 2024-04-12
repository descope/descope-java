package com.descope.sdk.auth.impl;

import static com.descope.enums.DeliveryMethod.EMAIL;
import static com.descope.literals.Routes.AuthEndPoints.ENCHANTED_LINK_SESSION;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_EMAIL_ENCHANTED_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_ENCHANTED_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EmptyResponse;
import com.descope.model.enchantedlink.EnchantedLinkResponse;
import com.descope.model.enchantedlink.EnchantedLinkSessionBody;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.magiclink.request.SignInRequest;
import com.descope.model.magiclink.request.SignUpRequest;
import com.descope.model.magiclink.request.UpdateEmailRequest;
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
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI enchantedLink = composeEnchantedLinkSignInURL();
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
    return apiProxy.post(enchantedLink, signInRequest, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse signUp(String loginId, String uri, User user)
      throws DescopeException {
    return signUp(loginId, uri, user, null);
  }

  @Override
  public EnchantedLinkResponse signUp(String loginId, String uri, User user, SignUpOptions signupOptions)
      throws DescopeException {
    if (user == null) {
      user = new User();
    }
    URI enchantedLinkSignUpURL = composeEnchantedLinkSignUpURL();
    SignUpRequest.SignUpRequestBuilder signUpRequestBuilder =
        SignUpRequest.builder().loginId(loginId).uri(uri).user(user).email(loginId);
    if (StringUtils.isBlank(user.getEmail())) {
      user.setEmail(loginId);
    }
    if (signupOptions != null) {
      signUpRequestBuilder.loginOptions(signupOptions);
    }
    SignUpRequest signUpRequest = signUpRequestBuilder.user(user).build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(enchantedLinkSignUpURL, signUpRequest, EnchantedLinkResponse.class);
  }

  @Override
  public EnchantedLinkResponse signUpOrIn(String loginId, String uri) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI magicLinkSignUpOrInURL = composeEnchantedLinkSignUpOrInURL();
    SignInRequest signInRequest = new SignInRequest(uri, loginId, null);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(magicLinkSignUpOrInURL, signInRequest, EnchantedLinkResponse.class);
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
