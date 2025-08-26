package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN_LATEST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN_LATEST;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.OutboundAppsByTokenService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

class OutboundAppsByTokenServiceImpl extends ManagementsBase implements OutboundAppsByTokenService {

  OutboundAppsByTokenServiceImpl(Client client) {
    super(client);
  }

  @Override
  public FetchOutboundAppUserTokenResponse fetchOutboundAppUserTokenByScopes(
      String token, FetchOutboundAppUserTokenRequest request) throws DescopeException {
    validateToken(token);
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getUserId())) {
      throw ServerCommonException.invalidArgument("userId");
    }
    if (CollectionUtils.isEmpty(request.getScopes())) {
      throw ServerCommonException.invalidArgument("scopes");
    }
    ApiProxy apiProxy = getApiProxyWithBearer(token);
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN), request,
        FetchOutboundAppUserTokenResponse.class);
  }

  @Override
  public FetchOutboundAppUserTokenResponse fetchLatestOutboundAppUserToken(
      String token, FetchLatestOutboundAppUserTokenRequest request) throws DescopeException {
    validateToken(token);
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getUserId())) {
      throw ServerCommonException.invalidArgument("userId");
    }
    ApiProxy apiProxy = getApiProxyWithBearer(token);
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN_LATEST), request,
        FetchOutboundAppUserTokenResponse.class);
  }

  @Override
  public FetchOutboundAppTenantTokenResponse fetchOutboundAppTenantTokenByScopes(
      String token, FetchOutboundAppTenantTokenRequest request) throws DescopeException {
    validateToken(token);
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("tenantId");
    }
    if (CollectionUtils.isEmpty(request.getScopes())) {
      throw ServerCommonException.invalidArgument("scopes");
    }
    ApiProxy apiProxy = getApiProxyWithBearer(token);
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN), request,
        FetchOutboundAppTenantTokenResponse.class);
  }

  @Override
  public FetchOutboundAppTenantTokenResponse fetchLatestOutboundAppTenantToken(
      String token, FetchOutboundAppTenantTokenRequest request) throws DescopeException {
    validateToken(token);
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("tenantId");
    }
    ApiProxy apiProxy = getApiProxyWithBearer(token);
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN_LATEST), request,
        FetchOutboundAppTenantTokenResponse.class);
  }

  private void validateToken(String token) {
    if (StringUtils.isBlank(token)) {
      throw ServerCommonException.invalidArgument("token");
    }
  }
}
