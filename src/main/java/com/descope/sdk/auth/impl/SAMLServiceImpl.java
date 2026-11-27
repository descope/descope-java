package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_SAML_START_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_SAML_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.enums.PromptType;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.SAMLResponse;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.SAMLService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class SAMLServiceImpl extends AuthenticationServiceImpl implements SAMLService {

  SAMLServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String start(String tenant, String returnURL, LoginOptions loginOptions)
      throws DescopeException {
    Map<String, String> params = mapOf("tenant", tenant);
    if (StringUtils.isNotBlank(returnURL)) {
      params.put("redirectURL", returnURL);
    }
    URI samlStartURL = getQueryParamUri(COMPOSE_SAML_START_LINK, params);
    ApiProxy apiProxy = getApiProxy();
    SAMLResponse response = apiProxy.post(samlStartURL, loginOptions, SAMLResponse.class);
    return response.getUrl();
  }

  @Override
  public String start(String tenant, String returnURL, LoginOptions loginOptions, String refreshToken,
      List<PromptType> prompt, boolean forceAuthn, String loginHint, String initiatedEmail)
      throws DescopeException {
    Map<String, String> params = new LinkedHashMap<>();
    if (StringUtils.isNotBlank(tenant)) {
      params.put("tenant", tenant);
    }
    if (StringUtils.isNotBlank(returnURL)) {
      params.put("redirectUrl", returnURL);
    }
    if (forceAuthn) {
      params.put("forceAuthn", "true");
    }
    if (StringUtils.isNotBlank(loginHint)) {
      params.put("loginHint", loginHint);
    }
    if (StringUtils.isNotBlank(initiatedEmail)) {
      params.put("initiatedEmail", initiatedEmail);
    }
    URI samlStartURL = getQueryParamUri(COMPOSE_SAML_START_LINK, params);
    if (prompt != null) {
      // Repeated query parameter, one per prompt
      StringBuilder sb = new StringBuilder(samlStartURL.toString());
      for (PromptType p : prompt) {
        if (p != null) {
          sb.append(sb.indexOf("?") < 0 ? '?' : '&').append("prompt=").append(p.getValue());
        }
      }
      samlStartURL = URI.create(sb.toString());
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
    SAMLResponse response = apiProxy.post(samlStartURL, loginOptions, SAMLResponse.class);
    return response.getUrl();
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_SAML_LINK);
  }
}
