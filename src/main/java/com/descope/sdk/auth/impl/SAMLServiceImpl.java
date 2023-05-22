package com.descope.sdk.auth.impl;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.sdk.auth.SAMLService;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_SAML_START_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_SAML_LINK;

class SAMLServiceImpl extends AuthenticationServiceImpl implements SAMLService {

  SAMLServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String start(String tenant, String returnURL, LoginOptions loginOptions) throws DescopeException {
    Map<String, String> request = new HashMap<>();
    request.put("provider", tenant);
    if (StringUtils.isNotBlank(returnURL)) {
      request.put("redirectURL", returnURL);
    }
    URI samlStartURLAML = composeSAMLStartURL();
    var apiProxy = getApiProxy();
    return apiProxy.post(samlStartURLAML, request, String.class);
  }

  @Override
  public AuthenticationInfo exchangeToken(String code) throws DescopeException {
    return exchangeToken(code, composeOAuthExchangeTokenURL());
  }

  private URI composeSAMLStartURL() {
    return getUri(COMPOSE_SAML_START_LINK);
  }

  private URI composeOAuthExchangeTokenURL() {
    return getUri(EXCHANGE_SAML_LINK);
  }
}
