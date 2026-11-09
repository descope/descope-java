package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_REVOKE_IDENTITIES;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_SEARCH_IDENTITIES;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticIdentityRevokeRequest;
import com.descope.model.agentic.AgenticIdentityRevokeResponse;
import com.descope.model.agentic.AgenticIdentitySearchRequest;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AgenticService;

class AgenticServiceImpl extends ManagementsBase implements AgenticService {

  AgenticServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AgenticIdentitySearchResponse searchIdentities(AgenticIdentitySearchRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_SEARCH_IDENTITIES), request, AgenticIdentitySearchResponse.class);
  }

  @Override
  public int revokeIdentities(AgenticIdentityRevokeRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }

    ApiProxy apiProxy = getApiProxy();
    AgenticIdentityRevokeResponse res =
        apiProxy.post(getUri(MANAGEMENT_AGENTIC_REVOKE_IDENTITIES), request, AgenticIdentityRevokeResponse.class);
    return res == null ? 0 : res.getRevoked();
  }
}
