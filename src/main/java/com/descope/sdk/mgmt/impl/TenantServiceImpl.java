package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.GENERATE_SSO_CONFIGURATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.GET_TENANT_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_TENANTS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.REVOKE_SSO_CONFIGURATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.TENANT_SEARCH_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_TENANT_LINK;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.response.GenerateTenantLinkResponse;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.TenantService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
class TenantServiceImpl extends ManagementsBase implements TenantService {

  TenantServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String create(String name, List<String> selfProvisioningDomains) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    return create(Tenant.builder().name(name).selfProvisioningDomains(selfProvisioningDomains).build());
  }

  @Override
  public String create(String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    return create(Tenant.builder()
        .name(name)
        .selfProvisioningDomains(selfProvisioningDomains)
        .customAttributes(customAttributes)
        .build());
  }

  @Override
  public void createWithId(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }
    create(Tenant.builder().id(id).name(name).selfProvisioningDomains(selfProvisioningDomains).build());
  }

  @Override
  public void createWithId(String id, String name, List<String> selfProvisioningDomains,
      Map<String, Object> customAttributes)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }
    create(Tenant.builder()
        .id(id)
        .name(name)
        .selfProvisioningDomains(selfProvisioningDomains)
        .customAttributes(customAttributes)
        .build());
  }

  private String create(Tenant tenant) {
    URI createTenantUri = composeCreateTenantUri();
    ApiProxy apiProxy = getApiProxy();
    Tenant savedTenant = apiProxy.post(createTenantUri, tenant, Tenant.class);
    return savedTenant.getId();
  }

  @Override
  public void update(String id, String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }
    update(Tenant.builder()
        .id(id)
        .name(name)
        .selfProvisioningDomains(selfProvisioningDomains)
        .customAttributes(customAttributes)
        .build());
  }

  private void update(Tenant tenant) {
    URI updateTenantUri = composeUpdateTenantUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(updateTenantUri, tenant, Void.class);
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    URI deleteTenantUri = composeDeleteTenantUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(deleteTenantUri, mapOf("id", id), Void.class);
  }

  @Override
  public Tenant load(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(loadTenantUri(id), Tenant.class);
  }

  @Override
  public List<Tenant> loadAll() throws DescopeException {
    URI loadAllTenantsUri = loadAllTenantsUri();
    ApiProxy apiProxy = getApiProxy();
    GetAllTenantsResponse response = apiProxy.get(loadAllTenantsUri, GetAllTenantsResponse.class);
    return response.getTenants();
  }

  @Override
  public List<Tenant> searchAll(TenantSearchRequest request)
      throws DescopeException {
    if (request == null) {
      request = TenantSearchRequest.builder().build();
    }

    URI composeSearchAllUri = composeSearchAllUri();
    ApiProxy apiProxy = getApiProxy();
    GetAllTenantsResponse response = apiProxy.post(composeSearchAllUri, request, GetAllTenantsResponse.class);
    return response.getTenants();
  }

  @Override
  public TenantSettings getSettings(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getSettingsUri(id), TenantSettings.class);
  }

  @Override
  public void configureSettings(String id, TenantSettings settings) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
    if (settings.getAuthType() == null && settings.getJitDisabled() != null) {
      throw ServerCommonException.invalidArgument("settings.authType");
    }
    Map<String, Object> req = mapOf("tenantId", id);
    addIfNotNull(req, "selfProvisioningDomains", settings.getSelfProvisioningDomains());
    addIfNotNull(req, "enabled", settings.getSessionSettingsEnabled());
    addIfNotNull(req, "sessionTokenExpiration", settings.getSessionTokenExpiration());
    addIfNotNull(req, "refreshTokenExpiration", settings.getRefreshTokenExpiration());
    addIfNotNull(req, "sessionTokenExpirationUnit", settings.getSessionTokenExpirationUnit());
    addIfNotNull(req, "refreshTokenExpirationUnit", settings.getRefreshTokenExpirationUnit());
    addIfNotNull(req, "inactivityTime", settings.getInactivityTime());
    addIfNotNull(req, "inactivityTimeUnit", settings.getInactivityTimeUnit());
    addIfNotNull(req, "enableInactivity", settings.getEnableInactivity());
    addIfNotNull(req, "domains", settings.getDomains());
    addIfNotNull(req, "JITDisabled", settings.getJitDisabled());
    addIfNotNull(req, "authType", settings.getAuthType());
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(configureSettingsUri(), req, Void.class);
  }

  @Override
  public String generateSSOConfigurationLink(GenerateTenantLinkRequest request) throws DescopeException {
    if (request == null) {
      request = GenerateTenantLinkRequest.builder().build();
    }

    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, Object> req = mapOf(
        "tenantId", request.getTenantId(),
        "expireTime", request.getExpireDuration(),
        "ssoId", request.getSsoId(),
        "email", request.getEmail(),
        "templateId", request.getTemplateId()
    );
    
    URI generateSSOConfigurationLinkUri = generateSSOConfigurationLinkUri();
    ApiProxy apiProxy = getApiProxy();
    GenerateTenantLinkResponse response = apiProxy.post(
        generateSSOConfigurationLinkUri, req, GenerateTenantLinkResponse.class);
    return response.getAdminSSOConfigurationLink();
  }

  @Override
  public void revokeSSOConfigurationLink(String tenantId, String ssoID) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, Object> req = mapOf("tenantId", tenantId, "ssoId", ssoID);

    URI revokeSSOConfigurationLinkUri = revokeSSOConfigurationLinkUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(revokeSSOConfigurationLinkUri, req, Void.class);
  }

  private URI composeCreateTenantUri() {
    return getUri(CREATE_TENANT_LINK);
  }

  private URI composeUpdateTenantUri() {
    return getUri(UPDATE_TENANT_LINK);
  }

  private URI composeDeleteTenantUri() {
    return getUri(DELETE_TENANT_LINK);
  }

  private URI loadTenantUri(String id) {
    return getQueryParamUri(LOAD_TENANT_LINK, mapOf("id", id));
  }

  private URI loadAllTenantsUri() {
    return getUri(LOAD_ALL_TENANTS_LINK);
  }

  private URI composeSearchAllUri() {
    return getUri(TENANT_SEARCH_ALL_LINK);
  }

  private URI getSettingsUri(String id) {
    return getQueryParamUri(GET_TENANT_SETTINGS_LINK, mapOf("id", id));
  }

  private URI configureSettingsUri() {
    return getUri(GET_TENANT_SETTINGS_LINK);
  }

  private URI generateSSOConfigurationLinkUri() {
    return getUri(GENERATE_SSO_CONFIGURATION_LINK);
  }

  private URI revokeSSOConfigurationLinkUri() {
    return getUri(REVOKE_SSO_CONFIGURATION_LINK);
  }
}
