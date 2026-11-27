package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.secret.ProjectSecret;
import java.util.List;

/**
 * Manage project secrets. A secret value is encrypted at rest and can be referenced from connectors and
 * scripts, but is never returned by any API.
 */
public interface ProjectSecretService {

  /**
   * Create a project secret.
   *
   * @param name The secret name, required. It cannot be changed later.
   * @param value The secret value, required. It is write-only.
   * @param description Optional description.
   * @return The created {@link ProjectSecret}, without its value.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ProjectSecret createSecret(String name, String value, String description) throws DescopeException;

  /**
   * Update the description of a project secret, and optionally rotate its value. The name cannot be changed.
   *
   * @param id The secret ID (the lowercased secret name), required.
   * @param description The description.
   * @param value The new secret value, or null or empty to keep the stored value.
   * @return The updated {@link ProjectSecret}, without its value.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ProjectSecret updateSecret(String id, String description, String value) throws DescopeException;

  /**
   * Delete a project secret. Fails while a connector still references the secret.
   *
   * @param id The secret ID (the lowercased secret name), required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteSecret(String id) throws DescopeException;

  /**
   * Load a project secret by ID. The response never includes the secret value.
   *
   * @param id The secret ID (the lowercased secret name), required.
   * @return The {@link ProjectSecret}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  ProjectSecret loadSecret(String id) throws DescopeException;

  /**
   * Load all project secrets. The response never includes secret values.
   *
   * @return The list of {@link ProjectSecret}.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<ProjectSecret> loadAllSecrets() throws DescopeException;
}
