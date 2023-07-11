package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_UPDATE_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.roles.RoleResponse;
import com.descope.sdk.mgmt.RolesService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class RolesServiceImpl extends ManagementsBase implements RolesService {

  RolesServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public void create(String name, String description, List<String> permissionNames)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, Object> request =
        Map.of("name", name, "description", description);
    if (permissionNames != null) {
      request.put("permissionNames", permissionNames);
    }
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_CREATE_LINK), request, Void.class);
  }

  @Override
  public void update(String name, String newName, String description, List<String> permissionNames)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    if (StringUtils.isBlank(newName)) {
      throw ServerCommonException.invalidArgument("NewName");
    }
    Map<String, Object> request =
        Map.of(
            "name",
            name,
            "newName",
            newName,
            "description",
            description,
            "permissionNames",
            permissionNames);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_UPDATE_LINK), request, Void.class);
  }

  @Override
  public void delete(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = Map.of("name", name);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_DELETE_LINK), request, Void.class);
  }

  @Override
  public RoleResponse loadAll() throws DescopeException {
    var apiProxy = getApiProxy();
    return apiProxy.get(getUri(MANAGEMENT_ROLES_LOAD_ALL_LINK), RoleResponse.class);
  }
}
