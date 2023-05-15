package com.descope.sdk.auth.impl;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.otp.AuthenticationVerifyRequestBody;
import com.descope.model.totp.TOTPResponse;
import com.descope.model.totp.TotpSignUpRequestBody;
import com.descope.sdk.auth.TOTPService;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

import static com.descope.literals.Routes.AuthEndPoints.*;

class TOTPServiceImpl extends AuthenticationServiceImpl implements TOTPService {

  TOTPServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public TOTPResponse signUp(String loginId, User user) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI totpSignUpURL = composeSignUpTOTPURL();

    var signUpRequest = TotpSignUpRequestBody.builder().LoginID(loginId).user(user).build();
    var apiProxy = getApiProxy();
    return apiProxy.post(totpSignUpURL, signUpRequest, TOTPResponse.class);
  }


  @Override
  public AuthenticationInfo signInCode(String loginId, String code, LoginOptions loginOptions) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    var authenticationVerifyRequestBody = new AuthenticationVerifyRequestBody(loginId, code);
    URI totpVerifyCode = composeVerifyTOTPCodeURL();
    var apiProxy = getApiProxy();
    var jwtResponse = apiProxy.post(totpVerifyCode, authenticationVerifyRequestBody, JWTResponse.class);

    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public TOTPResponse updateUser(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI totpUpdateUser = composeUpdateTOTPURL();
    var signUpRequest = TotpSignUpRequestBody.builder().LoginID(loginId).build();
    var apiProxy = getApiProxy();
    return apiProxy.post(totpUpdateUser, signUpRequest, TOTPResponse.class);
  }

  private URI composeSignUpTOTPURL() {
    return getUri(TOTP_SIGNUP);
  }

  private URI composeUpdateTOTPURL() {
    return getUri(TOTP_USER_UPDATE);

  }

  private URI composeVerifyTOTPCodeURL() {
    return getUri(VERIFY_TOTP_CODE);
  }
}
