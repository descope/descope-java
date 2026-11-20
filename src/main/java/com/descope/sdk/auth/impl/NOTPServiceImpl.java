package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.NOTP_SESSION;

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
      String refreshToken, NOTPOptions options) throws DescopeException {
    verifyProvider(provider);
    ApiProxy apiProxy;
    if (loginOptions != null && (loginOptions.isStepup() || loginOptions.isMfa())) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(composeURI(provider, "signin"), newSignInRequest(provider, loginId, loginOptions, options),
        NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUp(NOTPProvider provider, String loginId, NOTPSignUpUser user,
      NOTPSignUpOptions signUpOptions, NOTPOptions options) throws DescopeException {
    verifyProvider(provider);
    if (user == null) {
      user = new NOTPSignUpUser();
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
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(composeURI(provider, "signup"), signUpRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUpOrIn(NOTPProvider provider, String loginId, NOTPLoginOptions loginOptions,
      NOTPOptions options) throws DescopeException {
    verifyProvider(provider);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(composeURI(provider, "signup-in"),
        newSignInRequest(provider, loginId, loginOptions, options), NOTPResponse.class);
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(getUri(NOTP_SESSION),
        NOTPSessionRequest.builder().pendingRef(pendingRef).build(), JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public NOTPResponse updateUserPhone(NOTPProvider provider, String loginId, String phone, String refreshToken,
      NOTPUpdateOptions updateOptions) throws DescopeException {
    verifyProvider(provider);
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
        .failOnConflict(updateOptions.isFailOnConflict())
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(composeURI(provider, "update"), updateRequest, NOTPResponse.class);
  }

  private NOTPSignInRequest newSignInRequest(NOTPProvider provider, String loginId, NOTPLoginOptions loginOptions,
      NOTPOptions options) {
    if (options == null) {
      options = new NOTPOptions();
    }
    return NOTPSignInRequest.builder()
        .provider(provider)
        .loginId(StringUtils.isEmpty(loginId) ? null : loginId)
        .loginOptions(loginOptions)
        .providerId(options.getProviderId())
        .templates(options.getTemplates())
        .ssoAppId(options.getSsoAppId())
        .build();
  }

  private void verifyProvider(NOTPProvider provider) {
    if (provider == null) {
      throw ServerCommonException.invalidArgument("Provider");
    }
  }

  private URI composeURI(NOTPProvider provider, String action) {
    return composeURI(NOTP_LINK, provider.getValue() + "/" + action);
  }
}
