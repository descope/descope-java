package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_TENANTS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_TENANT_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.sdk.mgmt.TenantService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class TenantServiceImpl extends ManagementsBase implements TenantService {

  TenantServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public String create(String name, List<String> selfProvisioningDomains) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    var tenant = new Tenant("", name, selfProvisioningDomains);
    return create(tenant);
  }

  @Override
  public void createWithId(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }

    var tenant = new Tenant(id, name, selfProvisioningDomains);
    create(tenant);
  }

  private String create(Tenant tenant) {
    URI createTenantUri = composeCreateTenantUri();
    var apiProxy = getApiProxy();
    Tenant savedTenant = apiProxy.post(createTenantUri, tenant, Tenant.class);
    return savedTenant.getId();
  }

  @Override
  public void update(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException {
    if (StringUtils.isAnyBlank(id, name)) {
      throw ServerCommonException.invalidArgument("id or name");
    }

    var tenant = new Tenant(id, name, selfProvisioningDomains);
    update(tenant);
  }

  private void update(Tenant tenant) {
    URI updateTenantUri = composeUpdateTenantUri();
    var apiProxy = getApiProxy();
    apiProxy.post(updateTenantUri, tenant, String.class);
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    URI deleteTenantUri = composeDeleteTenantUri();
    var apiProxy = getApiProxy();
    apiProxy.post(deleteTenantUri, Map.of("id", id), String.class);
  }

  @Override
  public List<Tenant> loadAll() throws DescopeException {
    URI loadAllTenantsUri = loadAllTenantsUri();
    var apiProxy = getApiProxy();
    GetAllTenantsResponse response = apiProxy.get(loadAllTenantsUri, GetAllTenantsResponse.class);
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
}
