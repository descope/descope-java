package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCOPE_CLAIM_MAPPING_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCOPE_CLAIM_MAPPING_GET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCOPE_CLAIM_MAPPING_SET;
import static com.descope.utils.CollectionUtils.addIfNotNull;

import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.scopeclaimmapping.ScopeClaimMappingEntry;
import com.descope.model.scopeclaimmapping.ScopeClaimMappingResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ScopeClaimMappingService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScopeClaimMappingServiceImpl extends ManagementsBase implements ScopeClaimMappingService {

  ScopeClaimMappingServiceImpl(Client client) {
    super(client);
  }

  @Override
  public List<ScopeClaimMappingEntry> getScopeClaimMapping() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    ScopeClaimMappingResponse resp = apiProxy.post(getUri(MANAGEMENT_SCOPE_CLAIM_MAPPING_GET), new HashMap<>(),
        ScopeClaimMappingResponse.class);
    if (resp == null || resp.getMappings() == null) {
      return new ArrayList<>();
    }
    return resp.getMappings();
  }

  @Override
  public void setScopeClaimMapping(List<ScopeClaimMappingEntry> mappings) throws DescopeException {
    Map<String, Object> request = new HashMap<>();
    addIfNotNull(request, "mappings", mappings);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_SCOPE_CLAIM_MAPPING_SET), request, Void.class);
  }

  @Override
  public void deleteScopeClaimMapping() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_SCOPE_CLAIM_MAPPING_DELETE), new HashMap<>(), Void.class);
  }
}
