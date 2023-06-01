package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.GROUP_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_FOR_GROUP_MEMBERS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_ALL_GROUP_MEMBERS_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.group.Group;
import com.descope.model.mgmt.ManagementParams;
import com.descope.sdk.mgmt.GroupService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class GroupServiceImpl extends ManagementsBase implements GroupService {
  GroupServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public List<Group> loadAllGroups(String tenantID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, String> request = Map.of("tenantId", tenantID);
    var apiProxy = getApiProxy();
    return (List<Group>) apiProxy.post(getUri(GROUP_LOAD_ALL_LINK), request, List.class);
  }

  @Override
  public List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (userIDs.size() == 0 && loginIDs.size() == 0) {
      throw ServerCommonException.invalidArgument("userIDs and loginIDs");
    }
    Map<String, Object> request =
        Map.of("tenantId", tenantID, "loginIds", loginIDs, "userIds", userIDs);
    var apiProxy = getApiProxy();
    return (List<Group>) apiProxy.post(getUri(LOAD_ALL_FOR_GROUP_MEMBERS_LINK), request, List.class);
  }

  @Override
  public List<Group> loadAllGroupMembers(String tenantID, String groupID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (StringUtils.isBlank(groupID)) {
      throw ServerCommonException.invalidArgument("GroupID");
    }
    Map<String, String> request = Map.of("tenantId", tenantID, "groupId", groupID);
    var apiProxy = getApiProxy();
    return (List<Group>) apiProxy.post(getUri(LOAD_ALL_GROUP_MEMBERS_LINK), request, List.class);
  }
}
