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
   * @param ssoId optional SSO configuration ID; only groups from it are returned. Use
   *     {@code "default_ssoid"} for the tenant's default SSO configuration. Pass {@code null} or
   *     empty to get all the tenant's groups (same as {@link #loadAllGroups(String)}).
   * @return the groups
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  List<Group> loadAllGroups(String tenantID, String ssoId) throws DescopeException;

  List<Group> loadAllGroupsForMembers(String tenantID, List<String> userIDs, List<String> loginIDs)
      throws DescopeException;

  /**
   * Load all groups of the given members, optionally only those from one SSO configuration.
   *
   * @param tenantID the tenant ID
   * @param userIDs the members' user IDs; at least one of userIDs or loginIDs is required
   * @param loginIDs the members' login IDs; at least one of userIDs or loginIDs is required
   * @param ssoId optional SSO configuration ID; only groups from it are returned. Use
   *     {@code "default_ssoid"} for the tenant's default SSO configuration. Pass {@code null} or
   *     empty for no filter (same as {@link #loadAllGroupsForMembers(String, List, List)}).
   * @return the groups
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  List<Group> loadAllGroupsForMembers(
      String tenantID, List<String> userIDs, List<String> loginIDs, String ssoId) throws DescopeException;

  List<Group> loadAllGroupMembers(String tenantID, String groupID) throws DescopeException;

  /**
   * Load the given group's members, optionally only if the group came from one SSO configuration.
   *
   * @param tenantID the tenant ID
   * @param groupID the group ID
   * @param ssoId optional SSO configuration ID; the group is returned only if it came from it. Use
   *     {@code "default_ssoid"} for the tenant's default SSO configuration. Pass {@code null} or
   *     empty for no filter (same as {@link #loadAllGroupMembers(String, String)}).
   * @return the group with its members
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  List<Group> loadAllGroupMembers(String tenantID, String groupID, String ssoId) throws DescopeException;
}
