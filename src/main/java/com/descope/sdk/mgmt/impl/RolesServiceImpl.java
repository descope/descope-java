package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_SEARCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ROLES_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.roles.RoleResponse;
import com.descope.model.roles.RoleSearchOptions;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.RolesService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class RolesServiceImpl extends ManagementsBase implements RolesService {

  RolesServiceImpl(Client client) {
    super(client);
  }

  @Override
  public void create(String name, String description, List<String> permissionNames) throws DescopeException {
    this.create(name, "", description, permissionNames);
  }

  @Override
  public void create(String name, String tenantId, String description, List<String> permissionNames)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, Object> request = mapOf("name", name, "description", description, "tenantId", tenantId);
    if (permissionNames != null) {
      request.put("permissionNames", permissionNames);
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_CREATE_LINK), request, Void.class);
  }

  @Override
  public void update(String name, String newName, String description, List<String> permissionNames)
      throws DescopeException {
    this.update(name, "", newName, description, permissionNames);
  }

  @Override
  public void update(String name, String tenantId, String newName, String description, List<String> permissionNames)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    if (StringUtils.isBlank(newName)) {
      throw ServerCommonException.invalidArgument("NewName");
    }
    Map<String, Object> request = mapOf("name", name, "newName", newName, "description", description, "permissionNames",
        permissionNames, "tenantId", tenantId);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_UPDATE_LINK), request, Void.class);
  }

  @Override
  public void delete(String name) throws DescopeException {
    this.delete(name, "");
  }

  @Override
  public void delete(String name, String tenantId) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = mapOf("name", name, "tenantId", tenantId);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ROLES_DELETE_LINK), request, Void.class);
  }

  @Override
  public RoleResponse loadAll() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(MANAGEMENT_ROLES_LOAD_ALL_LINK), RoleResponse.class);
  }

  @Override
  public RoleResponse search(RoleSearchOptions roleSearchOptions) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_ROLES_SEARCH_LINK), roleSearchOptions, RoleResponse.class);
  }
}
