package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.descoper.Descoper;
import com.descope.model.descoper.DescoperAttributes;
import com.descope.model.descoper.DescoperRBAC;
import com.descope.model.descoper.DescopersListResponse;

// Provides functions for managing descopers (console users) in a company.
public interface DescoperService {
  /**
   * Load a descoper by ID.
   *
   * @param id The descoper ID, required
   * @return The descoper
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Descoper getDescoper(String id) throws DescopeException;

  /**
   * Update an existing descoper's attributes and/or RBAC.
   *
   * @param id The descoper ID, required
   * @param attributes Optional attributes to update (displayName up to 254 characters)
   * @param rbac Optional RBAC to set
   * @return The updated descoper
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Descoper updateDescoper(String id, DescoperAttributes attributes, DescoperRBAC rbac) throws DescopeException;

  /**
   * Delete an existing descoper.
   *
   * @param id The descoper ID, required
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteDescoper(String id) throws DescopeException;

  /**
   * List all descopers.
   *
   * @return The descopers and their total count
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  DescopersListResponse listDescopers() throws DescopeException;
}
