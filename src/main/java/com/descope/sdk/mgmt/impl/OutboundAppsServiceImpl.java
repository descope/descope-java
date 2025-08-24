package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_OUTBOUND_APP_USER_TOKENS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_OUTBOUND_APP_USER_TOKEN_BY_ID;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN_LATEST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN_LATEST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.model.outbound.LoadAllOutboundApplicationsResponse;
import com.descope.model.outbound.OutboundApp;
import com.descope.model.outbound.OutboundAppCreateResponse;
import com.descope.model.outbound.OutboundAppRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.OutboundAppsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class OutboundAppsServiceImpl extends ManagementsBase implements OutboundAppsService {

  OutboundAppsServiceImpl(Client client) {
    super(client);
  }

  // ----- CRUD -----
  @Override
  public OutboundAppCreateResponse createApplication(OutboundAppRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_CREATE_LINK), request, OutboundAppCreateResponse.class);
  }

  @Override
  public void updateApplication(OutboundAppRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getId())) {
      throw ServerCommonException.invalidArgument("request.id");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_UPDATE_LINK), request, Void.class);
  }

  @Override
  public void deleteApplication(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public OutboundApp loadApplication(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_OUTBOUND_APP_LOAD_LINK, mapOf("id", id)), OutboundApp.class);
  }

  @Override
  public OutboundApp[] loadAllApplications() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    LoadAllOutboundApplicationsResponse res =
        apiProxy.get(getUri(MANAGEMENT_OUTBOUND_APP_LOAD_ALL_LINK), LoadAllOutboundApplicationsResponse.class);
    return res.getApps();
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
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN), request, 
      FetchOutboundAppUserTokenResponse.class);
  }

  @Override
  public FetchOutboundAppUserTokenResponse fetchLatestOutboundAppUserToken(
      FetchLatestOutboundAppUserTokenRequest request) throws DescopeException {
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
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN_LATEST), request,
        FetchOutboundAppUserTokenResponse.class);
  }

  @Override
  public void deleteOutboundAppTokenById(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(getUri(MANAGEMENT_DELETE_OUTBOUND_APP_USER_TOKEN_BY_ID), mapOf("id", id), Void.class);
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
    apiProxy.delete(getUri(MANAGEMENT_DELETE_OUTBOUND_APP_USER_TOKENS), request, Void.class);
  }

  @Override
  public FetchOutboundAppTenantTokenResponse fetchOutboundAppTenantTokenByScopes(
      FetchOutboundAppTenantTokenRequest request) throws DescopeException {
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
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN), request,
        FetchOutboundAppTenantTokenResponse.class);
  }

  @Override
  public FetchOutboundAppTenantTokenResponse fetchLatestOutboundAppTenantToken(
      FetchOutboundAppTenantTokenRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("tenantId");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN_LATEST), request,
        FetchOutboundAppTenantTokenResponse.class);
  }
}