package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.group.Group;
import java.util.List;

public interface GroupService {

  List<Group> loadAllGroups(String tenantID) throws DescopeException;

  List<Group> loadAllGroupsForMembers(String tenantID, List<String> userIDs, List<String> loginIDs) throws DescopeException;

  List<Group> loadAllGroupMembers(String tenantID, String groupID) throws DescopeException;
}
