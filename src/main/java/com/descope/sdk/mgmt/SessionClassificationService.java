package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.sessionclassification.SessionClassification;
import java.util.List;

/** Provides functions for managing session classifications in a project. */
public interface SessionClassificationService {

  /**
   * Load a session classification by name.
   *
   * @param name the session classification name
   * @return {@link SessionClassification}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  SessionClassification load(String name) throws DescopeException;

  /**
   * Create or update a session classification, by name. On a new name the defaults are applied; on an
   * existing one, fields left out keep their stored value.
   *
   * @param classification the session classification, its name is required
   * @return the stored {@link SessionClassification}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  SessionClassification set(SessionClassification classification) throws DescopeException;

  /**
   * Delete a session classification by name. Users assigned to it are left without a classification.
   *
   * @param name the session classification name
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void delete(String name) throws DescopeException;

  /**
   * Load all session classifications in the project.
   *
   * @return the list of {@link SessionClassification}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<SessionClassification> loadAll() throws DescopeException;
}
