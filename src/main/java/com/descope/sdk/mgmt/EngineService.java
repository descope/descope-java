package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.engine.Engine;
import java.util.List;

// Provides functions for managing engines in a project.
public interface EngineService {
  /**
   * Load all engines in the project.
   *
   * @return The project's engines, without their secrets. Empty when the project has none.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<Engine> loadAllEngines() throws DescopeException;
}
