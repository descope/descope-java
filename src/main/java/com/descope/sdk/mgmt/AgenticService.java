package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.agentic.AgenticGovernanceSettings;
import com.descope.model.agentic.AgenticIdentitiesRevokeOptions;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.agentic.GovernanceAccessRequest;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.XaaAgenticIdentitiesResponse;
import java.util.List;

/** Provides functions for managing agentic identities and the Agentic Governance Suite in a project. */
public interface AgenticService {

  /**
   * Create an Agentic Governance Suite access request to provision an agent client on behalf of a console user.
   * When auto-approval is on and the current policies already permit the request, it is approved immediately.
   *
   * @param request - The request. requestorUserId is required.
   * @return The created access request.
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequest createGovernanceAccessRequest(GovernanceAccessRequestCreateRequest request)
      throws DescopeException;

  /**
   * Load a single Agentic Governance Suite access request.
   *
   * @param id - The access request ID.
   * @return The access request.
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequest loadGovernanceAccessRequest(String id) throws DescopeException;

  /**
   * Approve or deny a pending Agentic Governance Suite access request. Approving provisions the agent client.
   *
   * @param id             - The access request ID.
   * @param action         - "approve" or "deny".
   * @param resolutionNote - Optional note recorded with the resolution.
   * @return The resolved access request.
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequest resolveGovernanceAccessRequest(String id, String action, String resolutionNote)
      throws DescopeException;

  /**
   * Search the project's Agentic Governance Suite access requests.
   *
   * @param options - Filtering, sorting and paging options.
   * @return The matching access requests and their total count.
   * @throws DescopeException in case of errors
   */
  GovernanceAccessRequestSearchResponse searchGovernanceAccessRequests(GovernanceAccessRequestSearchOptions options)
      throws DescopeException;

  /**
   * Load the project's Agentic Governance Suite settings, or the defaults.
   *
   * @return The settings.
   * @throws DescopeException in case of errors
   */
  AgenticGovernanceSettings loadGovernanceSettings() throws DescopeException;

  /**
   * Update the project's Agentic Governance Suite settings. Only the non-null fields are changed.
   *
   * @param settings - The settings to change.
   * @return The updated settings.
   * @throws DescopeException in case of errors
   */
  AgenticGovernanceSettings updateGovernanceSettings(AgenticGovernanceSettings settings) throws DescopeException;

  /**
   * Revoke agentic identities and their consents. A request with only a user ID revokes all of the user's
   * agentic identities.
   *
   * @param options - At least one of userId, clientId, resourceId or consentId must be set.
   * @return The number of revoked identities.
   * @throws DescopeException in case of errors
   */
  int revokeAgenticIdentities(AgenticIdentitiesRevokeOptions options) throws DescopeException;

  /**
   * Search agentic identities (consents granted to agentic apps).
   *
   * @param options - Filtering and paging options.
   * @return The matching identities and their total count.
   * @throws DescopeException in case of errors
   */
  AgenticIdentitySearchResponse searchAgenticIdentities(AgenticIdentitySearchOptions options)
      throws DescopeException;

  /**
   * Block XAA (ID-JAG) agentic identities, so their token exchanges are rejected. A block can take up to 5
   * minutes to apply. Blocking does not revoke the identity or delete its consent.
   *
   * @param consentIds - Consent IDs of the identities, as returned by searchAgenticIdentities. Up to 100.
   * @return The updated identities, and the consent IDs that were not updated.
   * @throws DescopeException in case of errors
   */
  XaaAgenticIdentitiesResponse blockXaaAgenticIdentities(List<String> consentIds) throws DescopeException;

  /**
   * Unblock XAA (ID-JAG) agentic identities, restoring their token exchanges. An unblock can take up to 5
   * minutes to apply.
   *
   * @param consentIds - Consent IDs of the identities, as returned by searchAgenticIdentities. Up to 100.
   * @return The updated identities, and the consent IDs that were not updated.
   * @throws DescopeException in case of errors
   */
  XaaAgenticIdentitiesResponse unblockXaaAgenticIdentities(List<String> consentIds) throws DescopeException;
}
