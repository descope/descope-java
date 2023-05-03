package com.descope.sdk.auth.impl;

import static java.util.Objects.isNull;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.magiclink.Tokens;
import com.descope.sdk.auth.AuthenticationService;
import java.net.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

class AuthenticationServiceImpl extends AuthenticationsBase implements AuthenticationService {

  AuthenticationServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public Token validateSessionWithRequest(HttpRequest httpRequest) throws DescopeException {
    if (isNull(httpRequest)) {
      throw ServerCommonException.invalidArgument("request");
    }

    Tokens tokens = provideTokens(httpRequest);
    if (Strings.isEmpty(tokens.getSessionToken())) {
      throw ServerCommonException.missingArguments("Request doesn't contain session token");
    }

    return validateJWT(tokens.getSessionToken());
  }

  @Override
  public Token validateSessionWithToken(String sessionToken) throws DescopeException {
    if (StringUtils.isBlank(sessionToken)) {
      throw ServerCommonException.invalidArgument("sessionToken");
    }
    return validateJWT(sessionToken);
  }

  @Override
  public Token refreshSessionWithRequest(HttpRequest httpRequest, boolean addCookies)
      throws DescopeException {
    if (isNull(httpRequest)) {
      throw ServerCommonException.invalidArgument("request");
    }

    Tokens tokens = provideTokens(httpRequest);
    String refreshToken = tokens.getRefreshToken();
    return refreshSessionWithToken(refreshToken, addCookies);
  }

  @Override
  public Token refreshSessionWithToken(String refreshToken, boolean addCookies)
      throws DescopeException {
    if (Strings.isEmpty(refreshToken)) {
      throw ServerCommonException.missingArguments("Request doesn't contain refresh token");
    }

    // TODO - Add Cookies while creating AuthenticationInfo | 03/05/23 | by keshavram
    return refreshSession(refreshToken);
  }

  @Override
  public Token validateAndRefreshSessionWithTokens(String sessionToken, String refreshToken)
      throws DescopeException {
    if (StringUtils.isAllBlank(sessionToken, refreshToken)) {
      throw ServerCommonException.missingArguments("Both sessionToken and refreshToken are empty");
    } else if (StringUtils.isNotBlank(sessionToken)) {
      return validateSessionWithToken(sessionToken);
    } else {
      return refreshSessionWithToken(refreshToken, false);
    }
  }
}
