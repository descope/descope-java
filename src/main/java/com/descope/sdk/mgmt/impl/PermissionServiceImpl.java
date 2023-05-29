package com.descope.sdk.mgmt.impl;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.permission.Permission;
import com.descope.sdk.mgmt.PermissionService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_LOADALL;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_UPDATE;

public class PermissionServiceImpl extends ManagementsBase implements PermissionService {

  PermissionServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public void create(String name, String description) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = Map.of("name", name, "description", description);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_CREATE), request, Void.class);
  }

  @Override
  public void update(String name, String newName, String description) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    if (StringUtils.isBlank(newName)) {
      throw ServerCommonException.invalidArgument("NewName");
    }
    Map<String, String> request = Map.of("name", name, "newName", newName, "description", description);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_UPDATE), request, Void.class);
  }

  @Override
  public void delete(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = Map.of("name", name);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_DELETE), request, Void.class);
  }

  @Override
  public List<Permission> loadAll() throws DescopeException {
    var apiProxy = getApiProxy();
    return (List<Permission>) apiProxy.get(getUri(MANAGEMENT_PERMISSION_LOADALL), List.class);
  }
}
