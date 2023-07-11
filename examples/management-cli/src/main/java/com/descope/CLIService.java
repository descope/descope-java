package com.descope;

import static java.lang.Integer.parseInt;

import com.descope.model.audit.AuditSearchRequest;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;

@Builder
final class CLIService {
  private ManagementServices managementServices;

  public void createUser(String loginId, UserRequest userRequest) {
    var userService = managementServices.getUserService();
    var userResponse = userService.create(loginId, userRequest);
    System.out.printf(
        "User with userId: %s was successfully created%n", userResponse.getUser().getUserId());
  }

  public void updateUser(String loginId, UserRequest userRequest) {
    var userService = managementServices.getUserService();
    var userResponse = userService.update(loginId, userRequest);
    System.out.printf(
        "User with userId: %s was successfully updated%n", userResponse.getUser().getUserId());
  }

  public void deleteUser(String loginId) {
    var userService = managementServices.getUserService();
    userService.delete(loginId);
    System.out.printf("User with loginId: %s was successfully deleted%n", loginId);
  }

  public void loadUser(String loginId) {
    var userService = managementServices.getUserService();
    var response = userService.load(loginId);
    System.out.printf("User with loginId: %s was successfully loaded%n", loginId);
    System.out.println(response);
  }

  public void searchAllUsers() {
    var userService = managementServices.getUserService();
    var response = userService.searchAll(null);
    System.out.println(response);
  }

  public void createAccessKey(
      String keyName,
      String expirationTime,
      List<String> roleNames,
      List<AssociatedTenant> keyTenants) {

    var accessKeyResponse =
        managementServices
            .getAccessKeyService()
            .create(keyName, parseInt(expirationTime), roleNames, keyTenants);
    System.out.printf("Access key with Id: %s was successfully Created%n", accessKeyResponse);
  }

  public void updateAccessKey(String keyId, String keyName) {
    var accessKeyService = managementServices.getAccessKeyService();
    var accessKeyResponse = accessKeyService.update(keyId, keyName);
    System.out.printf("Access key with Id: %s was successfully updated%n", accessKeyResponse);
  }

  public void deleteAccessKey(String keyId) {
    var accessKeyService = managementServices.getAccessKeyService();
    accessKeyService.delete(keyId);
    System.out.printf("Access key with keyId: %s was successfully deleted%n", keyId);
  }

  public void loadAccessKey(String keyId) {
    var accessKeyService = managementServices.getAccessKeyService();
    var accessKeyResponse = accessKeyService.load(keyId);
    System.out.printf("Access key with Id: %s was successfully loaded%n", accessKeyResponse);
  }

  public void searchAllAccessKey() {
    var accessKeyService = managementServices.getAccessKeyService();
    var accessKeyResponse = accessKeyService.searchAll(Collections.emptyList());
    System.out.println(accessKeyResponse);
  }

  public void createTenant(String tenantName) {
    var tenantService = managementServices.getTenantService();
    var response = tenantService.create(tenantName, Collections.emptyList());
    System.out.printf("Tenant with Id: %s was successfully created%n", response);
  }

  public void updateTenant(String tenantId, String tenantName) {
    var tenantService = managementServices.getTenantService();
    tenantService.update(tenantId, tenantName, null);
    System.out.printf("Tenant with Id: %s was successfully updated%n", tenantId);
  }

  public void deleteTenant(String tenantId) {
    var tenantService = managementServices.getTenantService();
    tenantService.delete(tenantId);
    System.out.printf("Tenant with Id: %s was successfully deleted%n", tenantId);
  }

  public void searchAllTenant() {
    var tenantService = managementServices.getTenantService();
    var tenantResponse = tenantService.loadAll();
    System.out.println(tenantResponse);
  }

  public void createPermission(String permissionName) {
    var permissionService = managementServices.getPermissionService();
    permissionService.create(permissionName, "");
    System.out.printf("Permission with Name: %s was successfully created%n", permissionName);
  }

  public void updatePermission(String permissionName, String permissionId) {
    var permissionService = managementServices.getPermissionService();
    permissionService.update(permissionName, permissionId, "");
    System.out.printf("permission with permission Id: %s was successfully updated%n", permissionId);
  }

  public void deletePermission(String permissionId) {
    var permissionService = managementServices.getPermissionService();
    permissionService.delete(permissionId);
    System.out.printf("Permission with Id: %s was successfully deleted%n", permissionId);
  }

  public void permissionAll() {
    var permissionService = managementServices.getPermissionService();
    var permissionResponse = permissionService.loadAll();
    System.out.println(permissionResponse);
  }

  public void createRole(String roleName) {
    var roleService = managementServices.getRolesService();
    roleService.create(roleName, "", Collections.emptyList());
    System.out.printf("Role with Name: %s was successfully created%n", roleName);
  }

  public void updateRole(String roleId, String roleName) {
    var roleService = managementServices.getRolesService();
    roleService.update(roleId, roleName, "", Collections.emptyList());
    System.out.printf("Role Name with Role Id: %s was successfully updated%n", roleId);
  }

  public void deleteRole(String roleId) {
    var roleService = managementServices.getRolesService();
    roleService.delete(roleId);
    System.out.printf("Role with Id: %s was successfully deleted%n", roleId);
  }

  public void roleAll() {
    var roleService = managementServices.getRolesService();
    var roleResponse = roleService.loadAll();
    System.out.println(roleResponse);
  }

  public void audit() {
    AuditSearchRequest auditSearchRequest =
        AuditSearchRequest.builder()
            .from(Instant.now().minus(Duration.ofDays(0)))
            .to(Instant.now().minus(Duration.ofDays(0)))
            .userIds(Collections.emptyList())
            .actions(Collections.emptyList())
            .devices(Collections.emptyList())
            .methods(Collections.emptyList())
            .geos(Collections.emptyList())
            .remoteAddresses(Collections.emptyList())
            .tenants(Collections.emptyList())
            .noTenants(false)
            .text("")
            .excludedActions(Collections.emptyList())
            .build();
    var auditService = managementServices.getAuditService();
    var response = auditService.search(auditSearchRequest);
    System.out.println(response);
  }

  public void groupAllForTenant(String tenantId) {
    var groupService = managementServices.getGroupService();
    var response = groupService.loadAllGroups(tenantId);
    System.out.println(response);
  }

  public void groupAllForMember(String tenantId, String userIds, String loginIds) {
    var groupService = managementServices.getGroupService();
    var response =
        groupService.loadAllGroupsForMembers(tenantId, getValues(userIds), getValues(loginIds));
    System.out.println(response);
  }

  public void groupMembers(String tenantId, String groupId) {
    var groupService = managementServices.getGroupService();
    var response = groupService.loadAllGroupMembers(tenantId, groupId);
    System.out.println(response);
  }

  private List<String> getValues(String str) {
    return Stream.of(str.split(",")).map(String::trim).collect(Collectors.toList());
  }
}
