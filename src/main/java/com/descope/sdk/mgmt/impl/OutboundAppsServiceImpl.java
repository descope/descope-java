package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_OUTBOUND_APP_TOKENS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_OUTBOUND_APP_TOKEN_BY_ID;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TOKEN;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.OutboundAppsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class OutboundAppsServiceImpl extends ManagementsBase implements OutboundAppsService {

  OutboundAppsServiceImpl(Client client) {
    super(client);
  }

  @Override
  public FetchOutboundAppUserTokenResponse fetchOutboundAppUserToken(FetchOutboundAppUserTokenRequest request)
      throws DescopeException {
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
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_TOKEN), request, FetchOutboundAppUserTokenResponse.class);
  }

  @Override
  public void deleteOutboundAppTokenById(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(getUri(MANAGEMENT_DELETE_OUTBOUND_APP_TOKEN_BY_ID), mapOf("id", id), Void.class);
  }

  @Override
  public void deleteOutboundAppUserTokens(DeleteOutboundAppUserTokensRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getUserId())) {
      throw ServerCommonException.invalidArgument("userId");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(getUri(MANAGEMENT_DELETE_OUTBOUND_APP_TOKENS), request, Void.class);
  }
}