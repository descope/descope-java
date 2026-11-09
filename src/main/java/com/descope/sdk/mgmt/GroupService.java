package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.group.Group;
import java.util.List;

public interface GroupService {

  List<Group> loadAllGroups(String tenantID) throws DescopeException;

  /**
   * Load all groups for the given tenant, optionally only those from one SSO configuration.
   *
   * @param tenantID the tenant ID
   * @param ssoID optional SSO configuration ID (max 254 chars); {@code null} or empty returns all
   *     the tenant's groups, "default_ssoid" selects the tenant's default SSO configuration
   * @return the groups
   * @throws DescopeException if the request fails
   */
  List<Group> loadAllGroups(String tenantID, String ssoID) throws DescopeException;

  List<Group> loadAllGroupsForMembers(String tenantID, List<String> userIDs, List<String> loginIDs)
      throws DescopeException;

  /**
   * Load all groups for the given user/login IDs, optionally only those from one SSO configuration.
   *
   * @param tenantID the tenant ID
   * @param userIDs user IDs to load groups for
   * @param loginIDs login IDs to load groups for
   * @param ssoID optional SSO configuration ID (max 254 chars); {@code null} or empty returns
   *     groups from all SSO configurations, "default_ssoid" selects the tenant's default one
   * @return the groups
   * @throws DescopeException if the request fails
   */
  List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs, String ssoID)
      throws DescopeException;

  List<Group> loadAllGroupMembers(String tenantID, String groupID) throws DescopeException;

  /**
   * Load the given group with its members, only if it came from the given SSO configuration.
   *
   * @param tenantID the tenant ID
   * @param groupID the group ID
   * @param ssoID optional SSO configuration ID (max 254 chars); {@code null} or empty skips the
   *     filter, "default_ssoid" selects the tenant's default SSO configuration
   * @return the groups
   * @throws DescopeException if the request fails
   */
  List<Group> loadAllGroupMembers(String tenantID, String groupID, String ssoID)
      throws DescopeException;
}
