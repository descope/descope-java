package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_ACCESS_KEY_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.ExchangeTokenRequest;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import java.net.URI;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

class AuthenticationServiceImpl extends AuthenticationsBase {

  AuthenticationServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public Token validateSessionWithToken(String sessionToken) throws DescopeException {
    if (StringUtils.isBlank(sessionToken)) {
      throw ServerCommonException.invalidArgument("sessionToken");
    }
    return validateJWT(sessionToken);
  }

  @Override
  public Token refreshSessionWithToken(String refreshToken) throws DescopeException {
    if (Strings.isEmpty(refreshToken)) {
      throw ServerCommonException.missingArguments("Request doesn't contain refresh token");
    }

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
      return refreshSessionWithToken(refreshToken);
    }
  }

  @Override
  public Token exchangeAccessKey(String accessKey) throws DescopeException {
    var apiProxy = getApiProxy(accessKey);
    URI exchangeAccessKeyLinkURL = composeExchangeAccessKeyLinkURL();

    var jwtResponse = apiProxy.post(exchangeAccessKeyLinkURL, null, JWTResponse.class);
    var authenticationInfo = getAuthenticationInfo(jwtResponse);
    return authenticationInfo.getToken();
  }

  @Override
  public boolean validatePermissions(Token token, List<String> permissions)
      throws DescopeException {
    return validatePermissions(token, "", permissions);
  }

  @Override
  public boolean validatePermissions(Token token, String tenant, List<String> permissions)
      throws DescopeException {
    List<String> authorizationClaimItems = getAuthorizationClaimItems(token, tenant, permissions);
    return CollectionUtils.isEqualCollection(authorizationClaimItems, permissions);
  }

  @Override
  public boolean validateRoles(Token token, List<String> roles) throws DescopeException {
    return validateRoles(token, "", roles);
  }

  @Override
  public boolean validateRoles(Token token, String tenant, List<String> roles)
      throws DescopeException {
    List<String> authorizationClaimItems = getAuthorizationClaimItems(token, tenant, roles);
    return CollectionUtils.isEqualCollection(authorizationClaimItems, roles);
  }

  AuthenticationInfo exchangeToken(String code, URI url) {
    if (StringUtils.isBlank(code)) {
      throw ServerCommonException.invalidArgument("Code");
    }
    ExchangeTokenRequest request = new ExchangeTokenRequest(code);
    var apiProxy = getApiProxy();
    var jwtResponse = apiProxy.post(url, request, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  private URI composeExchangeAccessKeyLinkURL() {
    return getUri(EXCHANGE_ACCESS_KEY_LINK);
  }
}
