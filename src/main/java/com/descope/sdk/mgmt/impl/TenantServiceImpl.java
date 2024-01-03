package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_TENANTS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.TENANT_SEARCH_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_TENANT_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.request.TenantSearchRequest;
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
    Tenant tenant = new Tenant("", name, selfProvisioningDomains, null);
    return create(tenant);
  }

  @Override
  public String create(String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    Tenant tenant = new Tenant("", name, selfProvisioningDomains, customAttributes);
    return create(tenant);
  }

  @Override
  public void createWithId(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }

    Tenant tenant = new Tenant(id, name, selfProvisioningDomains, null);
    create(tenant);
  }

  @Override
  public void createWithId(String id, String name, List<String> selfProvisioningDomains,
      Map<String, Object> customAttributes)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }

    Tenant tenant = new Tenant(id, name, selfProvisioningDomains, customAttributes);
    create(tenant);
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

    Tenant tenant = new Tenant(id, name, selfProvisioningDomains, customAttributes);
    update(tenant);
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

  private URI composeCreateTenantUri() {
    return getUri(CREATE_TENANT_LINK);
  }

  private URI composeUpdateTenantUri() {
    return getUri(UPDATE_TENANT_LINK);
  }

  private URI composeDeleteTenantUri() {
    return getUri(DELETE_TENANT_LINK);
  }

  private URI loadAllTenantsUri() {
    return getUri(LOAD_ALL_TENANTS_LINK);
  }

  private URI composeSearchAllUri() {
    return getUri(TENANT_SEARCH_ALL_LINK);
  }
}
