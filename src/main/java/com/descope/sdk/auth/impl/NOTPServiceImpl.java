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
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPRequestOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.notp.request.NOTPSessionRequest;
import com.descope.model.notp.request.NOTPSignInRequest;
import com.descope.model.notp.request.NOTPSignUpRequest;
import com.descope.model.notp.request.NOTPUpdateRequest;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.NOTPService;
import java.net.URI;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class NOTPServiceImpl extends AuthenticationServiceImpl implements NOTPService {

  // Provider is a path segment
  private static final Pattern PROVIDER_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

  NOTPServiceImpl(Client client) {
    super(client);
  }

  @Override
  public NOTPResponse signUp(String provider, String loginId, User user, NOTPSignUpOptions signUpOptions,
      NOTPRequestOptions requestOptions) throws DescopeException {
    final URI signUpURL = composeProviderURL(SIGN_UP_NOTP_LINK, provider);
    if (user == null) {
      user = new User();
    }
    if (StringUtils.isBlank(user.getPhone())) {
      user.setPhone(loginId);
    }
    if (requestOptions == null) {
      requestOptions = new NOTPRequestOptions();
    }
    NOTPSignUpRequest signUpRequest = NOTPSignUpRequest.builder()
        .loginId(loginId)
        .phone(user.getPhone())
        .user(user)
        .loginOptions(signUpOptions)
        .providerId(requestOptions.getProviderId())
        .templates(requestOptions.getTemplates())
        .ssoAppId(requestOptions.getSsoAppId())
        .build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(signUpURL, signUpRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signIn(String provider, String loginId, String refreshToken, NOTPLoginOptions loginOptions,
      NOTPRequestOptions requestOptions) throws DescopeException {
    URI signInURL = composeProviderURL(SIGN_IN_NOTP_LINK, provider);
    ApiProxy apiProxy;
    if (loginOptions != null && loginOptions.isJWTRequired()) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("Refresh Token");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    NOTPSignInRequest signInRequest = buildSignInRequest(loginId, loginOptions, requestOptions);
    return apiProxy.post(signInURL, signInRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUpOrIn(String provider, String loginId, NOTPSignUpOptions signUpOptions,
      NOTPRequestOptions requestOptions) throws DescopeException {
    URI signUpOrInURL = composeProviderURL(SIGN_UP_OR_IN_NOTP_LINK, provider);
    NOTPLoginOptions loginOptions = null;
    if (signUpOptions != null) {
      loginOptions = NOTPLoginOptions.builder()
          .customClaims(signUpOptions.getCustomClaims())
          .templateOptions(signUpOptions.getTemplateOptions())
          .locale(signUpOptions.getLocale())
          .tenantId(signUpOptions.getTenantId())
          .build();
    }
    NOTPSignInRequest signInRequest = buildSignInRequest(loginId, loginOptions, requestOptions);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(signUpOrInURL, signInRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse updateUser(String provider, String loginId, String phone, String refreshToken,
      NOTPUpdateOptions updateOptions) throws DescopeException {
    final URI updateURL = composeProviderURL(UPDATE_USER_NOTP_LINK, provider);
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    if (updateOptions == null) {
      updateOptions = new NOTPUpdateOptions();
    }
    NOTPUpdateRequest updateRequest = NOTPUpdateRequest.builder()
        .loginId(loginId)
        .phone(phone)
        .addToLoginIds(updateOptions.getAddToLoginIds())
        .onMergeUseExisting(updateOptions.getOnMergeUseExisting())
        .failOnConflict(updateOptions.getFailOnConflict())
        .providerId(updateOptions.getProviderId())
        .templates(updateOptions.getTemplates())
        .templateOptions(updateOptions.getTemplateOptions())
        .ssoAppId(updateOptions.getSsoAppId())
        .locale(updateOptions.getLocale())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(updateURL, updateRequest, NOTPResponse.class);
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(getUri(NOTP_SESSION_LINK),
        NOTPSessionRequest.builder().pendingRef(pendingRef).build(), JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  private NOTPSignInRequest buildSignInRequest(String loginId, NOTPLoginOptions loginOptions,
      NOTPRequestOptions requestOptions) {
    if (requestOptions == null) {
      requestOptions = new NOTPRequestOptions();
    }
    return NOTPSignInRequest.builder()
        .loginId(loginId)
        .loginOptions(loginOptions)
        .providerId(requestOptions.getProviderId())
        .templates(requestOptions.getTemplates())
        .ssoAppId(requestOptions.getSsoAppId())
        .build();
  }

  private URI composeProviderURL(String route, String provider) {
    if (StringUtils.isBlank(provider) || !PROVIDER_PATTERN.matcher(provider).matches()) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    return getUri(String.format(route, provider));
  }
}
