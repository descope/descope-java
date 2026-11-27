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
   * Create a new role.
   *
   * @param name            - The role name (required)
   * @param tenantId        - The tenant the role belongs to (optional)
   * @param description     - The role description (optional)
   * @param permissionNames - The permissions the role grants (optional)
   * @param defaultRole     - Whether the role is a default role (optional)
   * @param privateRole     - Whether the role is private (optional)
   * @return {@link Role Role} the created role
   */
  Role create(String name, String tenantId, String description, List<String> permissionNames, Boolean defaultRole,
      Boolean privateRole) throws DescopeException;

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
   * Update an existing role identified by its name. All parameters override the role's current values.
   *
   * @param name            - The name of the role to update (required)
   * @param tenantId        - The tenant the role belongs to (optional)
   * @param newName         - The role's new name (required)
   * @param description     - The role description (optional)
   * @param permissionNames - The permissions the role grants (optional)
   * @param defaultRole     - Whether the role is a default role (optional)
   * @param privateRole     - Whether the role is private (optional)
   * @return {@link Role Role} the updated role
   */
  Role update(String name, String tenantId, String newName, String description, List<String> permissionNames,
      Boolean defaultRole, Boolean privateRole) throws DescopeException;

  /**
   * Update an existing role identified by its ID. All parameters override the role's current values.
   *
   * @param id              - The ID of the role to update (required)
   * @param tenantId        - The tenant the role belongs to (optional)
   * @param newName         - The role's new name (required)
   * @param description     - The role description (optional)
   * @param permissionNames - The permissions the role grants (optional)
   * @param defaultRole     - Whether the role is a default role (optional)
   * @param privateRole     - Whether the role is private (optional)
   * @return {@link Role Role} the updated role
   */
  Role updateWithId(String id, String tenantId, String newName, String description, List<String> permissionNames,
      Boolean defaultRole, Boolean privateRole) throws DescopeException;

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
