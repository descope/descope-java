package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_SIGN_IN_FINISH;
import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_SIGN_IN_START;
import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_SIGN_UP_FINISH;
import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_SIGN_UP_OR_IN_START;
import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_SIGN_UP_START;
import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_UPDATE_FINISH;
import static com.descope.literals.Routes.AuthEndPoints.WEBAUTHN_UPDATE_START;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.model.user.User;
import com.descope.model.webauthn.WebAuthnFinishRequest;
import com.descope.model.webauthn.WebAuthnTransactionResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.WebAuthnService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class WebAuthnServiceImpl extends AuthenticationServiceImpl implements WebAuthnService {

  WebAuthnServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public WebAuthnTransactionResponse signUpStart(String loginId, User user, String origin) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(origin)) {
      throw ServerCommonException.invalidArgument("Origin");
    }
    if (user == null) {
      user = new User();
    }
    URI webAuthnSignUpURL = getUri(WEBAUTHN_SIGN_UP_START);
    Map<String, Object> signUpRequest = mapOf("loginId", loginId, "user", user, "origin", origin);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(webAuthnSignUpURL, signUpRequest, WebAuthnTransactionResponse.class);
  }

  @Override
  public AuthenticationInfo signUpFinish(WebAuthnFinishRequest finishRequest) throws DescopeException {
    if (finishRequest == null
        || StringUtils.isBlank(finishRequest.getResponse())
        || StringUtils.isBlank(finishRequest.getTransactionId())) {
      throw ServerCommonException.invalidArgument("Finish Request");
    }
    URI webAuthnSignUpURL = getUri(WEBAUTHN_SIGN_UP_FINISH);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(webAuthnSignUpURL, finishRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public WebAuthnTransactionResponse signInStart(String loginId, String origin, String token, LoginOptions loginOptions)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(origin)) {
      throw ServerCommonException.invalidArgument("Origin");
    }
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(token)) {
        throw ServerCommonException.invalidArgument("Token");
      }
      apiProxy = getApiProxy(token);
    } else {
      apiProxy = getApiProxy();
    }
    URI webAuthnSignInURL = getUri(WEBAUTHN_SIGN_IN_START);
    Map<String, Object> signInRequest = mapOf("loginId", loginId, "origin", origin);
    if (loginOptions != null) {
      signInRequest.put("loginOptions", loginOptions);
    }
    return apiProxy.post(webAuthnSignInURL, signInRequest, WebAuthnTransactionResponse.class);
  }

  @Override
  public AuthenticationInfo signInFinish(WebAuthnFinishRequest finishRequest) throws DescopeException {
    if (finishRequest == null
        || StringUtils.isBlank(finishRequest.getResponse())
        || StringUtils.isBlank(finishRequest.getTransactionId())) {
      throw ServerCommonException.invalidArgument("Finish Request");
    }
    URI webAuthnSignInURL = getUri(WEBAUTHN_SIGN_IN_FINISH);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(webAuthnSignInURL, finishRequest, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  @Override
  public WebAuthnTransactionResponse signUpOrInStart(String loginId, String origin) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(origin)) {
      throw ServerCommonException.invalidArgument("Origin");
    }
    URI webAuthnSignUpOrInURL = getUri(WEBAUTHN_SIGN_UP_OR_IN_START);
    Map<String, Object> signUpOrInRequest = mapOf("loginId", loginId, "origin", origin);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(webAuthnSignUpOrInURL, signUpOrInRequest, WebAuthnTransactionResponse.class);
  }

  @Override
  public WebAuthnTransactionResponse updateUserDeviceStart(String loginId, String origin, String token)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(origin)) {
      throw ServerCommonException.invalidArgument("Origin");
    }
    if (StringUtils.isBlank(token)) {
      throw ServerCommonException.invalidArgument("Token");
    }
    validateSessionWithToken(token); // no need to send remote if the token is not valid
    URI webAuthnUpdateStartURL = getUri(WEBAUTHN_UPDATE_START);
    Map<String, Object> updateRequest = mapOf("loginId", loginId, "origin", origin);
    ApiProxy apiProxy = getApiProxy(token);
    return apiProxy.post(webAuthnUpdateStartURL, updateRequest, WebAuthnTransactionResponse.class);
  }

  @Override
  public void updateUserDeviceFinish(WebAuthnFinishRequest finishRequest) throws DescopeException {
    if (finishRequest == null
        || StringUtils.isBlank(finishRequest.getResponse())
        || StringUtils.isBlank(finishRequest.getTransactionId())) {
      throw ServerCommonException.invalidArgument("Finish Request");
    }
    URI webAuthnUpdateFinishURL = getUri(WEBAUTHN_UPDATE_FINISH);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(webAuthnUpdateFinishURL, finishRequest, Void.class);
  }  
}
