package com.descope.sdk.mgmt.impl;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.roles.Role;
import com.descope.sdk.mgmt.RolesService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_LOADALL;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_UPDATE;

public class RolesServiceImpl extends ManagementsBase implements RolesService {

  RolesServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public void create(String name, String description, List<String> permissionNames) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, Object> request = Map.of("name", name, "description", description, "permissionNames", permissionNames);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_CREATE), request, Void.class);
  }

  @Override
  public void update(String name, String newName, String description, List<String> permissionNames) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    if (StringUtils.isBlank(newName)) {
      throw ServerCommonException.invalidArgument("NewName");
    }
    Map<String, Object> request = Map.of("name", name, "newName", newName, "description", description, "permissionNames", permissionNames);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_UPDATE), request, Void.class);
  }

  @Override
  public void delete(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = Map.of("name", name);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_DELETE), request, Void.class);
  }

  @Override
  public List<Role> loadAll() throws DescopeException {
    var apiProxy = getApiProxy();
    return (List<Role>) apiProxy.get(getUri(MANAGEMENT_ROLES_LOADALL), List.class);
  }
}
