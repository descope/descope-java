package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.agentic.GovernanceAccessRequest;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestResolveRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.GovernanceSettings;

// Provides functions for managing agentic identities and the Agentic Governance Suite in a project.
public interface AgenticService {
  /**
   * Search agentic identities (consents granted to agentic apps), filterable by client ID,
   * resource ID, consent ID or user ID.
   *
   * @param options the search filters and paging
   * @return the matching identities and their total count
   * @throws DescopeException if there is an error
   */
  AgenticIdentitySearchResponse searchAgenticIdentities(AgenticIdentitySearchOptions options)
      throws DescopeException;

  /**
   * Revoke agentic identities and their consents by client ID, consent ID, resource ID and/or user ID.
   * Options with only a user ID revoke all of the user's agentic identities.
   *
   * @param options the identities to revoke
   * @return the number of revoked identities
   * @throws DescopeException if there is an error
   */
  int revokeAgenticIdentities(AgenticIdentityRevokeOptions options) throws DescopeException;

  /**
   * Load the project's Agentic Governance Suite settings.
   *
   * @return the stored settings, or the defaults
   * @throws DescopeException if there is an error
   */
  GovernanceSettings getGovernanceSettings() throws DescopeException;

  /**
   * Update the project's Agentic Governance Suite settings. Only the fields that are set are
   * changed. Requires the Agentic Governance Suite in the company license.
   *
   * @param settings the settings to change
   * @return the settings after the update
   * @throws DescopeException if there is an error
   */
  GovernanceSettings setGovernanceSettings(GovernanceSettings settings) throws DescopeException;

  /**
   * Search the project's Agentic Governance Suite access requests, with filtering, sorting and
   * paging. Requires the Agentic Governance Suite in the company license.
   *
   * @param options the search filters, sorting and paging
   * @return the matching access requests and their total count
   * @throws DescopeException if there is an error
   */
  GovernanceAccessRequestSearchResponse searchGovernanceAccessRequests(GovernanceAccessRequestSearchOptions options)
      throws DescopeException;

  /**
   * Load a single Agentic Governance Suite access request by ID. Requires the Agentic Governance
   * Suite in the company license.
   *
   * @param id the access request ID
   * @return the access request
   * @throws DescopeException if there is an error
   */
  GovernanceAccessRequest getGovernanceAccessRequest(String id) throws DescopeException;

  /**
   * Approve or deny a pending Agentic Governance Suite access request. Approving provisions the
   * requested agent client. The resolution is attributed to the Management API. Requires the
   * Agentic Governance Suite in the company license.
   *
   * @param request the access request ID, the action ("approve" or "deny") and an optional note
   * @return the resolved access request
   * @throws DescopeException if there is an error
   */
  GovernanceAccessRequest resolveGovernanceAccessRequest(GovernanceAccessRequestResolveRequest request)
      throws DescopeException;

  /**
   * Create an Agentic Governance Suite access request to provision an agent client on behalf of a
   * developer. The requestor must be an existing, active console user in your company. When
   * auto-approval is on and the current policies already permit the request, it is approved and the
   * client provisioned immediately. Requires the Agentic Governance Suite in the company license, and
   * fails while the suite is disabled.
   *
   * @param request the access request to create
   * @return the created access request
   * @throws DescopeException if there is an error
   */
  GovernanceAccessRequest createGovernanceAccessRequest(GovernanceAccessRequestCreateRequest request)
      throws DescopeException;
}
