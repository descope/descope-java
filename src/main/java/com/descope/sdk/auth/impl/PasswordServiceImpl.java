package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.PASSWORD_POLICY_LINK;
import static com.descope.literals.Routes.AuthEndPoints.REPLACE_USER_PASSWORD_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SEND_RESET_PASSWORD_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_IN_PASSWORD_LINK;
import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_PASSWORD_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_PASSWORD_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.password.AuthenticationPasswordReplaceRequestBody;
import com.descope.model.password.AuthenticationPasswordResetRequestBody;
import com.descope.model.password.AuthenticationPasswordSignInRequestBody;
import com.descope.model.password.AuthenticationPasswordSignUpRequestBody;
import com.descope.model.password.AuthenticationPasswordUpdateRequestBody;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.password.PasswordResetOptions;
import com.descope.model.password.PasswordResetResponse;
import com.descope.model.password.PasswordResetSendRequestBody;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.PasswordService;
import com.descope.utils.JwtUtils;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class PasswordServiceImpl extends AuthenticationServiceImpl implements PasswordService {
  PasswordServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AuthenticationInfo signUp(String loginId, User user, String password)
      throws DescopeException {
    return signUp(loginId, user, password, null);
  }

  @Override
  public AuthenticationInfo signUp(String loginId, User user, String password, SignUpOptions signUpOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (user == null) {
      user = new User();
    }
    AuthenticationPasswordSignUpRequestBody pwdSignUpRequest =
        AuthenticationPasswordSignUpRequestBody.builder()
            .loginId(loginId)
            .user(user)
            .password(password)
            .loginOptions(signUpOptions)
            .build();
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(getUri(SIGN_UP_PASSWORD_LINK), pwdSignUpRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public AuthenticationInfo signIn(String loginId, String password) throws DescopeException {
    return signIn(loginId, password, null, null, null);
  }

  @Override
  public AuthenticationInfo signIn(String loginId, String password, String ssoAppId, LoginOptions loginOptions,
      String refreshToken) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    AuthenticationPasswordSignInRequestBody pwdSignInRequest =
        AuthenticationPasswordSignInRequestBody.builder()
            .loginId(loginId)
            .password(password)
            .ssoAppId(ssoAppId)
            .loginOptions(loginOptions)
            .build();
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    JWTResponse jwtResponse =
        apiProxy.post(getUri(SIGN_IN_PASSWORD_LINK), pwdSignInRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public void sendPasswordReset(String loginId, String redirectURL) throws DescopeException {
    sendPasswordReset(loginId, redirectURL, null);
  }

  @Override
  public void sendPasswordReset(String loginId, String redirectURL, Map<String, String> templateOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    AuthenticationPasswordResetRequestBody pwdResetRequest =
        AuthenticationPasswordResetRequestBody.builder()
            .loginId(loginId)
            .redirectURL(redirectURL)
            .templateOptions(templateOptions)
            .build();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(SEND_RESET_PASSWORD_LINK), pwdResetRequest, Void.class);
  }

  @Override
  public PasswordResetResponse sendPasswordReset(String loginId, String redirectURL,
      Map<String, String> templateOptions, PasswordResetOptions options) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (options == null) {
      options = new PasswordResetOptions();
    }
    PasswordResetSendRequestBody pwdResetRequest =
        PasswordResetSendRequestBody.builder()
            .loginId(loginId)
            .redirectUrl(redirectURL)
            .templateOptions(templateOptions)
            .providerId(options.getProviderId())
            .locale(options.getLocale())
            .useRecovery(options.getUseRecovery())
            .build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(SEND_RESET_PASSWORD_LINK), pwdResetRequest, PasswordResetResponse.class);
  }

  @Override
  public void updateUserPassword(String loginId, String newPassword, String refreshToken)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    AuthenticationPasswordUpdateRequestBody pwdUpdateRequest =
        AuthenticationPasswordUpdateRequestBody.builder()
            .loginId(loginId)
            .newPassword(newPassword)
            .build();
    ApiProxy apiProxy = getApiProxy(refreshToken);
    apiProxy.post(getUri(UPDATE_USER_PASSWORD_LINK), pwdUpdateRequest, Void.class);
  }

  @Override
  public AuthenticationInfo replaceUserPassword(String loginId, String oldPassword, String newPassword)
      throws DescopeException {
    return replaceUserPassword(loginId, oldPassword, newPassword, null, null);
  }

  @Override
  public AuthenticationInfo replaceUserPassword(String loginId, String oldPassword, String newPassword,
      Boolean revokeOtherSessions, String[] revokeOtherSessionsTypes) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    AuthenticationPasswordReplaceRequestBody pwdUpdateRequest =
        AuthenticationPasswordReplaceRequestBody.builder()
            .loginId(loginId)
            .oldPassword(oldPassword)
            .newPassword(newPassword)
            .revokeOtherSessions(revokeOtherSessions)
            .revokeOtherSessionsTypes(revokeOtherSessionsTypes)
            .build();
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(getUri(REPLACE_USER_PASSWORD_LINK), pwdUpdateRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public PasswordPolicy getPasswordPolicy() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(PASSWORD_POLICY_LINK), PasswordPolicy.class);
  }
}
