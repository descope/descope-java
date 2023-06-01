package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.PASSWORD_POLICY;
import static com.descope.literals.Routes.AuthEndPoints.REPLACE_USER_PASSWORD;
import static com.descope.literals.Routes.AuthEndPoints.SEND_RESET_PASSWORD;
import static com.descope.literals.Routes.AuthEndPoints.SIGNIN_PASSWORD;
import static com.descope.literals.Routes.AuthEndPoints.SIGNUP_PASSWORD;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_PASSWORD;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.password.AuthenticationPasswordReplaceRequestBody;
import com.descope.model.password.AuthenticationPasswordResetRequestBody;
import com.descope.model.password.AuthenticationPasswordSignInRequestBody;
import com.descope.model.password.AuthenticationPasswordSignUpRequestBody;
import com.descope.model.password.AuthenticationPasswordUpdateRequestBody;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.user.User;
import com.descope.sdk.auth.PasswordService;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class PasswordServiceImpl extends AuthenticationServiceImpl implements PasswordService {
  PasswordServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public AuthenticationInfo signUp(String loginId, User user, String password)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (Objects.isNull(user)) {
      user = new User();
    }
    var pwdSignUpRequest =
        AuthenticationPasswordSignUpRequestBody.builder()
            .loginId(loginId)
            .user(user)
            .password(password)
            .build();
    var apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(getUri(SIGNUP_PASSWORD), pwdSignUpRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public AuthenticationInfo signIn(String loginId, String password) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    var pwdSignInRequest =
        AuthenticationPasswordSignInRequestBody.builder()
            .loginId(loginId)
            .password(password)
            .build();
    var apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(getUri(SIGNIN_PASSWORD), pwdSignInRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public void sendPasswordReset(String loginId, String redirectURL) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    var pwdResetRequest =
        AuthenticationPasswordResetRequestBody.builder()
            .loginId(loginId)
            .redirectURL(redirectURL)
            .build();
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(SEND_RESET_PASSWORD), pwdResetRequest, Void.class);
  }

  @Override
  public void updateUserPassword(String loginId, String newPassword) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    var pwdUpdateRequest =
        AuthenticationPasswordUpdateRequestBody.builder()
            .loginId(loginId)
            .newPassword(newPassword)
            .build();
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(UPDATE_USER_PASSWORD), pwdUpdateRequest, Void.class);
  }

  @Override
  public void replaceUserPassword(String loginId, String oldPassword, String newPassword)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    var pwdUpdateRequest =
        AuthenticationPasswordReplaceRequestBody.builder()
            .loginId(loginId)
            .oldPassword(oldPassword)
            .newPassword(newPassword)
            .build();
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(REPLACE_USER_PASSWORD), pwdUpdateRequest, Void.class);
  }

  @Override
  public PasswordPolicy getPasswordPolicy() throws DescopeException {
    var apiProxy = getApiProxy();
    return apiProxy.get(getUri(PASSWORD_POLICY), PasswordPolicy.class);
  }
}
