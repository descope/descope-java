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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class GroupServiceImpl extends ManagementsBase implements GroupService {
  GroupServiceImpl(Client client) {
    super(client);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Group> loadAllGroups(String tenantID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, String> request = mapOf("tenantId", tenantID);
    ApiProxy apiProxy = getApiProxy();
    return (List<Group>) apiProxy.post(getUri(GROUP_LOAD_ALL_LINK), request, List.class);
  }

  @Override
  public List<Group> loadAllGroups(String tenantID, String ssoID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, Object> request = new HashMap<>();
    request.put("tenantId", tenantID);
    return loadGroups(GROUP_LOAD_ALL_LINK, request, ssoID);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (userIDs.isEmpty() && loginIDs.isEmpty()) {
      throw ServerCommonException.invalidArgument("userIDs and loginIDs");
    }
    Map<String, Object> request =
        mapOf("tenantId", tenantID, "loginIds", loginIDs, "userIds", userIDs);
    ApiProxy apiProxy = getApiProxy();
    return (List<Group>)
        apiProxy.post(getUri(LOAD_ALL_FOR_GROUP_MEMBERS_LINK), request, List.class);
  }

  @Override
  public List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs, String ssoID)
      throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if ((userIDs == null || userIDs.isEmpty()) && (loginIDs == null || loginIDs.isEmpty())) {
      throw ServerCommonException.invalidArgument("userIDs and loginIDs");
    }
    Map<String, Object> request = new HashMap<>();
    request.put("tenantId", tenantID);
    if (loginIDs != null) {
      request.put("loginIds", loginIDs);
    }
    if (userIDs != null) {
      request.put("userIds", userIDs);
    }
    return loadGroups(LOAD_ALL_FOR_GROUP_MEMBERS_LINK, request, ssoID);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Group> loadAllGroupMembers(String tenantID, String groupID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (StringUtils.isBlank(groupID)) {
      throw ServerCommonException.invalidArgument("GroupID");
    }
    Map<String, String> request = mapOf("tenantId", tenantID, "groupId", groupID);
    ApiProxy apiProxy = getApiProxy();
    return (List<Group>) apiProxy.post(getUri(LOAD_ALL_GROUP_MEMBERS_LINK), request, List.class);
  }

  @Override
  public List<Group> loadAllGroupMembers(String tenantID, String groupID, String ssoID)
      throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (StringUtils.isBlank(groupID)) {
      throw ServerCommonException.invalidArgument("GroupID");
    }
    Map<String, Object> request = new HashMap<>();
    request.put("tenantId", tenantID);
    request.put("groupId", groupID);
    return loadGroups(LOAD_ALL_GROUP_MEMBERS_LINK, request, ssoID);
  }

  private List<Group> loadGroups(String path, Map<String, Object> request, String ssoID) {
    if (StringUtils.isNotBlank(ssoID)) {
      request.put("ssoId", ssoID);
    }
    JsonNode res = getApiProxy().post(getUri(path), request, JsonNode.class);
    // Accept both a bare array and a {"groups": [...]} object
    JsonNode groups = res != null && res.isObject() ? res.get("groups") : res;
    if (groups == null || !groups.isArray()) {
      return Collections.emptyList();
    }
    ObjectMapper mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.convertValue(groups, new TypeReference<List<Group>>() {});
  }
}
