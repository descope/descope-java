package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_IN;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_SIGN_UP;
import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_OAUTH_LINK_UPDATE_USER;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_OAUTH_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.enums.OAuthPrompt;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthAuthorizeOptions;
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
import java.net.URLEncoder;
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

  private String authorize(String route, String provider, String redirectUrl, LoginOptions loginOptions,
      Map<String, String> authParams, OAuthAuthorizeOptions options, String refreshToken, boolean allowAllMerge) {
    Map<String, String> params = mapOf("provider", provider);
    if (StringUtils.isNotBlank(redirectUrl)) {
      params.put("redirectUrl", redirectUrl);
    }
    if (allowAllMerge) {
      params.put("allowAllMerge", "true");
    }
    if (options != null) {
      if (StringUtils.isNotBlank(options.getLoginHint())) {
        params.put("loginHint", options.getLoginHint());
      }
      if (StringUtils.isNotBlank(options.getInitiatedEmail())) {
        params.put("initiatedEmail", options.getInitiatedEmail());
      }
    }
    URI uri = withPrompt(getQueryParamUri(route, params), options);
    ApiProxy apiProxy = getApiProxy(refreshToken);
    OAuthResponse res = apiProxy.post(uri, loginOptionsBody(loginOptions), OAuthResponse.class);
    return appendQueryParams(res.getUrl(), authParams);
  }

  // Step-up and MFA send the refresh JWT from the login options
  private static String stepUpToken(LoginOptions loginOptions) {
    if (!JwtUtils.isJWTRequired(loginOptions)) {
      return null;
    }
    if (StringUtils.isBlank(loginOptions.getJwt())) {
      throw ServerCommonException.invalidArgument("JWT");
    }
    return loginOptions.getJwt();
  }

  // LoginOptions also serializes jwt and jwtrequired, which the API doesn't accept
  private static Map<String, Object> loginOptionsBody(LoginOptions loginOptions) {
    Map<String, Object> body = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
        .convertValue(loginOptions != null ? loginOptions : new LoginOptions(),
            new TypeReference<Map<String, Object>>() {});
    body.remove("jwt");
    body.remove("jwtrequired");
    return body;
  }

  // prompt is an array query param: one entry per value
  @SneakyThrows
  private static URI withPrompt(URI uri, OAuthAuthorizeOptions options) {
    if (options == null || options.getPrompt() == null || options.getPrompt().isEmpty()) {
      return uri;
    }
    StringBuilder sb = new StringBuilder(uri.toString());
    for (OAuthPrompt prompt : options.getPrompt()) {
      if (prompt != null) {
        sb.append("&prompt=").append(URLEncoder.encode(prompt.getValue(), "UTF-8"));
      }
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
      Map<String, String> authParams, OAuthAuthorizeOptions options) throws DescopeException {
    return authorize(COMPOSE_OAUTH_LINK, provider, redirectURL, loginOptions, authParams, options,
        stepUpToken(loginOptions), false);
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
      Map<String, String> authParams, OAuthAuthorizeOptions options) throws DescopeException {
    return authorize(COMPOSE_OAUTH_LINK_SIGN_IN, provider, redirectURL, loginOptions, authParams, options,
        stepUpToken(loginOptions), false);
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
      Map<String, String> authParams, OAuthAuthorizeOptions options) throws DescopeException {
    return authorize(COMPOSE_OAUTH_LINK_SIGN_UP, provider, redirectURL, loginOptions, authParams, options,
        stepUpToken(loginOptions), false);
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  @Override
  public String updateUser(String provider, String redirectURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions) throws DescopeException {
    return updateUser(provider, redirectURL, refreshToken, allowAllMerge, loginOptions, null);
  }

  @Override
  public String updateUser(String provider, String redirectURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions, OAuthAuthorizeOptions options) throws DescopeException {
    if (StringUtils.isBlank(refreshToken)) {
      throw ServerCommonException.invalidArgument("Refresh Token");
    }
    return authorize(COMPOSE_OAUTH_LINK_UPDATE_USER, provider, redirectURL, loginOptions, null, options,
        refreshToken, allowAllMerge);
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_OAUTH_LINK);
  }
}
