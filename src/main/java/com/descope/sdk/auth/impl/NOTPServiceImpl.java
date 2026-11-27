package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.NOTP_SESSION_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_OR_IN_NOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_NOTP_LINK;

import com.descope.enums.NOTPProvider;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.UpdateOptions;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.notp.NOTPLoginOptions;
import com.descope.model.notp.NOTPResponse;
import com.descope.model.notp.NOTPSessionRequest;
import com.descope.model.notp.NOTPSignInRequest;
import com.descope.model.notp.NOTPSignUpRequest;
import com.descope.model.notp.NOTPUpdateUserRequest;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.NOTPService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class NOTPServiceImpl extends AuthenticationServiceImpl implements NOTPService {

  NOTPServiceImpl(Client client) {
    super(client);
  }

  @Override
  public NOTPResponse signIn(NOTPProvider provider, String loginId, LoginOptions loginOptions)
      throws DescopeException {
    return signIn(provider, loginId, loginOptions, null);
  }

  @Override
  public NOTPResponse signIn(NOTPProvider provider, String loginId, LoginOptions loginOptions, String refreshToken)
      throws DescopeException {
    URI signInURL = composeProviderURI(SIGN_IN_NOTP_LINK, provider);
    NOTPSignInRequest signInRequest = new NOTPSignInRequest(loginId, toNOTPLoginOptions(loginOptions));
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(signInURL, signInRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUp(NOTPProvider provider, String loginId, User user) throws DescopeException {
    return signUp(provider, loginId, user, null);
  }

  @Override
  public NOTPResponse signUp(NOTPProvider provider, String loginId, User user, SignUpOptions signUpOptions)
      throws DescopeException {
    URI signUpURL = composeProviderURI(SIGN_UP_NOTP_LINK, provider);
    if (user == null) {
      user = new User();
    }
    if (StringUtils.isBlank(user.getPhone())) {
      user.setPhone(loginId);
    }
    NOTPSignUpRequest signUpRequest = NOTPSignUpRequest.builder()
        .loginId(loginId)
        .phone(user.getPhone())
        .user(user)
        .loginOptions(signUpOptions)
        .build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(signUpURL, signUpRequest, NOTPResponse.class);
  }

  @Override
  public NOTPResponse signUpOrIn(NOTPProvider provider, String loginId) throws DescopeException {
    return signUpOrIn(provider, loginId, null);
  }

  @Override
  public NOTPResponse signUpOrIn(NOTPProvider provider, String loginId, SignUpOptions signUpOptions)
      throws DescopeException {
    URI signUpOrInURL = composeProviderURI(SIGN_UP_OR_IN_NOTP_LINK, provider);
    NOTPLoginOptions loginOptions = null;
    if (signUpOptions != null) {
      loginOptions = NOTPLoginOptions.builder()
          .customClaims(signUpOptions.getCustomClaims())
          .templateOptions(signUpOptions.getTemplateOptions())
          .build();
    }
    NOTPSignInRequest signInRequest = new NOTPSignInRequest(loginId, loginOptions);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(signUpOrInURL, signInRequest, NOTPResponse.class);
  }

  @Override
  public AuthenticationInfo getSession(String pendingRef) throws DescopeException {
    URI getSessionURL = getUri(NOTP_SESSION_LINK);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(
        getSessionURL, NOTPSessionRequest.builder().pendingRef(pendingRef).build(), JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public NOTPResponse updateUserPhone(NOTPProvider provider, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions) throws DescopeException {
    return updateUserPhone(provider, loginId, phone, refreshToken, updateOptions, null);
  }

  @Override
  public NOTPResponse updateUserPhone(NOTPProvider provider, String loginId, String phone, String refreshToken,
      UpdateOptions updateOptions, Map<String, String> templateOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    URI updateURL = composeProviderURI(UPDATE_USER_NOTP_LINK, provider);
    if (updateOptions == null) {
      updateOptions = new UpdateOptions();
    }
    NOTPUpdateUserRequest updateRequest = NOTPUpdateUserRequest.builder()
        .loginId(loginId)
        .phone(phone)
        .addToLoginIds(updateOptions.isAddToLoginIds())
        .onMergeUseExisting(updateOptions.isOnMergeUseExisting())
        .templateOptions(templateOptions)
        .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    return apiProxy.post(updateURL, updateRequest, NOTPResponse.class);
  }

  // LoginOptions also serializes jwt and jwtrequired, which these endpoints reject
  private static NOTPLoginOptions toNOTPLoginOptions(LoginOptions loginOptions) {
    if (loginOptions == null) {
      return null;
    }
    return NOTPLoginOptions.builder()
        .stepup(loginOptions.isStepup())
        .mfa(loginOptions.isMfa())
        .customClaims(loginOptions.getCustomClaims())
        .templateOptions(loginOptions.getTemplateOptions())
        .revokeOtherSessions(loginOptions.isRevokeOtherSessions())
        .revokeOtherSessionsTypes(loginOptions.getRevokeOtherSessionsTypes())
        .locale(loginOptions.getLocale())
        .templateId(loginOptions.getTemplateId())
        .build();
  }

  private URI composeProviderURI(String route, NOTPProvider provider) {
    if (provider == null) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    return getUri(String.format(route, provider.getValue()));
  }
}
