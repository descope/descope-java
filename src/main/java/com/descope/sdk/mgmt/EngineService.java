package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.engine.Engine;

// Provides functions for managing engines in a project.
public interface EngineService {
  /**
   * Create a new engine.
   *
   * @param name The engine name, required.
   * @return The created engine, including its secret. The secret cannot be retrieved again.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Engine createEngine(String name) throws DescopeException;

  /**
   * Update an existing engine.
   *
   * @param id The engine ID, required.
   * @param name The new engine name, required.
   * @return The updated engine, without its secret.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Engine updateEngine(String id, String name) throws DescopeException;

  /**
   * Delete an engine.
   *
   * @param id The engine ID, required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteEngine(String id) throws DescopeException;

  /**
   * Load an engine by ID.
   *
   * @param id The engine ID, required.
   * @return The engine, without its secret.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Engine loadEngine(String id) throws DescopeException;

  /**
   * Rotate an engine's secret. The previous secret is immediately invalidated.
   *
   * @param id The engine ID, required.
   * @return The new secret. It cannot be retrieved again.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String rotateEngineSecret(String id) throws DescopeException;
}
