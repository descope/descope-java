package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentityRevokeResponse;
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
   * Search agentic identities (consents granted to agentic apps), filterable by client ID, resource ID,
   * consent ID or user ID.
   *
   * @param options optional filters and paging
   * @return the matching agentic identities and their total count
   * @throws DescopeException in case of errors
   */
  AgenticIdentitySearchResponse searchIdentities(AgenticIdentitySearchOptions options) throws DescopeException;

  /**
   * Revoke agentic identities and their consents by client ID, consent ID, resource ID and/or user ID.
   * A request with only a user ID revokes all of the user's agentic identities.
   *
   * @param options the filters, at least one must be set
   * @return the number of revoked agentic identities
   * @throws DescopeException in case of errors
   */
  AgenticIdentityRevokeResponse revokeIdentities(AgenticIdentityRevokeOptions options) throws DescopeException;

  /**
   * Load the project's Agentic Governance Suite settings. Returns the stored settings or the defaults.
   *
   * @return the governance settings
   * @throws DescopeException in case of errors
   */
  GovernanceSettings getGovernanceSettings() throws DescopeException;

  /**
   * Update the project's Agentic Governance Suite settings. Only the fields that are set are changed.
   * Requires the Agentic Governance Suite in the company license, except a request that changes nothing.
   *
   * @param settings the settings to change
   * @return the updated governance settings
   * @throws DescopeException in case of errors
   */
  GovernanceSettings setGovernanceSettings(GovernanceSettings settings) throws DescopeException;

  /**
   * Search the project's Agentic Governance Suite access requests, with filtering, sorting and paging.
   * Requires the Agentic Governance Suite in the company license.
   *
   * @param options optional filters, sort order and paging
   * @return the matching access requests and their total count
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequestSearchResponse searchGovernanceAccessRequests(GovernanceAccessRequestSearchOptions options)
      throws DescopeException;

  /**
   * Load a single Agentic Governance Suite access request by id.
   * Requires the Agentic Governance Suite in the company license.
   *
   * @param id the access request id
   * @return the access request
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequest getGovernanceAccessRequest(String id) throws DescopeException;

  /**
   * Approve or deny a pending Agentic Governance Suite access request. Approving provisions the requested
   * agent client. The resolution is attributed to the Management API.
   * Requires the Agentic Governance Suite in the company license.
   *
   * @param request the access request id, the action ("approve" or "deny") and an optional note
   * @return the resolved access request
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequest resolveGovernanceAccessRequest(GovernanceAccessRequestResolveRequest request)
      throws DescopeException;

  /**
   * Create an Agentic Governance Suite access request to provision an agent client on behalf of a developer.
   * The requestorUserId must be an existing, active console user in your company. When auto-approval is on
   * and the current policies already permit the request, it is approved and the client provisioned
   * immediately. Requires the Agentic Governance Suite in the company license, and fails while the suite is
   * disabled.
   *
   * @param request the access request details
   * @return the created access request
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequest createGovernanceAccessRequest(GovernanceAccessRequestCreateRequest request)
      throws DescopeException;
}
