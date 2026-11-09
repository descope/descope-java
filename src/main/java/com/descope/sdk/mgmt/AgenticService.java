package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.agentic.AgenticIdentityRevokeRequest;
import com.descope.model.agentic.AgenticIdentitySearchRequest;
import com.descope.model.agentic.AgenticIdentitySearchResponse;

/** Provides functions for managing agentic identities in a project. */
public interface AgenticService {

  /**
   * Search agentic identities, the authorization records that bind an OAuth client to a user, tenant, or
   * autonomous client.
   *
   * @param request optional filters by userId, clientId, resourceId or consentId (clientId, resourceId and
   *     consentId are up to 100 characters), and pagination with page (zero-based) and limit
   * @return {@link AgenticIdentitySearchResponse} with the matching identities and their total count
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  AgenticIdentitySearchResponse searchIdentities(AgenticIdentitySearchRequest request) throws DescopeException;

  /**
   * Revoke agentic identities and invalidate their consents.
   *
   * @param request at least one of userId, clientId, resourceId or consentId must be set (clientId, resourceId
   *     and consentId are up to 100 characters)
   * @return the number of identities revoked
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  int revokeIdentities(AgenticIdentityRevokeRequest request) throws DescopeException;
}
