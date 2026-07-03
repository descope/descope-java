package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.roles.RoleResponse;
import com.descope.model.roles.RoleSearchOptions;
import java.util.List;

public interface RolesService {

  void create(String name, String description, List<String> permissionNames) throws DescopeException;

  void create(String name, String tenantId, String description, List<String> permissionNames) throws DescopeException;

  void update(String name, String newName, String description, List<String> permissionNames) throws DescopeException;

  void update(String name, String tenantId, String newName, String description, List<String> permissionNames)
      throws DescopeException;

  void delete(String name) throws DescopeException;

  void delete(String name, String tenantId) throws DescopeException;

  /**
   * Delete an existing role identified by its ID.
   *
   * @param id       - The ID of the role to delete
   * @param tenantId - The tenant the role belongs to (optional)
   */
  void deleteWithId(String id, String tenantId) throws DescopeException;

  RoleResponse loadAll() throws DescopeException;

  RoleResponse search(RoleSearchOptions roleSearchOptions) throws DescopeException;
}
