package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_SAML_START_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_SAML_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.SAMLResponse;
import com.descope.model.auth.SAMLStartOptions;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.SAMLService;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

class SAMLServiceImpl extends AuthenticationServiceImpl implements SAMLService {

  SAMLServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String start(String tenant, String returnURL, LoginOptions loginOptions)
      throws DescopeException {
    return start(tenant, returnURL, loginOptions, null).getUrl();
  }

  @Override
  public SAMLResponse start(String tenant, String returnURL, LoginOptions loginOptions, SAMLStartOptions options)
      throws DescopeException {
    Map<String, String> params = mapOf("tenant", tenant);
    if (StringUtils.isNotBlank(returnURL)) {
      params.put("redirectURL", returnURL);
    }
    if (options != null) {
      if (Boolean.TRUE.equals(options.getForceAuthn())) {
        params.put("forceAuthn", "true");
      }
      if (StringUtils.isNotBlank(options.getLoginHint())) {
        params.put("loginHint", options.getLoginHint());
      }
      if (StringUtils.isNotBlank(options.getInitiatedEmail())) {
        params.put("initiatedEmail", options.getInitiatedEmail());
      }
    }
    URI samlStartURL = getQueryParamUri(COMPOSE_SAML_START_LINK, params);
    if (options != null && options.getPrompt() != null) {
      samlStartURL = appendPrompt(samlStartURL, options);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(samlStartURL, loginOptions, SAMLResponse.class);
  }

  // prompt is an array: one query param per value
  @SneakyThrows
  private URI appendPrompt(URI uri, SAMLStartOptions options) {
    StringBuilder sb = new StringBuilder(uri.toString());
    for (String p : options.getPrompt()) {
      if (StringUtils.isNotBlank(p)) {
        sb.append('&').append("prompt=").append(URLEncoder.encode(p, "UTF-8"));
      }
    }
    return new URI(sb.toString());
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_SAML_LINK);
  }
}
