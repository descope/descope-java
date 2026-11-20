package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_IN;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_UP;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_UPDATE;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_OAUTH_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.enums.OAuthPrompt;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OAuthService;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
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
    String resUrl = appendQueryParams(url, authParams);
    return resUrl;
  }

  private String startWithUrl(String url, String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectURL)) {
      params.put("redirectUrl", redirectURL);
    }
    addHintParams(params, loginHint, initiatedEmail);
    URI oauthURL = getOAuthUri(url, params, prompt);
    if (loginOptions == null) {
      loginOptions = new LoginOptions();
    }
    OAuthResponse res = getApiProxy().post(oauthURL, loginOptions, OAuthResponse.class);
    return appendQueryParams(res.getUrl(), authParams);
  }

  private void addHintParams(Map<String, String> params, String loginHint, String initiatedEmail) {
    if (StringUtils.isNotBlank(loginHint)) {
      params.put("loginHint", loginHint);
    }
    if (StringUtils.isNotBlank(initiatedEmail)) {
      params.put("initiatedEmail", initiatedEmail);
    }
  }

  // prompt is a repeated query param, which a single-valued map can't hold
  @SneakyThrows
  private URI getOAuthUri(String url, Map<String, String> params, List<OAuthPrompt> prompt) {
    URI uri = getQueryParamUri(url, params);
    if (prompt == null || prompt.isEmpty()) {
      return uri;
    }
    StringBuilder sb = new StringBuilder(uri.toString());
    for (OAuthPrompt p : prompt) {
      if (p == null) {
        throw ServerCommonException.invalidArgument("prompt");
      }
      sb.append("&prompt=").append(URLEncoder.encode(p.getValue(), "UTF-8"));
    }
    return URI.create(sb.toString());
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
  public String start(String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK, provider, redirectURL, loginOptions, authParams, prompt, loginHint,
        initiatedEmail);
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
  public String startSignIn(String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK_SIGN_IN, provider, redirectURL, loginOptions, authParams, prompt, loginHint,
        initiatedEmail);
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
  public String startSignUp(String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    return startWithUrl(COMPOSE_OAUTH_LINK_SIGN_UP, provider, redirectURL, loginOptions, authParams, prompt, loginHint,
        initiatedEmail);
  }

  @Override
  public String updateUser(String provider, String redirectURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions) throws DescopeException {
    return updateUser(provider, redirectURL, refreshToken, allowAllMerge, loginOptions, null, null, null);
  }

  @Override
  public String updateUser(String provider, String redirectURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    if (StringUtils.isBlank(provider)) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectURL)) {
      params.put("redirectUrl", redirectURL);
    }
    if (allowAllMerge) {
      params.put("allowAllMerge", "true");
    }
    addHintParams(params, loginHint, initiatedEmail);
    URI oauthURL = getOAuthUri(COMPOSE_OAUTH_LINK_UPDATE, params, prompt);
    if (loginOptions == null) {
      loginOptions = new LoginOptions();
    }
    return getApiProxy(refreshToken).post(oauthURL, loginOptions, OAuthResponse.class).getUrl();
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
