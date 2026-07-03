package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.permission.Permission;
import com.descope.model.permission.PermissionResponse;
import java.util.List;

public interface PermissionService {
  void create(String name, String description) throws DescopeException;

  /**
   * Create multiple permissions in a single batch.
   *
   * @param permissions - The permissions to create
   */
  void createBatch(List<Permission> permissions) throws DescopeException;

  void update(String name, String newName, String description) throws DescopeException;

  /**
   * Update an existing permission identified by its ID.
   *
   * @param id          - The ID of the permission to update
   * @param newName     - The new name for the permission
   * @param description - The new description for the permission (optional)
   */
  void updateWithId(String id, String newName, String description) throws DescopeException;

  void delete(String name) throws DescopeException;

  /**
   * Delete an existing permission identified by its ID.
   *
   * @param id - The ID of the permission to delete
   */
  void deleteWithId(String id) throws DescopeException;

  /**
   * Delete multiple permissions identified by their names and/or IDs.
   *
   * @param names - The names of the permissions to delete (optional)
   * @param ids   - The IDs of the permissions to delete (optional)
   */
  void deleteBatch(List<String> names, List<String> ids) throws DescopeException;

  PermissionResponse loadAll() throws DescopeException;
}
