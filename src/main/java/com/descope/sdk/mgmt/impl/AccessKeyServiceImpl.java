package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_ACTIVE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_DEACTIVATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_SEARCH_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ACCESS_KEY_UPDATE_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.mgmt.AccessKeyRequest;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseList;
import com.descope.model.mgmt.ManagementParams;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.utils.MgmtUtils;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class AccessKeyServiceImpl extends ManagementsBase implements AccessKeyService {
  AccessKeyServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public AccessKeyResponse create(
      String name, int expireTime, List<String> roleNames, List<AssociatedTenant> keyTenants)
      throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }
    AccessKeyRequest body = createAccessKeyBody(name, expireTime, roleNames, keyTenants);
    var apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_ACCESS_KEY_CREATE_LINK), body, AccessKeyResponse.class);
  }

  @Override
  public AccessKeyResponse load(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("Id");
    }
    var apiProxy = getApiProxy();
    return apiProxy.get(
        getQueryParamUri(MANAGEMENT_ACCESS_KEY_LOAD_LINK, Map.of("id", id)),
        AccessKeyResponse.class);
  }

  @Override
  public AccessKeyResponseList searchAll(List<String> tenantIDs) throws DescopeException {
    Map<String, List<String>> request = Map.of("tenantIds", tenantIDs);
    var apiProxy = getApiProxy();
    return apiProxy.post(
        getUri(MANAGEMENT_ACCESS_KEY_SEARCH_ALL_LINK), request, AccessKeyResponseList.class);
  }

  @Override
  public AccessKeyResponse update(String id, String name) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("Id");
    }
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("Name");
    }

    Map<String, String> request = Map.of("id", id, "name", name);
    var apiProxy = getApiProxy();
    return apiProxy.post(
        getUri(MANAGEMENT_ACCESS_KEY_UPDATE_LINK), request, AccessKeyResponse.class);
  }

  @Override
  public AccessKeyResponse deactivate(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("Id");
    }
    Map<String, String> request = Map.of("id", id);
    var apiProxy = getApiProxy();
    return apiProxy.post(
        getUri(MANAGEMENT_ACCESS_KEY_DEACTIVATE_LINK), request, AccessKeyResponse.class);
  }

  @Override
  public AccessKeyResponse activate(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("Id");
    }
    Map<String, String> request = Map.of("id", id);
    var apiProxy = getApiProxy();
    return apiProxy.post(
        getUri(MANAGEMENT_ACCESS_KEY_ACTIVE_LINK), request, AccessKeyResponse.class);
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("Id");
    }
    Map<String, String> request = Map.of("id", id);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ACCESS_KEY_DELETE_LINK), request, Void.class);
  }

  private AccessKeyRequest createAccessKeyBody(
      String name, int expireTime, List<String> roleNames, List<AssociatedTenant> keyTenants) {
    return AccessKeyRequest.builder()
        .name(name)
        .expireTime(expireTime)
        .roleNames(roleNames)
        .keyTenants(MgmtUtils.createAssociatedTenantList(keyTenants))
        .build();
  }
}
