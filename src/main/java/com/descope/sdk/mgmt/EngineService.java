package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.engine.Engine;

// Provides functions for managing engines in a project.
public interface EngineService {
  /**
   * Create a new engine. The returned engine includes its generated secret.
   *
   * @param name The engine name, up to 10000 characters. Required.
   * @return The created {@link Engine}, including its secret.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Engine create(String name) throws DescopeException;

  /**
   * Update an existing engine's name.
   *
   * @param id The engine ID, up to 254 characters. Required.
   * @param name The new engine name, up to 10000 characters. Required.
   * @return The updated {@link Engine}, without its secret.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Engine update(String id, String name) throws DescopeException;

  /**
   * Delete an engine.
   *
   * @param id The engine ID, up to 254 characters. Required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void delete(String id) throws DescopeException;

  /**
   * Load an engine by ID. The returned engine's secret is always empty.
   *
   * @param id The engine ID, up to 254 characters. Required.
   * @return The {@link Engine}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Engine load(String id) throws DescopeException;

  /**
   * Generate a new secret for an engine. The previous secret is invalidated immediately.
   *
   * @param id The engine ID, up to 254 characters. Required.
   * @return The new secret.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String rotateSecret(String id) throws DescopeException;
}
