package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_TENANTS_BATCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.CREATE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_TENANTS_BATCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.GENERATE_SSO_CONFIGURATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.GET_TENANT_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_TENANTS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.PATCH_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.REVOKE_SSO_CONFIGURATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.TENANT_SEARCH_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.TENANT_UPDATE_DEFAULT_ROLES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_TENANTS_BATCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_TENANT_LINK;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.CreateTenantRequest;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.PatchTenantRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.request.UpdateTenantRequest;
import com.descope.model.tenant.response.CreateTenantResponse;
import com.descope.model.tenant.response.CreateTenantsBatchResponse;
import com.descope.model.tenant.response.DeleteTenantsBatchResponse;
import com.descope.model.tenant.response.GenerateTenantLinkResponse;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.model.tenant.response.UpdateTenantsBatchResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.TenantService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
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

  @Override
  public String create(CreateTenantRequest request) throws DescopeException {
    if (request == null || StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    CreateTenantResponse response = apiProxy.post(composeCreateTenantUri(), request, CreateTenantResponse.class);
    return response.getId();
  }

  @Override
  public CreateTenantsBatchResponse createBatch(List<CreateTenantRequest> tenants, Boolean strict)
      throws DescopeException {
    if (CollectionUtils.isEmpty(tenants)) {
      throw ServerCommonException.invalidArgument("tenants");
    }
    Map<String, Object> req = mapOf("tenants", tenants);
    addIfNotNull(req, "strict", strict);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(CREATE_TENANTS_BATCH_LINK), req, CreateTenantsBatchResponse.class);
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
    update(id, name, selfProvisioningDomains, customAttributes, null, null, null, null, null, null);
  }

  @Override
  public void update(String id, String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes,
      String authType, Boolean disabled, Boolean enforceSSO, List<String> enforceSSOExclusions,
      List<String> federatedAppIds, String roleInheritance) throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }
    URI updateTenantUri = composeUpdateTenantUri();
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(updateTenantUri, Tenant.builder()
        .id(id)
        .name(name)
        .selfProvisioningDomains(selfProvisioningDomains)
        .customAttributes(customAttributes)
        .authType(authType)
        .disabled(disabled)
        .enforceSSO(enforceSSO)
        .enforceSSOExclusions(enforceSSOExclusions)
        .federatedAppIds(federatedAppIds)
        .roleInheritance(roleInheritance)
        .build(), Void.class);
  }

  @Override
  public void update(UpdateTenantRequest request) throws DescopeException {
    if (request == null || StringUtils.isAnyBlank(request.getId(), request.getName())) {
      throw ServerCommonException.invalidArgument("id or name");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(composeUpdateTenantUri(), request, Void.class);
  }

  @Override
  public UpdateTenantsBatchResponse updateBatch(List<UpdateTenantRequest> tenants, Boolean strict)
      throws DescopeException {
    if (CollectionUtils.isEmpty(tenants)) {
      throw ServerCommonException.invalidArgument("tenants");
    }
    Map<String, Object> req = mapOf("tenants", tenants);
    addIfNotNull(req, "strict", strict);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(UPDATE_TENANTS_BATCH_LINK), req, UpdateTenantsBatchResponse.class);
  }

  @Override
  public void patch(String id, PatchTenantRequest request) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (request == null) {
      request = new PatchTenantRequest();
    }
    Map<String, Object> req = mapOf("id", id);
    req.putAll(request.toMap());
    ApiProxy apiProxy = getApiProxy();
    apiProxy.patch(getUri(PATCH_TENANT_LINK), req, Void.class);
  }

  @Override
  public void delete(String id) throws DescopeException {
    delete(id, null);
  }

  @Override
  public void delete(String id, Boolean cascade) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    URI deleteTenantUri = composeDeleteTenantUri();
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> req = mapOf("id", id);
    addIfNotNull(req, "cascade", cascade);
    apiProxy.post(deleteTenantUri, req, Void.class);
  }

  @Override
  public DeleteTenantsBatchResponse deleteBatch(List<String> ids, Boolean cascade) throws DescopeException {
    if (CollectionUtils.isEmpty(ids)) {
      throw ServerCommonException.invalidArgument("ids");
    }
    Map<String, Object> req = mapOf("ids", ids);
    addIfNotNull(req, "cascade", cascade);
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(DELETE_TENANTS_BATCH_LINK), req, DeleteTenantsBatchResponse.class);
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
    addIfNotNull(req, "stepupTokenExpiration", settings.getStepupTokenExpiration());
    addIfNotNull(req, "stepupTokenExpirationUnit", settings.getStepupTokenExpirationUnit());
    addIfNotNull(req, "ssoSetupSuiteSettings", settings.getSsoSetupSuiteSettings());
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

    Map<String, Object> req = mapOf("tenantId", request.getTenantId());
    addIfNotNull(req, "expireTime", request.getExpireDuration());
    addIfNotNull(req, "ssoId", request.getSsoId());
    addIfNotNull(req, "email", request.getEmail());
    addIfNotNull(req, "templateId", request.getTemplateId());
    
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

  @Override
  public void updateDefaultRoles(String tenantId, List<String> defaultRoles) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, Object> req = mapOf("id", tenantId);
    req.put("defaultRoles", defaultRoles);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(TENANT_UPDATE_DEFAULT_ROLES_LINK), req, Void.class);
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
