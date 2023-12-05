package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.SIGN_UP_TOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_TOTP_LINK;
import static com.descope.literals.Routes.AuthEndPoints.VERIFY_TOTP_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.otp.AuthenticationVerifyRequestBody;
import com.descope.model.totp.TOTPResponse;
import com.descope.model.totp.TotpSignUpRequestBody;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.TOTPService;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;

class TOTPServiceImpl extends AuthenticationServiceImpl implements TOTPService {

  TOTPServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public TOTPResponse signUp(String loginId, User user) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("loginId");
    }
    URI totpSignUpURL = composeSignUpTOTPURL();

    TotpSignUpRequestBody signUpRequest = TotpSignUpRequestBody.builder().loginId(loginId).user(user).build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(totpSignUpURL, signUpRequest, TOTPResponse.class);
  }

  @Override
  public AuthenticationInfo signInCode(String loginId, String code, LoginOptions loginOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("loginId");
    }

    AuthenticationVerifyRequestBody authenticationVerifyRequestBody =
        new AuthenticationVerifyRequestBody(loginId, code, loginOptions);
    URI totpVerifyCode = composeVerifyTOTPCodeURL();
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse =
        apiProxy.post(totpVerifyCode, authenticationVerifyRequestBody, JWTResponse.class);

    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public TOTPResponse updateUser(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("loginId");
    }

    URI totpUpdateUser = composeUpdateTOTPURL();
    TotpSignUpRequestBody signUpRequest = TotpSignUpRequestBody.builder().loginId(loginId).build();
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(totpUpdateUser, signUpRequest, TOTPResponse.class);
  }

  private URI composeSignUpTOTPURL() {
    return getUri(SIGN_UP_TOTP_LINK);
  }

  private URI composeUpdateTOTPURL() {
    return getUri(UPDATE_USER_TOTP_LINK);
  }

  private URI composeVerifyTOTPCodeURL() {
    return getUri(VERIFY_TOTP_LINK);
  }
}
