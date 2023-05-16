package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import java.util.Map;

/** Provide functions for manipulating valid JWT */
public interface JwtService {

  /**
   * Update a valid JWT with the custom claims provided. The new JWT will be returned.
   *
   * @param jwt - Old JWT
   * @param customClaims - Custom Claims
   * @return - New JWT
   */
  String updateJWTWithCustomClaims(String jwt, Map<String, Object> customClaims)
      throws DescopeException;
}
