package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.GROUP_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_FOR_GROUP_MEMBERS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_GROUP_MEMBERS_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.group.Group;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.GroupService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class GroupServiceImpl extends ManagementsBase implements GroupService {
  GroupServiceImpl(Client client) {
    super(client);
  }

  @Override
  public List<Group> loadAllGroups(String tenantID) throws DescopeException {
    return loadAllGroups(tenantID, null);
  }

  @Override
  public List<Group> loadAllGroups(String tenantID, String ssoId) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, String> request = mapOf("tenantId", tenantID);
    if (StringUtils.isNotBlank(ssoId)) {
      request.put("ssoId", ssoId);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.postAndGetArray(
        getUri(GROUP_LOAD_ALL_LINK), request, new TypeReference<List<Group>>() {});
  }

  @Override
  public List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs) throws DescopeException {
    return loadAllGroupsForMembers(tenantID, userIDs, loginIDs, null);
  }

  @Override
  public List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs, String ssoId) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (userIDs.isEmpty() && loginIDs.isEmpty()) {
      throw ServerCommonException.invalidArgument("userIDs and loginIDs");
    }
    Map<String, Object> request =
        mapOf("tenantId", tenantID, "loginIds", loginIDs, "userIds", userIDs);
    if (StringUtils.isNotBlank(ssoId)) {
      request.put("ssoId", ssoId);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.postAndGetArray(
        getUri(LOAD_ALL_FOR_GROUP_MEMBERS_LINK), request, new TypeReference<List<Group>>() {});
  }

  @Override
  public List<Group> loadAllGroupMembers(String tenantID, String groupID) throws DescopeException {
    return loadAllGroupMembers(tenantID, groupID, null);
  }

  @Override
  public List<Group> loadAllGroupMembers(String tenantID, String groupID, String ssoId)
      throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (StringUtils.isBlank(groupID)) {
      throw ServerCommonException.invalidArgument("GroupID");
    }
    Map<String, String> request = mapOf("tenantId", tenantID, "groupId", groupID);
    if (StringUtils.isNotBlank(ssoId)) {
      request.put("ssoId", ssoId);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.postAndGetArray(
        getUri(LOAD_ALL_GROUP_MEMBERS_LINK), request, new TypeReference<List<Group>>() {});
  }
}
