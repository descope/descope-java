package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_SAML_START_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_SAML_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import java.net.URI;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.SAMLResponse;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.SAMLService;

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
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_SAML_LINK);
  }
}
