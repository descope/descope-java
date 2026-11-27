package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_ADD_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_CREATE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_CREATE_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_APPS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_DELETE_TENANT_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_GET_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_LOAD_APPS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_PATCH_APP;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_REVOKE_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_ROTATE_SECRET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_SEARCH_CONSENT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_INBOUND_UPDATE_APP;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppAddSecretResponse;
import com.descope.model.inbound.InboundAppConsent;
import com.descope.model.inbound.InboundAppConsentCreateRequest;
import com.descope.model.inbound.InboundAppConsentCreateResponse;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppLoadAllOptions;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppRevokeSecretResponse;
import com.descope.model.inbound.InboundAppSecret;
import com.descope.model.inbound.InboundAppSecretMeta;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.LoadAllApplicationsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.InboundAppsService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    checkNoCreateOnlyFields(request);

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
    checkNoCreateOnlyFields(request);

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
  public void deleteApplications(List<String> ids) throws DescopeException {
    if (ids == null || ids.isEmpty()) {
      throw ServerCommonException.invalidArgument("ids");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_INBOUND_DELETE_APPS), mapOf("ids", ids), Void.class);
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
  public String getApplicationSecret(String id, String secretName) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, String> params = new HashMap<>();
    params.put("id", id);
    if (StringUtils.isNotEmpty(secretName)) {
      params.put("secretName", secretName);
    }
    ApiProxy apiProxy = getApiProxy();
    InboundAppSecret clearText = apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_GET_SECRET, params),
        InboundAppSecret.class);
    return clearText.getSecret();
  }

  @Override
  public InboundAppAddSecretResponse addApplicationSecret(String id, String name, Integer expireTime)
      throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    Map<String, Object> request = new HashMap<>();
    request.put("id", id);
    request.put("name", name);
    if (expireTime != null) {
      request.put("expireTime", expireTime);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_INBOUND_ADD_SECRET), request, InboundAppAddSecretResponse.class);
  }

  @Override
  public InboundAppSecretMeta[] revokeApplicationSecret(String id, String name) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    InboundAppRevokeSecretResponse res = apiProxy.post(getUri(MANAGEMENT_INBOUND_REVOKE_SECRET),
        mapOf("id", id, "name", name), InboundAppRevokeSecretResponse.class);
    return res.getSecrets();
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
  public LoadAllApplicationsResponse loadAllApplications(InboundAppLoadAllOptions options)
      throws DescopeException {
    Map<String, String> params = new HashMap<>();
    if (options != null) {
      if (options.getPage() != null) {
        if (options.getPage() < 0) {
          throw ServerCommonException.invalidArgument("options.page");
        }
        params.put("page", options.getPage().toString());
      }
      if (options.getLimit() != null) {
        if (options.getLimit() < 0) {
          throw ServerCommonException.invalidArgument("options.limit");
        }
        params.put("limit", options.getLimit().toString());
      }
      if (StringUtils.isNotEmpty(options.getCreatedBy())) {
        params.put("createdBy", options.getCreatedBy());
      }
      if (options.getAgentic() != null) {
        params.put("agentic", options.getAgentic().toString());
      }
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_INBOUND_LOAD_APPS, params), LoadAllApplicationsResponse.class);
  }

  @Override
  public InboundAppConsent createConsent(InboundAppConsentCreateRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getAppId())) {
      throw ServerCommonException.invalidArgument("request.appId");
    }
    if (StringUtils.isBlank(request.getUserId())) {
      throw ServerCommonException.invalidArgument("request.userId");
    }
    ApiProxy apiProxy = getApiProxy();
    InboundAppConsentCreateResponse res = apiProxy.post(getUri(MANAGEMENT_INBOUND_CREATE_CONSENT), request,
        InboundAppConsentCreateResponse.class);
    return res.getConsent();
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

  // Update and patch don't accept these
  private static void checkNoCreateOnlyFields(InboundAppRequest request) {
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