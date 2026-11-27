package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_GET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_RESOLVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_SEARCH;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_SETTINGS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_IDENTITIES_REVOKE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_IDENTITIES_SEARCH;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_IDENTITIES_XAA_BLOCK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_IDENTITIES_XAA_UNBLOCK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticGovernanceSettings;
import com.descope.model.agentic.AgenticIdentitiesRevokeOptions;
import com.descope.model.agentic.AgenticIdentitiesRevokeResponse;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.agentic.GovernanceAccessRequest;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.XaaAgenticIdentitiesResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AgenticService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class AgenticServiceImpl extends ManagementsBase implements AgenticService {

  AgenticServiceImpl(Client client) {
    super(client);
  }

  @Override
  public GovernanceAccessRequest createGovernanceAccessRequest(GovernanceAccessRequestCreateRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getRequestorUserId())) {
      throw ServerCommonException.invalidArgument("request.requestorUserId");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_CREATE), request,
        GovernanceAccessRequest.class);
  }

  @Override
  public GovernanceAccessRequest loadGovernanceAccessRequest(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_GET), mapOf("id", id),
        GovernanceAccessRequest.class);
  }

  @Override
  public GovernanceAccessRequest resolveGovernanceAccessRequest(String id, String action, String resolutionNote)
      throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(action)) {
      throw ServerCommonException.invalidArgument("action");
    }
    Map<String, String> request = mapOf("id", id, "action", action);
    if (resolutionNote != null) {
      request.put("resolutionNote", resolutionNote);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_RESOLVE), request,
        GovernanceAccessRequest.class);
  }

  @Override
  public GovernanceAccessRequestSearchResponse searchGovernanceAccessRequests(
      GovernanceAccessRequestSearchOptions options) throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUEST_SEARCH), options,
        GovernanceAccessRequestSearchResponse.class);
  }

  @Override
  public AgenticGovernanceSettings loadGovernanceSettings() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_SETTINGS), AgenticGovernanceSettings.class);
  }

  @Override
  public AgenticGovernanceSettings updateGovernanceSettings(AgenticGovernanceSettings settings)
      throws DescopeException {
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_SETTINGS), settings,
        AgenticGovernanceSettings.class);
  }

  @Override
  public int revokeAgenticIdentities(AgenticIdentitiesRevokeOptions options) throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }
    if (StringUtils.isAllBlank(options.getUserId(), options.getClientId(), options.getResourceId(),
        options.getConsentId())) {
      throw ServerCommonException.invalidArgument("userId, clientId, resourceId or consentId");
    }
    ApiProxy apiProxy = getApiProxy();
    AgenticIdentitiesRevokeResponse res = apiProxy.post(getUri(MANAGEMENT_AGENTIC_IDENTITIES_REVOKE), options,
        AgenticIdentitiesRevokeResponse.class);
    return res == null ? 0 : res.getRevoked();
  }

  @Override
  public AgenticIdentitySearchResponse searchAgenticIdentities(AgenticIdentitySearchOptions options)
      throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_AGENTIC_IDENTITIES_SEARCH), options,
        AgenticIdentitySearchResponse.class);
  }

  @Override
  public XaaAgenticIdentitiesResponse blockXaaAgenticIdentities(List<String> consentIds) throws DescopeException {
    return setXaaBlocked(MANAGEMENT_AGENTIC_IDENTITIES_XAA_BLOCK, consentIds);
  }

  @Override
  public XaaAgenticIdentitiesResponse unblockXaaAgenticIdentities(List<String> consentIds)
      throws DescopeException {
    return setXaaBlocked(MANAGEMENT_AGENTIC_IDENTITIES_XAA_UNBLOCK, consentIds);
  }

  private XaaAgenticIdentitiesResponse setXaaBlocked(String path, List<String> consentIds) {
    if (consentIds == null || consentIds.isEmpty()) {
      throw ServerCommonException.invalidArgument("consentIds");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(path), mapOf("consentIds", consentIds), XaaAgenticIdentitiesResponse.class);
  }
}
