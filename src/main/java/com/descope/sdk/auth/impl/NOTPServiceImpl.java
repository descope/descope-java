package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.NOTP_SESSION_LINK;

import com.descope.enums.NOTPProvider;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSessionRequest;
import com.descope.model.notp.NOTPSignInRequest;
import com.descope.model.notp.NOTPSignUpOptions;
import com.descope.model.notp.NOTPSignUpRequest;
import com.descope.model.notp.NOTPSignUpUser;
import com.descope.model.notp.NOTPUpdateOptions;
import com.descope.model.notp.NOTPUpdateUserRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.NOTPService;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;

class NOTPServiceImpl extends AuthenticationServiceImpl implements NOTPService {

  NOTPServiceImpl(Client client) {
    super(client);
  }

  @Override
  public NOTPResponse signIn(NOTPProvider provider, String loginId, NOTPLoginOptions loginOptions,
      NOTPOptions options, String refreshToken) throws DescopeException {
    return signInByAction(provider, "signin", loginId, loginOptions, options, refreshToken);
  }

  @Override
  public NOTPResponse signUpOrIn(NOTPProvider provider, String loginId, NOTPLoginOptions loginOptions,
      NOTPOptions options, String refreshToken) throws DescopeException {
    return signInByAction(provider, "signup-in", loginId, loginOptions, options, refreshToken);
  }

  private NOTPResponse signInByAction(NOTPProvider provider, String action, String loginId,
      NOTPLoginOptions loginOptions, NOTPOptions options, String refreshToken) throws DescopeException {
    if (provider == null) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    if (options == null) {
      options = new NOTPOptions();
    }
    NOTPSignInRequest request = NOTPSignInRequest.builder()
        .provider(provider.getValue())
        .loginId(StringUtils.isBlank(loginId) ? null : loginId)
        .loginOptions(loginOptions)
        .providerId(options.getProviderId())
        .templates(options.getTemplates())
        .ssoAppId(options.getSsoAppId())
        .build();
    ApiProxy apiProxy;
    if (loginOptions != null && loginOptions.isJWTRequired()) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(composeNOTPURL(provider, action), request, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUp(NOTPProvider provider, String loginId, NOTPSignUpUser user,
      NOTPSignUpOptions signUpOptions, NOTPOptions options) throws DescopeException {
    if (provider == null) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    if (user == null) {
      user = new NOTPSignUpUser();
    }
    if (StringUtils.isBlank(user.getPhone()) && StringUtils.isNotBlank(loginId)) {
      user.setPhone(loginId);
    }
    if (options == null) {
      options = new NOTPOptions();
    }
    NOTPSignUpRequest request = NOTPSignUpRequest.builder()
        .provider(provider.getValue())
        .phone(StringUtils.isBlank(user.getPhone()) ? null : user.getPhone())
        .loginId(StringUtils.isBlank(loginId) ? null : loginId)
        .user(user)
        .providerId(options.getProviderId())
        .templates(options.getTemplates())
        .ssoAppId(options.getSsoAppId())
        .loginOptions(signUpOptions)
        .build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(composeNOTPURL(provider, "signup"), request, NOTPResponse.class);
  }

  @Override
  public NOTPResponse updateUserPhone(NOTPProvider provider, String loginId, String phone, String refreshToken,
      NOTPUpdateOptions updateOptions) throws DescopeException {
    if (provider == null) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    if (updateOptions == null) {
      updateOptions = new NOTPUpdateOptions();
    }
    NOTPUpdateUserRequest request = NOTPUpdateUserRequest.builder()
        .provider(provider.getValue())
        .loginId(loginId)
        .phone(StringUtils.isBlank(phone) ? null : phone)
        .addToLoginIds(updateOptions.getAddToLoginIds())
        .onMergeUseExisting(updateOptions.getOnMergeUseExisting())
        .failOnConflict(updateOptions.getFailOnConflict())
        .templateOptions(updateOptions.getTemplateOptions())
        .locale(updateOptions.getLocale())
        .providerId(updateOptions.getProviderId())
        .templates(updateOptions.getTemplates())
        .ssoAppId(updateOptions.getSsoAppId())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(composeNOTPURL(provider, "update"), request, NOTPResponse.class);
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    if (StringUtils.isBlank(pendingRef)) {
      throw ServerCommonException.invalidArgument("Pending Ref");
    }
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(getUri(NOTP_SESSION_LINK),
        NOTPSessionRequest.builder().pendingRef(pendingRef).build(), JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  private URI composeNOTPURL(NOTPProvider provider, String action) {
    return composeURI(NOTP_LINK, provider.getValue() + "/" + action);
  }
}
