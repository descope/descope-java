package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.scopeclaimmapping.ScopeClaimMappingEntry;
import java.util.List;

/** Provides functions for managing the project-wide mapping of OIDC scopes to JWT claims. */
public interface ScopeClaimMappingService {

  /**
   * Get the project-wide scope to claims mapping.
   *
   * @return the ordered list of mapping entries, empty when no mapping is configured
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<ScopeClaimMappingEntry> getScopeClaimMapping() throws DescopeException;

  /**
   * Replace the project-wide scope to claims mapping. When two entries set the same claim name, the
   * later entry wins. An empty list clears the mapping, though {@link #deleteScopeClaimMapping()} is
   * preferred for that.
   *
   * @param mappings the ordered list of mapping entries
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void setScopeClaimMapping(List<ScopeClaimMappingEntry> mappings) throws DescopeException;

  /**
   * Delete the project-wide scope to claims mapping. This action is irreversible.
   *
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteScopeClaimMapping() throws DescopeException;
}
