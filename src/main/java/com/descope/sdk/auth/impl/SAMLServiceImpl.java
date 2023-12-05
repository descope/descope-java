package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.COMPOSE_SAML_START_LINK;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_SAML_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.magiclink.LoginOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.auth.SAMLService;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class SAMLServiceImpl extends AuthenticationServiceImpl implements SAMLService {

  SAMLServiceImpl(Client client, AuthParams authParams) {
    super(client, authParams);
  }

  @Override
  public String start(String tenant, String returnURL, LoginOptions loginOptions)
      throws DescopeException {
    Map<String, String> request = mapOf("provider", tenant);
    if (StringUtils.isNotBlank(returnURL)) {
      request.put("redirectURL", returnURL);
    }
    URI samlStartURLAML = composeSAMLStartURL();
    ApiProxy apiProxy = getApiProxy();
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
