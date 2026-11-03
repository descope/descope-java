package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_GET;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_RESOLVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_SEARCH;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_GOVERNANCE_SETTINGS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_IDENTITIES_REVOKE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AGENTIC_IDENTITIES_SEARCH;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.agentic.AgenticIdentityRevokeOptions;
import com.descope.model.agentic.AgenticIdentityRevokeResponse;
import com.descope.model.agentic.AgenticIdentitySearchOptions;
import com.descope.model.agentic.AgenticIdentitySearchResponse;
import com.descope.model.agentic.GovernanceAccessRequest;
import com.descope.model.agentic.GovernanceAccessRequestCreateRequest;
import com.descope.model.agentic.GovernanceAccessRequestResolveRequest;
import com.descope.model.agentic.GovernanceAccessRequestSearchOptions;
import com.descope.model.agentic.GovernanceAccessRequestSearchResponse;
import com.descope.model.agentic.GovernanceAccessRequestSortField;
import com.descope.model.agentic.GovernanceSettings;
import com.descope.model.client.Client;
import com.descope.sdk.mgmt.AgenticService;
import org.apache.commons.lang3.StringUtils;

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
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_IDENTITIES_SEARCH), options,
        AgenticIdentitySearchResponse.class);
  }

  @Override
  public AgenticIdentityRevokeResponse revokeIdentities(AgenticIdentityRevokeOptions options)
      throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }
    if (StringUtils.isAllBlank(options.getUserId(), options.getClientId(), options.getResourceId(),
        options.getConsentId())) {
      throw ServerCommonException.invalidArgument("options");
    }
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_IDENTITIES_REVOKE), options,
        AgenticIdentityRevokeResponse.class);
  }

  @Override
  public GovernanceSettings getGovernanceSettings() throws DescopeException {
    return getApiProxy().get(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_SETTINGS), GovernanceSettings.class);
  }

  @Override
  public GovernanceSettings setGovernanceSettings(GovernanceSettings settings) throws DescopeException {
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_SETTINGS), settings, GovernanceSettings.class);
  }

  @Override
  public GovernanceAccessRequestSearchResponse searchGovernanceAccessRequests(
      GovernanceAccessRequestSearchOptions options) throws DescopeException {
    if (options == null) {
      throw ServerCommonException.invalidArgument("options");
    }
    if (options.getSort() != null) {
      for (GovernanceAccessRequestSortField sort : options.getSort()) {
        if (sort == null || StringUtils.isBlank(sort.getField())) {
          throw ServerCommonException.invalidArgument("options.sort.field");
        }
      }
    }
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_SEARCH), options,
        GovernanceAccessRequestSearchResponse.class);
  }

  @Override
  public GovernanceAccessRequest getGovernanceAccessRequest(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_GET), mapOf("id", id),
        GovernanceAccessRequest.class);
  }

  @Override
  public GovernanceAccessRequest resolveGovernanceAccessRequest(GovernanceAccessRequestResolveRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getId())) {
      throw ServerCommonException.invalidArgument("request.id");
    }
    if (StringUtils.isBlank(request.getAction())) {
      throw ServerCommonException.invalidArgument("request.action");
    }
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_RESOLVE), request,
        GovernanceAccessRequest.class);
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
    if (request.getGrantTypes() != null) {
      for (String grantType : request.getGrantTypes()) {
        if (StringUtils.isBlank(grantType)) {
          throw ServerCommonException.invalidArgument("request.grantTypes");
        }
      }
    }
    return getApiProxy().post(getUri(MANAGEMENT_AGENTIC_GOVERNANCE_REQUESTS_CREATE), request,
        GovernanceAccessRequest.class);
  }
}
