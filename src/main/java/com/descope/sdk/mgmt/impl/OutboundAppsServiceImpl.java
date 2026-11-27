package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_OUTBOUND_APP_USER_TOKENS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_OUTBOUND_APP_USER_TOKEN_BY_ID;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_TENANT_TOKEN_LATEST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FETCH_OUTBOUND_APP_USER_TOKEN_LATEST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_BATCH_UPLOAD_TENANT_TOKENS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_BATCH_UPLOAD_USER_TOKENS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_CREATE_BY_TEMPLATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_LIST_WITH_USER_TOKEN_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_UPDATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_UPLOAD_TENANT_API_KEY;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_UPLOAD_TENANT_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_UPLOAD_USER_API_KEY;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_APP_UPLOAD_USER_TOKEN;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_SCIM_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_SCIM_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_SCIM_SET_ENABLED;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_OUTBOUND_SCIM_UPDATE;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.outbound.BatchUploadOutboundAppTokensResponse;
import com.descope.model.outbound.CreateOutboundAppByTemplateRequest;
import com.descope.model.outbound.CreateOutboundSCIMConfigurationRequest;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.model.outbound.ListOutboundAppsWithUserTokenResponse;
import com.descope.model.outbound.LoadAllOutboundApplicationsResponse;
import com.descope.model.outbound.OutboundApp;
import com.descope.model.outbound.OutboundAppCreateResponse;
import com.descope.model.outbound.OutboundAppRequest;
import com.descope.model.outbound.OutboundAppResponse;
import com.descope.model.outbound.OutboundAppTenantTokenUploadItem;
import com.descope.model.outbound.OutboundAppTokenUploadFailure;
import com.descope.model.outbound.OutboundAppUserTokenUploadItem;
import com.descope.model.outbound.OutboundSCIMConfiguration;
import com.descope.model.outbound.OutboundSCIMConfigurationResponse;
import com.descope.model.outbound.UpdateOutboundSCIMConfigurationRequest;
import com.descope.model.outbound.UploadOutboundAppTenantAPIKeyRequest;
import com.descope.model.outbound.UploadOutboundAppTenantTokenRequest;
import com.descope.model.outbound.UploadOutboundAppUserAPIKeyRequest;
import com.descope.model.outbound.UploadOutboundAppUserTokenRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.OutboundAppsService;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
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

  @Override
  public OutboundApp createApplicationByTemplate(CreateOutboundAppByTemplateRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getTemplateId())) {
      throw ServerCommonException.invalidArgument("request.templateId");
    }
    ApiProxy apiProxy = getApiProxy();
    OutboundAppResponse res = apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_CREATE_BY_TEMPLATE_LINK), request,
        OutboundAppResponse.class);
    return res.getApp();
  }

  @Override
  public List<String> listAppsWithUserToken(String userId, String tenantId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("userId");
    }
    Map<String, String> params = new HashMap<>();
    params.put("userId", userId);
    if (StringUtils.isNotBlank(tenantId)) {
      params.put("tenantId", tenantId);
    }
    ApiProxy apiProxy = getApiProxy();
    ListOutboundAppsWithUserTokenResponse res = apiProxy.get(
        getQueryParamUri(MANAGEMENT_OUTBOUND_APP_LIST_WITH_USER_TOKEN_LINK, params),
        ListOutboundAppsWithUserTokenResponse.class);
    return res.getAppIds() != null ? res.getAppIds() : new ArrayList<>();
  }

  @Override
  public void uploadUserAPIKey(UploadOutboundAppUserAPIKeyRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getUserId())) {
      throw ServerCommonException.invalidArgument("userId");
    }
    if (StringUtils.isBlank(request.getApiKey())) {
      throw ServerCommonException.invalidArgument("apiKey");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_UPLOAD_USER_API_KEY), request, Void.class);
  }

  @Override
  public void uploadTenantAPIKey(UploadOutboundAppTenantAPIKeyRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("tenantId");
    }
    if (StringUtils.isBlank(request.getApiKey())) {
      throw ServerCommonException.invalidArgument("apiKey");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_UPLOAD_TENANT_API_KEY), request, Void.class);
  }

  @Override
  public void uploadUserToken(UploadOutboundAppUserTokenRequest request) throws DescopeException {
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
    apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_UPLOAD_USER_TOKEN), request, Void.class);
  }

  @Override
  public void uploadTenantToken(UploadOutboundAppTenantTokenRequest request) throws DescopeException {
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
    apiProxy.post(getUri(MANAGEMENT_OUTBOUND_APP_UPLOAD_TENANT_TOKEN), request, Void.class);
  }

  @Override
  public List<OutboundAppTokenUploadFailure> batchUploadUserTokens(List<OutboundAppUserTokenUploadItem> tokens)
      throws DescopeException {
    if (CollectionUtils.isEmpty(tokens)) {
      throw ServerCommonException.invalidArgument("tokens");
    }
    return batchUploadTokens(MANAGEMENT_OUTBOUND_APP_BATCH_UPLOAD_USER_TOKENS, tokens);
  }

  @Override
  public List<OutboundAppTokenUploadFailure> batchUploadTenantTokens(List<OutboundAppTenantTokenUploadItem> tokens)
      throws DescopeException {
    if (CollectionUtils.isEmpty(tokens)) {
      throw ServerCommonException.invalidArgument("tokens");
    }
    return batchUploadTokens(MANAGEMENT_OUTBOUND_APP_BATCH_UPLOAD_TENANT_TOKENS, tokens);
  }

  private List<OutboundAppTokenUploadFailure> batchUploadTokens(String path, List<?> tokens) {
    ApiProxy apiProxy = getApiProxy();
    BatchUploadOutboundAppTokensResponse res =
        apiProxy.post(getUri(path), mapOf("tokens", tokens), BatchUploadOutboundAppTokensResponse.class);
    return res.getFailures() != null ? res.getFailures() : new ArrayList<>();
  }

  @Override
  public OutboundSCIMConfiguration createSCIMConfiguration(CreateOutboundSCIMConfigurationRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    if (request.getConfiguration() == null) {
      throw ServerCommonException.invalidArgument("configuration");
    }
    ApiProxy apiProxy = getApiProxy();
    OutboundSCIMConfigurationResponse res =
        apiProxy.post(getUri(MANAGEMENT_OUTBOUND_SCIM_CREATE), request, OutboundSCIMConfigurationResponse.class);
    return res.getConfiguration();
  }

  @Override
  public OutboundSCIMConfiguration updateSCIMConfiguration(UpdateOutboundSCIMConfigurationRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("appId");
    }
    ApiProxy apiProxy = getApiProxy();
    OutboundSCIMConfigurationResponse res =
        apiProxy.post(getUri(MANAGEMENT_OUTBOUND_SCIM_UPDATE), request, OutboundSCIMConfigurationResponse.class);
    return res.getConfiguration();
  }

  @Override
  public OutboundSCIMConfiguration setSCIMEnabled(String appId, boolean enabled) throws DescopeException {
    if (StringUtils.isBlank(appId)) {
      throw ServerCommonException.invalidArgument("appId");
    }
    ApiProxy apiProxy = getApiProxy();
    OutboundSCIMConfigurationResponse res = apiProxy.post(getUri(MANAGEMENT_OUTBOUND_SCIM_SET_ENABLED),
        mapOf("appId", (Object) appId, "enabled", enabled), OutboundSCIMConfigurationResponse.class);
    return res.getConfiguration();
  }


  @Override
  @SneakyThrows
  public OutboundSCIMConfiguration loadSCIMConfiguration(String appId) throws DescopeException {
    if (StringUtils.isBlank(appId)) {
      throw ServerCommonException.invalidArgument("appId");
    }
    ApiProxy apiProxy = getApiProxy();
    String path = MANAGEMENT_OUTBOUND_SCIM_LOAD + URLEncoder.encode(appId, "UTF-8").replace("+", "%20");
    OutboundSCIMConfigurationResponse res = apiProxy.get(getUri(path), OutboundSCIMConfigurationResponse.class);
    return res.getConfiguration();
  }
}
