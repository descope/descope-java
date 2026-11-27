package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.descoper.Descoper;
import com.descope.model.descoper.DescoperAttributes;
import com.descope.model.descoper.DescoperListResponse;
import com.descope.model.descoper.DescoperRBAC;

/** Provides functions for managing descopers, the members of the company console. */
public interface DescoperService {

  /**
   * Load a descoper by ID.
   *
   * @param id The descoper ID (required)
   * @return {@link Descoper}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Descoper load(String id) throws DescopeException;

  /**
   * Update a descoper's attributes and roles.
   *
   * @param id The descoper ID (required)
   * @param attributes Attributes to set, or null to leave unchanged
   * @param rbac Roles to set, or null to leave unchanged
   * @return the updated {@link Descoper}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Descoper update(String id, DescoperAttributes attributes, DescoperRBAC rbac) throws DescopeException;

  /**
   * Delete a descoper.
   *
   * @param id The descoper ID (required)
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void delete(String id) throws DescopeException;

  /**
   * List all descopers.
   *
   * @return {@link DescoperListResponse} with the descopers and their total
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  DescoperListResponse list() throws DescopeException;
}
