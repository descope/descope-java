package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_SSO_START_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_SSO_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.SSOServiceProvider;
import com.descope.utils.JwtUtils;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class SSOServiceProviderImpl extends AuthenticationServiceImpl implements SSOServiceProvider {

  SSOServiceProviderImpl(Client client) {
    super(client);
  }

  @Override
  public String start(String tenant, String redirectUrl, String prompt, LoginOptions loginOptions) {
    return start(tenant, redirectUrl, prompt, loginOptions, null);
  }

  @Override
  public String start(String tenant, String redirectUrl, String prompt, LoginOptions loginOptions, String refreshToken)
      throws DescopeException {
    if (StringUtils.isBlank(tenant)) {
      throw ServerCommonException.invalidArgument("Tenant");
    }
    Map<String, String> request = mapOf("tenant", tenant);
    if (StringUtils.isNotBlank(redirectUrl)) {
      request.put("redirectURL", redirectUrl);
    }
    if (StringUtils.isNotBlank(prompt)) {
      request.put("prompt", prompt);
    }
    ApiProxy apiProxy;
    if (JwtUtils.isJWTRequired(loginOptions)) {
      if (StringUtils.isBlank(refreshToken)) {
        throw ServerCommonException.invalidArgument("refreshToken");
      }
      apiProxy = getApiProxy(refreshToken);
    } else {
      apiProxy = getApiProxy();
    }
    return apiProxy.post(getQueryParamUri(COMPOSE_SSO_START_LINK, request), loginOptions, OAuthResponse.class).getUrl();
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, getUri(EXCHANGE_SSO_LINK));
  }
}
