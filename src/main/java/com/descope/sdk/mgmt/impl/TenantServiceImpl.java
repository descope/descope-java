package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_TENANT_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.magement.ManagementParams;
import com.descope.model.tenant.Tenant;
import com.descope.sdk.mgmt.TenantService;
import java.net.URI;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class TenantServiceImpl extends ManagementsBase implements TenantService {

  TenantServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public String create(String name, List<String> selfProvisioningDomains) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    var tenant = new Tenant("", name, selfProvisioningDomains);
    return null;
  }

  @Override
  public void createWithId(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException {}

  private String create(Tenant tenant){
    URI createTenantUri = composeCreateTenantUri();
    var apiProxy = getApiProxy();
    Tenant savedTenant = apiProxy.post(createTenantUri, tenant, Tenant.class);
    return savedTenant.getId();
  }

  @Override
  public void update(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException {}

  @Override
  public void delete(String id) throws DescopeException {}

  @Override
  public List<Tenant> loadAll() throws DescopeException {
    return null;
  }



  private URI composeCreateTenantUri() {
    return getUri(CREATE_TENANT_LINK);
  }
}
