package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;

// Provides functions for managing agentic identities in a project.
public interface AgenticService {
  /**
   * Search agentic identities (consents granted to agentic apps), filterable by client ID,
   * resource ID or user ID.
   *
   * @param options the search filters and paging
   * @return the matching agentic identities and their total count
   * @throws DescopeException if the request fails
   */
  AgenticIdentitySearchResponse searchIdentities(AgenticIdentitySearchOptions options) throws DescopeException;

  /**
   * Revoke agentic identities and their consents by client ID, consent ID, resource ID and/or user ID.
   * At least one of them must be set. Setting only a user ID revokes all of the user's agentic identities
   * across MCP servers.
   *
   * @param options the filters that select the identities to revoke
   * @return the number of revoked agentic identities
   * @throws DescopeException if the request fails
   */
  int revokeIdentities(AgenticIdentityRevokeOptions options) throws DescopeException;
}
