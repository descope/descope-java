package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ANONYMOUS_USER;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SIGN_IN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SIGN_UP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SIGN_UP_OR_IN;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_JWT_LINK;

import com.descope.exception.ClientFunctionalException;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.MgmtSignUpUser;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.AnonymousUserRequest;
import com.descope.model.jwt.request.ManagementSignInRequest;
import com.descope.model.jwt.request.ManagementSignUpRequest;
import com.descope.model.jwt.request.UpdateJwtRequest;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.JwtService;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class JwtServiceImpl extends ManagementsBase implements JwtService {

  JwtServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AuthenticationInfo anonymous(AnonymousUserRequest request)
          throws DescopeException {
    // Make the API call
    URI uri = getUri(MANAGEMENT_ANONYMOUS_USER);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(uri, request, JWTResponse.class);
    // Validate the JWT and return AuthenticationInfo
    return validateAndCreateAuthInfo(jwtResponse);
  }

  @Override
  public Token updateJWTWithCustomClaims(String jwt, Map<String, Object> customClaims)
      throws DescopeException {
    if (StringUtils.isBlank(jwt)) {
      throw ServerCommonException.invalidArgument("JWT");
    }

    // customClaims can be nil, it will mean that this JWT will be validated, and updated authz data
    // will be set
    UpdateJwtRequest updateJwtRequest = new UpdateJwtRequest(jwt, customClaims);
    URI updateJwtUri = composeUpdateJwtUri();
    ApiProxy apiProxy = getApiProxy();

    UpdateJwtResponse jwtResponse = apiProxy.post(updateJwtUri, updateJwtRequest, UpdateJwtResponse.class);
    return validateAndCreateToken(jwtResponse.getJwt());
  }

  @Override
  public AuthenticationInfo signIn(String loginId, LoginOptions loginOptions) throws DescopeException {
    // validate input
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("loginId");
    }
    if (loginOptions == null) {
      loginOptions = new LoginOptions(); // Default values
    }
    if (loginOptions.isJWTRequired() && StringUtils.isBlank(loginOptions.getJwt())) {
      throw ServerCommonException.invalidArgument("JWT is required for step-up or MFA authentication");
    }
    // Construct request payload
    ManagementSignInRequest request = new ManagementSignInRequest(
            loginId,
            loginOptions.isStepup(),
            loginOptions.isMfa(),
            loginOptions.isRevokeOtherSessions(),
            loginOptions.getRevokeOtherSessionsTypes(),
            loginOptions.getCustomClaims(),
            loginOptions.getJwt());

    // Make the API call
    URI signInUri = getUri(MANAGEMENT_SIGN_IN);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(signInUri, request, JWTResponse.class);
    // Validate the JWT and return AuthenticationInfo
    return validateAndCreateAuthInfo(jwtResponse);
  }

  @Override
  public AuthenticationInfo signUpOrIn(String loginId, MgmtSignUpUser signUpUserDetails)
          throws DescopeException {
    return signUp(loginId, signUpUserDetails, MANAGEMENT_SIGN_UP_OR_IN);
  }

  @Override
  public AuthenticationInfo signUp(String loginId, MgmtSignUpUser signUpUserDetails)
          throws DescopeException {
    return signUp(loginId,  signUpUserDetails, MANAGEMENT_SIGN_UP);
  }

  private AuthenticationInfo signUp(String loginId, MgmtSignUpUser signUpUserDetails,
                                    String path) {
    // validate input
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("loginId");
    }
    // Construct request payload
    if (signUpUserDetails == null) {
      signUpUserDetails = new MgmtSignUpUser();
    }
    ManagementSignUpRequest request = new ManagementSignUpRequest(
            loginId,
            signUpUserDetails.getUser(),
            signUpUserDetails.isVerifiedEmail(),
            signUpUserDetails.isVerifiedPhone(),
            signUpUserDetails.getSsoAppId(),
            signUpUserDetails.getCustomClaims()
    );
    // Make the API call
    URI uri = getUri(path);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(uri, request, JWTResponse.class);
    // Validate the JWT and return AuthenticationInfo
    return validateAndCreateAuthInfo(jwtResponse);
  }

  private AuthenticationInfo validateAndCreateAuthInfo(JWTResponse jwtResponse) throws DescopeException {
    if (jwtResponse == null) {
      throw ClientFunctionalException.invalidToken(); //TODO: consider changing error type here
    }
    Token sessionToken = validateAndCreateToken(jwtResponse.getSessionJwt());
    Token refreshToken = validateAndCreateToken(jwtResponse.getRefreshJwt());
    return new AuthenticationInfo(sessionToken, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen());
  }

  private URI composeUpdateJwtUri() {
    return getUri(UPDATE_JWT_LINK);
  }
}
