package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_IN;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_UP;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_OAUTH_LINK;
import static com.descope.literals.Routes.AuthEndPoints.UPDATE_USER_OAUTH_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.auth.OAuthStartOptions;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.OAuthService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

class OAuthServiceImpl extends AuthenticationServiceImpl implements OAuthService {
  private static final ObjectMapper LOGIN_OPTIONS_MAPPER =
      new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

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
  public OAuthResponse startWithOptions(String provider, String returnURL, LoginOptions loginOptions,
      OAuthStartOptions options) throws DescopeException {
    return startWithParams(COMPOSE_OAUTH_LINK, provider, returnURL, null, null, loginOptions, options);
  }

  @Override
  public OAuthResponse startSignInWithOptions(String provider, String returnURL, LoginOptions loginOptions,
      OAuthStartOptions options) throws DescopeException {
    return startWithParams(COMPOSE_OAUTH_LINK_SIGN_IN, provider, returnURL, null, null, loginOptions, options);
  }

  @Override
  public OAuthResponse startSignUpWithOptions(String provider, String returnURL, LoginOptions loginOptions,
      OAuthStartOptions options) throws DescopeException {
    return startWithParams(COMPOSE_OAUTH_LINK_SIGN_UP, provider, returnURL, null, null, loginOptions, options);
  }

  @Override
  public OAuthResponse updateUser(String provider, String returnURL, String refreshToken, Boolean allowAllMerge,
      LoginOptions loginOptions, OAuthStartOptions options) throws DescopeException {
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    if (options != null && options.getRawResponse() != null) {
      throw ServerCommonException.invalidArgument("rawResponse");
    }
    return startWithParams(UPDATE_USER_OAUTH_LINK, provider, returnURL, refreshToken, allowAllMerge, loginOptions,
        options);
  }

  private OAuthResponse startWithParams(String path, String provider, String returnURL, String refreshToken,
      Boolean allowAllMerge, LoginOptions loginOptions, OAuthStartOptions options) {
    StringBuilder query = new StringBuilder();
    appendQueryParam(query, "provider", provider);
    if (StringUtils.isNotBlank(returnURL)) {
      appendQueryParam(query, "redirectUrl", returnURL);
    }
    if (allowAllMerge != null) {
      appendQueryParam(query, "allowAllMerge", allowAllMerge.toString());
    }
    if (options != null) {
      if (options.getPrompt() != null) {
        for (String prompt : options.getPrompt()) {
          if (StringUtils.isNotBlank(prompt)) {
            appendQueryParam(query, "prompt", prompt);
          }
        }
      }
      if (options.getRawResponse() != null) {
        appendQueryParam(query, "rawResponse", options.getRawResponse().toString());
      }
      if (StringUtils.isNotBlank(options.getLoginHint())) {
        appendQueryParam(query, "loginHint", options.getLoginHint());
      }
      if (StringUtils.isNotBlank(options.getInitiatedEmail())) {
        appendQueryParam(query, "initiatedEmail", options.getInitiatedEmail());
      }
    }
    ApiProxy apiProxy = getApiProxy(refreshToken);
    OAuthResponse res = apiProxy.post(getUri(path + query), loginOptionsBody(loginOptions), OAuthResponse.class);
    if (options != null && res != null && StringUtils.isNotBlank(res.getUrl())) {
      res.setUrl(appendQueryParams(res.getUrl(), options.getAuthParams()));
    }
    return res;
  }

  // isJWTRequired() is not a request field
  private static Map<String, Object> loginOptionsBody(LoginOptions loginOptions) {
    if (loginOptions == null) {
      return new HashMap<>();
    }
    Map<String, Object> body =
        LOGIN_OPTIONS_MAPPER.convertValue(loginOptions, new TypeReference<Map<String, Object>>() {});
    body.remove("jwtrequired");
    return body;
  }

  // Repeats a key for array params like prompt
  @SneakyThrows
  private static void appendQueryParam(StringBuilder query, String key, String value) {
    query.append(query.length() == 0 ? '?' : '&')
        .append(URLEncoder.encode(key, "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8"));
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
