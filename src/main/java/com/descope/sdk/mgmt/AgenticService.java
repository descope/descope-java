package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;

// Provides functions for managing the agentic identities (consents granted to agentic apps) in a project.
public interface AgenticService {
  /**
   * Search agentic identities, filterable by client ID, resource ID, consent ID or user ID.
   *
   * @param options the search filters and paging, an empty options object returns all identities
   * @return the matching agentic identities and their total count
   * @throws DescopeException if the request fails
   */
  AgenticIdentitySearchResponse searchIdentities(AgenticIdentitySearchOptions options) throws DescopeException;

  /**
   * Revoke agentic identities and their consents by client ID, consent ID, resource ID and/or user ID.
   * At least one filter must be set. Options with only a user ID revoke all of the user's agentic
   * identities across MCP servers.
   *
   * @param options the identities to revoke
   * @return the number of revoked identities
   * @throws DescopeException if the request fails
   */
  int revokeIdentities(AgenticIdentityRevokeOptions options) throws DescopeException;
}
