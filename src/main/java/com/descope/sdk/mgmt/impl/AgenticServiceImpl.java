package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_REVOKE_IDENTITIES;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_SEARCH_IDENTITIES;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentityRevokeResponse;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AgenticService;

public class AgenticServiceImpl extends ManagementsBase implements AgenticService {

  AgenticServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AgenticIdentitySearchResponse searchIdentities(AgenticIdentitySearchOptions options)
      throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_SEARCH_IDENTITIES), options, AgenticIdentitySearchResponse.class);
  }

  @Override
  public int revokeIdentities(AgenticIdentityRevokeOptions options) throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }

    ApiProxy apiProxy = getApiProxy();
    AgenticIdentityRevokeResponse res =
        apiProxy.post(getUri(MANAGEMENT_AGENTIC_REVOKE_IDENTITIES), options, AgenticIdentityRevokeResponse.class);
    return res == null ? 0 : res.getRevoked();
  }
}
