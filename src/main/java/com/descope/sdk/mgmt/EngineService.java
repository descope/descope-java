package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.engine.Engine;
import java.util.List;

/** Provides functions for managing engines in a project. */
public interface EngineService {

  /**
   * Load all project engines. The engine secret is never returned by this call.
   *
   * @return list of {@link Engine}
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  List<Engine> loadAll() throws DescopeException;
}
