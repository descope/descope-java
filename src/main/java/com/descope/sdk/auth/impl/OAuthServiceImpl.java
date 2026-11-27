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
import com.descope.utils.JwtUtils;
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
    return startWithUrl(url, provider, redirectURL, loginOptions, authParams, null, null, null);
  }

  protected String startWithUrl(String url, String provider, String redirectURL, LoginOptions loginOptions,
      Map<String, String> authParams, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    validatePrompt(prompt);
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectURL)) {
      params.put("redirectURL", redirectURL);
    }
    putOptionalParams(params, loginHint, initiatedEmail);
    URI oauthURL = getOAuthUri(url, params, prompt);
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      // Step-up and MFA need the user's refresh JWT as the bearer
      if (StringUtils.isBlank(loginOptions.getJwt())) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(loginOptions.getJwt());
    } else {
      apiProxy = getApiProxy();
    }
    OAuthResponse res = apiProxy.post(oauthURL, loginOptions, OAuthResponse.class);
    url = res.getUrl();
    String resUrl = appendQueryParams(url, authParams);
    return resUrl;
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
  public String updateUser(String provider, String redirectURL, String refreshToken, LoginOptions loginOptions,
      boolean allowAllMerge) throws DescopeException {
    return updateUser(provider, redirectURL, refreshToken, loginOptions, allowAllMerge, null, null, null);
  }

  @Override
  public String updateUser(String provider, String redirectURL, String refreshToken, LoginOptions loginOptions,
      boolean allowAllMerge, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException {
    if (StringUtils.isBlank(provider)) {
      throw ServerCommonException.invalidArgument("provider");
    }
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("refreshToken");
    }
    validatePrompt(prompt);
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectURL)) {
      params.put("redirectUrl", redirectURL);
    }
    if (allowAllMerge) {
      params.put("allowAllMerge", "true");
    }
    putOptionalParams(params, loginHint, initiatedEmail);
    URI oauthURL = getOAuthUri(COMPOSE_OAUTH_LINK_UPDATE, params, prompt);
    // The request body is required
    LoginOptions body = loginOptions != null ? loginOptions : new LoginOptions();
    OAuthResponse res = getApiProxy(refreshToken).post(oauthURL, body, OAuthResponse.class);
    return res.getUrl();
  }

  private static void validatePrompt(List<OAuthPrompt> prompt) {
    if (prompt != null && prompt.contains(null)) {
      throw ServerCommonException.invalidArgument("prompt");
    }
  }

  private static void putOptionalParams(Map<String, String> params, String loginHint, String initiatedEmail) {
    if (StringUtils.isNotBlank(loginHint)) {
      params.put("loginHint", loginHint);
    }
    if (StringUtils.isNotBlank(initiatedEmail)) {
      params.put("initiatedEmail", initiatedEmail);
    }
  }

  // Repeats the prompt query param once per value
  @SneakyThrows
  private URI getOAuthUri(String url, Map<String, String> params, List<OAuthPrompt> prompt) {
    URI uri = getQueryParamUri(url, params);
    if (prompt == null || prompt.isEmpty()) {
      return uri;
    }
    StringBuilder sb = new StringBuilder(uri.toString());
    for (OAuthPrompt p : prompt) {
      sb.append("&prompt=").append(URLEncoder.encode(p.getValue(), "UTF-8"));
    }
    return URI.create(sb.toString());
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
