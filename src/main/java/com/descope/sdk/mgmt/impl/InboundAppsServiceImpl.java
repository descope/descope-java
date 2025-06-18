package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_CREATE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_TENANT_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_GET_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APPS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_PATCH_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_ROTATE_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_SEARCH_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_UPDATE_APP;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppSecret;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.LoadAllApplicationsResponse;
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
}