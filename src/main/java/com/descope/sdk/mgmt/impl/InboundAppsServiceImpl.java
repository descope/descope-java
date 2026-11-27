package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_CREATE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_CREATE_APP_V2;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_TENANT_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_GET_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APPS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APPS_V2;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APP_V2;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_PATCH_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_PATCH_APP_V2;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_ROTATE_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_SEARCH_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_UPDATE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_UPDATE_APP_V2;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppLoadAllOptions;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppSecret;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.InboundAppV2;
import com.descope.model.inbound.InboundAppV2Request;
import com.descope.model.inbound.LoadAllApplicationsResponse;
import com.descope.model.inbound.LoadAllApplicationsV2Response;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.InboundAppsService;
import org.apache.commons.lang3.StringUtils;

public class InboundAppsServiceImpl extends ManagementsBase implements InboundAppsService {

  InboundAppsServiceImpl(Client client) {
    super(client);
  }

  @Override
  public InboundAppCreateResponse createApplication(InboundAppRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_INBOUND_CREATE_APP), request, InboundAppCreateResponse.class);
  }

  @Override
  public void updateApplication(InboundAppRequest request) throws DescopeException {
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
    apiProxy.post(getUri(MANAGEMENT_INBOUND_UPDATE_APP), request, Void.class);
  }

  @Override
  public void patchApplication(InboundAppRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }

    if (StringUtils.isBlank(request.getId())) {
      throw ServerCommonException.invalidArgument("request.id");
    }

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_PATCH_APP), request, Void.class);
  }

  @Override
  public void deleteApplication(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_DELETE_APP), mapOf("id", id), Void.class);
  }

  @Override
  public InboundApp loadApplication(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_LOAD_APP, mapOf("id", id)), InboundApp.class);
  }

  @Override
  public InboundApp loadApplicationByClientId(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_LOAD_APP, mapOf("clientId", id)), InboundApp.class);
  }

  @Override
  public String getApplicationSecret(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    InboundAppSecret clearText = apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_GET_SECRET, mapOf("id", id)),
        InboundAppSecret.class);
    return clearText.getSecret();
  }

  @Override
  public String rotateApplicationSecret(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    InboundAppSecret clearText = apiProxy.post(getUri(MANAGEMENT_INBOUND_ROTATE_SECRET), mapOf("id", id),
        InboundAppSecret.class);
    return clearText.getSecret();
  }

  @Override
  public InboundApp[] loadAllApplications() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    LoadAllApplicationsResponse res = apiProxy.get(getUri(MANAGEMENT_INBOUND_LOAD_APPS),
        LoadAllApplicationsResponse.class);
    return res.getApps();
  }

  @Override
  public void deleteConsents(InboundAppConsentDeleteOptions options) throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_DELETE_CONSENT), options, Void.class);
  }

  @Override
  public void deleteTenantConsents(InboundAppTenantConsentDeleteOptions options) throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_DELETE_TENANT_CONSENT), options, Void.class);
  }

  @Override
  public InboundAppConsentSearchResponse searchConsents(InboundAppConsentSearchOptions options)
      throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_INBOUND_SEARCH_CONSENT), options, InboundAppConsentSearchResponse.class);
  }

  @Override
  public InboundAppCreateResponse createApplicationV2(InboundAppV2Request request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    // The v2 create request has no ID
    if (request.getId() != null) {
      throw ServerCommonException.invalidArgument("request.id");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_INBOUND_CREATE_APP_V2), request, InboundAppCreateResponse.class);
  }

  @Override
  public void updateApplicationV2(InboundAppV2Request request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getId())) {
      throw ServerCommonException.invalidArgument("request.id");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }
    if (StringUtils.isBlank(request.getLoginPageUrl())) {
      throw ServerCommonException.invalidArgument("request.loginPageUrl");
    }
    checkNoCreateOnlyFieldsV2(request);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_UPDATE_APP_V2), request, Void.class);
  }

  @Override
  public void patchApplicationV2(InboundAppV2Request request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getId())) {
      throw ServerCommonException.invalidArgument("request.id");
    }
    checkNoCreateOnlyFieldsV2(request);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_PATCH_APP_V2), request, Void.class);
  }

  @Override
  public InboundAppV2 loadApplicationV2(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_LOAD_APP_V2, mapOf("id", id)), InboundAppV2.class);
  }

  @Override
  public InboundAppV2 loadApplicationByClientIdV2(String clientId) throws DescopeException {
    if (StringUtils.isBlank(clientId)) {
      throw ServerCommonException.invalidArgument("clientId");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_LOAD_APP_V2, mapOf("clientId", clientId)),
        InboundAppV2.class);
  }

  @Override
  public LoadAllApplicationsV2Response loadAllApplicationsV2(InboundAppLoadAllOptions options)
      throws DescopeException {
    if (options == null) {
      options = new InboundAppLoadAllOptions();
    }
    if (options.getPage() != null && options.getPage() < 0) {
      throw ServerCommonException.invalidArgument("options.page");
    }
    if (options.getLimit() != null && options.getLimit() < 0) {
      throw ServerCommonException.invalidArgument("options.limit");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_INBOUND_LOAD_APPS_V2), options, LoadAllApplicationsV2Response.class);
  }

  // Update and patch don't accept these
  private static void checkNoCreateOnlyFieldsV2(InboundAppV2Request request) {
    if (request.getConsentFlowId() != null) {
      throw ServerCommonException.invalidArgument("request.consentFlowId");
    }
    if (request.getNonConfidentialClient() != null) {
      throw ServerCommonException.invalidArgument("request.nonConfidentialClient");
    }
    if (request.getClientId() != null) {
      throw ServerCommonException.invalidArgument("request.clientId");
    }
    if (request.getClientSecret() != null) {
      throw ServerCommonException.invalidArgument("request.clientSecret");
    }
  }
}