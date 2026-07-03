package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_CREATE_BATCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_DELETE_BATCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PERMISSION_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.permission.Permission;
import com.descope.model.permission.PermissionResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.PermissionService;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

class PermissionServiceImpl extends ManagementsBase implements PermissionService {

  PermissionServiceImpl(Client client) {
    super(client);
  }

  @Override
  public void create(String name, String description) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = mapOf("name", name, "description", description);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_CREATE_LINK), request, Void.class);
  }

  @Override
  public void createBatch(List<Permission> permissions) throws DescopeException {
    if (CollectionUtils.isEmpty(permissions)) {
      throw ServerCommonException.invalidArgument("permissions");
    }
    Map<String, Object> request = mapOf("permissions", permissions);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_CREATE_BATCH_LINK), request, Void.class);
  }

  @Override
  public void update(String name, String newName, String description) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    if (StringUtils.isBlank(newName)) {
      throw ServerCommonException.invalidArgument("NewName");
    }
    Map<String, String> request =
        mapOf("name", name, "newName", newName, "description", description);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_UPDATE_LINK), request, Void.class);
  }

  @Override
  public void updateWithId(String id, String newName, String description) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(newName)) {
      throw ServerCommonException.invalidArgument("NewName");
    }
    Map<String, String> request =
        mapOf("id", id, "newName", newName, "description", description);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_UPDATE_LINK), request, Void.class);
  }

  @Override
  public void delete(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    Map<String, String> request = mapOf("name", name);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_DELETE_LINK), request, Void.class);
  }

  @Override
  public void deleteWithId(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, String> request = mapOf("id", id);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_DELETE_LINK), request, Void.class);
  }

  @Override
  public void deleteBatch(List<String> names, List<String> ids) throws DescopeException {
    if (CollectionUtils.isEmpty(names) && CollectionUtils.isEmpty(ids)) {
      throw ServerCommonException.invalidArgument("names");
    }
    Map<String, Object> request = mapOf("names", names, "ids", ids);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PERMISSION_DELETE_BATCH_LINK), request, Void.class);
  }

  @Override
  public PermissionResponse loadAll() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(MANAGEMENT_PERMISSION_LOAD_ALL_LINK), PermissionResponse.class);
  }
}
