package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.roles.Role;
import com.descope.model.roles.RoleResponse;
import com.descope.model.roles.RoleSearchOptions;
import com.descope.model.roles.RoleUpdateRequest;
import java.util.List;

public interface RolesService {

  void create(String name, String description, List<String> permissionNames) throws DescopeException;

  void create(String name, String tenantId, String description, List<String> permissionNames) throws DescopeException;

  /**
   * Create multiple roles in a single batch.
   *
   * @param roles - The roles to create
   * @return {@link RoleResponse RoleResponse} containing the created roles
   */
  RoleResponse createBatch(List<Role> roles) throws DescopeException;

  void update(String name, String newName, String description, List<String> permissionNames) throws DescopeException;

  void update(String name, String tenantId, String newName, String description, List<String> permissionNames)
      throws DescopeException;

  /**
   * Update multiple roles in a single batch.
   *
   * @param roles - The roles to update
   * @return {@link RoleResponse RoleResponse} containing the updated roles
   */
  RoleResponse updateBatch(List<RoleUpdateRequest> roles) throws DescopeException;

  void delete(String name) throws DescopeException;

  void delete(String name, String tenantId) throws DescopeException;

  /**
   * Delete an existing role identified by its ID.
   *
   * @param id       - The ID of the role to delete
   * @param tenantId - The tenant the role belongs to (optional)
   */
  void deleteWithId(String id, String tenantId) throws DescopeException;

  /**
   * Delete multiple roles identified by their names and/or IDs.
   *
   * @param roleNames - The names of the roles to delete (optional)
   * @param tenantId  - The tenant the roles belong to (optional)
   * @param roleIds   - The IDs of the roles to delete (optional)
   */
  void deleteBatch(List<String> roleNames, String tenantId, List<String> roleIds) throws DescopeException;

  RoleResponse loadAll() throws DescopeException;

  RoleResponse search(RoleSearchOptions roleSearchOptions) throws DescopeException;
}
