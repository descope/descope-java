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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class OAuthServiceImpl extends AuthenticationServiceImpl implements OAuthService {
  private static final ObjectMapper BODY_MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

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
  public String start(String provider, String redirectUrl, List<OAuthPrompt> prompt, String loginHint,
      String initiatedEmail, LoginOptions loginOptions, String refreshToken, Map<String, String> authParams)
      throws DescopeException {
    return startAuth(COMPOSE_OAUTH_LINK, provider, redirectUrl, prompt, loginHint, initiatedEmail, loginOptions,
        refreshToken, authParams);
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
  public String startSignIn(String provider, String redirectUrl, List<OAuthPrompt> prompt, String loginHint,
      String initiatedEmail, LoginOptions loginOptions, String refreshToken, Map<String, String> authParams)
      throws DescopeException {
    return startAuth(COMPOSE_OAUTH_LINK_SIGN_IN, provider, redirectUrl, prompt, loginHint, initiatedEmail,
        loginOptions, refreshToken, authParams);
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
  public String startSignUp(String provider, String redirectUrl, List<OAuthPrompt> prompt, String loginHint,
      String initiatedEmail, LoginOptions loginOptions, String refreshToken, Map<String, String> authParams)
      throws DescopeException {
    return startAuth(COMPOSE_OAUTH_LINK_SIGN_UP, provider, redirectUrl, prompt, loginHint, initiatedEmail,
        loginOptions, refreshToken, authParams);
  }

  @Override
  public String startUpdateUser(String provider, String redirectUrl, boolean allowAllMerge, LoginOptions loginOptions,
      String refreshToken) throws DescopeException {
    return startUpdateUser(provider, redirectUrl, allowAllMerge, null, null, null, loginOptions, refreshToken, null);
  }

  @Override
  public String startUpdateUser(String provider, String redirectUrl, boolean allowAllMerge, List<OAuthPrompt> prompt,
      String loginHint, String initiatedEmail, LoginOptions loginOptions, String refreshToken,
      Map<String, String> authParams) throws DescopeException {
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("refreshToken");
    }
    Map<String, String> params = composeParams(provider, redirectUrl, loginHint, initiatedEmail);
    if (allowAllMerge) {
      params.put("allowAllMerge", "true");
    }
    return post(COMPOSE_OAUTH_LINK_UPDATE, params, prompt, loginOptions, getApiProxy(refreshToken), authParams);
  }

  private String startAuth(String url, String provider, String redirectUrl, List<OAuthPrompt> prompt,
      String loginHint, String initiatedEmail, LoginOptions loginOptions, String refreshToken,
      Map<String, String> authParams) throws DescopeException {
    Map<String, String> params = composeParams(provider, redirectUrl, loginHint, initiatedEmail);
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    return post(url, params, prompt, loginOptions, apiProxy, authParams);
  }

  private Map<String, String> composeParams(String provider, String redirectUrl, String loginHint,
      String initiatedEmail) {
    if (StringUtils.isBlank(provider)) {
      throw ServerCommonException.invalidArgument("Provider");
    }
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectUrl)) {
      params.put("redirectUrl", redirectUrl);
    }
    if (StringUtils.isNotBlank(loginHint)) {
      params.put("loginHint", loginHint);
    }
    if (StringUtils.isNotBlank(initiatedEmail)) {
      params.put("initiatedEmail", initiatedEmail);
    }
    return params;
  }

  private String post(String url, Map<String, String> params, List<OAuthPrompt> prompt, LoginOptions loginOptions,
      ApiProxy apiProxy, Map<String, String> authParams) {
    StringBuilder uri = new StringBuilder(getQueryParamUri(url, params).toString());
    if (prompt != null) {
      // repeated query param, one per prompt
      for (OAuthPrompt p : prompt) {
        if (p != null) {
          uri.append("&prompt=").append(p.getValue());
        }
      }
    }
    OAuthResponse res = apiProxy.post(URI.create(uri.toString()), toBody(loginOptions), OAuthResponse.class);
    return appendQueryParams(res.getUrl(), authParams);
  }

  // The endpoint requires a body, and rejects LoginOptions' jwt and jwtRequired
  private static Map<String, Object> toBody(LoginOptions loginOptions) {
    Map<String, Object> body = BODY_MAPPER.convertValue(
        loginOptions != null ? loginOptions : new LoginOptions(), new TypeReference<Map<String, Object>>() {});
    body.remove("jwt");
    body.remove("jwtrequired");
    return body;
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
