package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.NOTP_SESSION_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_NOTP_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.enchantedlink.EnchantedLinkSessionBody;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.notp.NOTPOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.notp.request.NOTPLoginOptions;
import com.descope.model.notp.request.NOTPSignInRequest;
import com.descope.model.notp.request.NOTPSignUpRequest;
import com.descope.model.notp.request.NOTPUpdateUserRequest;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.net.URLEncoder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

class NOTPServiceImpl extends AuthenticationServiceImpl implements NOTPService {

  NOTPServiceImpl(Client client) {
    super(client);
  }

  @Override
  public NOTPResponse signIn(String provider, String loginId, String token, LoginOptions loginOptions,
      NOTPOptions options) throws DescopeException {
    URI signInURL = composeProviderURI(SIGN_IN_NOTP_LINK, provider);
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(token)) {
        throw ServerCommonException.invalidArgument("token");
      }
      apiProxy = getApiProxy(token);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(signInURL, newSignInRequest(provider, loginId, loginOptions, options),
        NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUp(String provider, String loginId, User user, SignUpOptions signUpOptions,
      NOTPOptions options) throws DescopeException {
    final URI signUpURL = composeProviderURI(SIGN_UP_NOTP_LINK, provider);
    if (user == null) {
      user = new User();
    }
    if (StringUtils.isBlank(user.getPhone())) {
      user.setPhone(loginId);
    }
    if (options == null) {
      options = new NOTPOptions();
    }
    NOTPSignUpRequest signUpRequest = NOTPSignUpRequest.builder()
        .provider(provider)
        .phone(user.getPhone())
        .loginId(loginId)
        .user(user)
        .providerId(options.getProviderId())
        .templates(options.getTemplates())
        .ssoAppId(options.getSsoAppId())
        .loginOptions(signUpOptions)
        .build();
    return getApiProxy().post(signUpURL, signUpRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUpOrIn(String provider, String loginId, SignUpOptions signUpOptions,
      NOTPOptions options) throws DescopeException {
    URI signUpOrInURL = composeProviderURI(SIGN_UP_OR_IN_NOTP_LINK, provider);
    LoginOptions loginOptions = null;
    if (signUpOptions != null) {
      loginOptions = LoginOptions.builder()
          .customClaims(signUpOptions.getCustomClaims())
          .templateOptions(signUpOptions.getTemplateOptions())
          .build();
    }
    return getApiProxy().post(signUpOrInURL, newSignInRequest(provider, loginId, loginOptions, options),
        NOTPResponse.class);
  }

  @Override
  public NOTPResponse updateUser(String provider, String loginId, String phone, String refreshToken,
      NOTPUpdateOptions updateOptions) throws DescopeException {
    final URI updateURL = composeProviderURI(UPDATE_USER_NOTP_LINK, provider);
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    if (updateOptions == null) {
      updateOptions = new NOTPUpdateOptions();
    }
    NOTPUpdateUserRequest updateRequest = NOTPUpdateUserRequest.builder()
        .provider(provider)
        .loginId(loginId)
        .phone(phone)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .providerId(updateOptions.getProviderId())
        .templates(updateOptions.getTemplates())
        .templateOptions(updateOptions.getTemplateOptions())
        .ssoAppId(updateOptions.getSsoAppId())
        .locale(updateOptions.getLocale())
        .failOnConflict(updateOptions.getFailOnConflict())
        .build();
    return getApiProxy(refreshToken).post(updateURL, updateRequest, NOTPResponse.class);
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    JWTResponse jwtResponse = getApiProxy().post(
        getUri(NOTP_SESSION_LINK),
        EnchantedLinkSessionBody.builder().pendingRef(pendingRef).build(),
        JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  private static NOTPSignInRequest newSignInRequest(String provider, String loginId, LoginOptions loginOptions,
      NOTPOptions options) {
    if (options == null) {
      options = new NOTPOptions();
    }
    return NOTPSignInRequest.builder()
        .provider(provider)
        .loginId(loginId)
        .loginOptions(NOTPLoginOptions.from(loginOptions))
        .providerId(options.getProviderId())
        .templates(options.getTemplates())
        .ssoAppId(options.getSsoAppId())
        .build();
  }

  // Provider must stay a single path segment
  @SneakyThrows
  private URI composeProviderURI(String route, String provider) {
    if (StringUtils.isBlank(provider) || ".".equals(provider) || "..".equals(provider)) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    return getUri(String.format(route, URLEncoder.encode(provider, "UTF-8")));
  }
}
