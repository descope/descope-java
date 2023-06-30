package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_OAUTH_LINK;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.sdk.auth.OAuthService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class OAuthServiceImpl extends AuthenticationServiceImpl implements OAuthService {
  OAuthServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String start(String provider, String redirectURL, LoginOptions loginOptions)
      throws DescopeException {
    Map<String, String> request = new HashMap<>();
    request.put("provider", provider);
    if (StringUtils.isNotBlank(redirectURL)) {
      request.put("redirectURL", redirectURL);
    }
    URI oauthURL = composeOAuthURL();
    var apiProxy = getApiProxy();
    return apiProxy.post(oauthURL, request, String.class);
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthURL() {
    return getUri(COMPOSE_OAUTH_LINK);
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
