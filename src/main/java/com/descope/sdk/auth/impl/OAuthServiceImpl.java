package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_IN;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_UP;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_OAUTH_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OAuthService;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class OAuthServiceImpl extends AuthenticationServiceImpl implements OAuthService {
  OAuthServiceImpl(Client client) {
    super(client);
  }

  protected String startWithUrl(String url, String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams) throws DescopeException {
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectURL)) {
      params.put("redirectURL", redirectURL);
    }
    URI oauthURL = getQueryParamUri(url, params);
    ApiProxy apiProxy = getApiProxy();
    OAuthResponse res = apiProxy.post(oauthURL, loginOptions, OAuthResponse.class);
    url = res.getUrl();
    URI resUrl = getQueryParamUri(url, authParams);
    return resUrl.toString();
  }

  @Override
  public String start(String provider, String redirectURL, LoginOptions loginOptions) throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK, provider, redirectURL, loginOptions, null);
  }

  @Override
  public String start(String provider, String redirectURL, LoginOptions loginOptions, Map<String, String> authParams)
      throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK, provider, redirectURL, loginOptions, authParams);
  }

  @Override
  public String startSignIn(String provider, String redirectURL, LoginOptions loginOptions) throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK_SIGN_IN, provider, redirectURL, loginOptions, null);
  }

  @Override
  public String startSignIn(String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams) throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK_SIGN_IN, provider, redirectURL, loginOptions, authParams);
  }

  @Override
  public String startSignUp(String provider, String redirectURL, LoginOptions loginOptions) throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK_SIGN_UP, provider, redirectURL, loginOptions, null);
  }

  @Override
  public String startSignUp(String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams) throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK_SIGN_UP, provider, redirectURL, loginOptions, authParams);
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
