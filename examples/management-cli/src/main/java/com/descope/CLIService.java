package com.descope;

import com.descope.model.auth.AssociatedTenant;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.response.UserResponse;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

@Builder
final class CLIService {
    private ManagementServices managementServices;

    public void createUser(String loginId, UserRequest userRequest) {
        var userService = managementServices.getUserService();
        var userResponse = userService.create(loginId, userRequest);
        String userId = userResponse.getUserId();
        System.out.printf("User with userId: %s was successfully created%n", userId);
    }

    public void updateUser(String loginId, UserRequest userRequest) {
        var userService = managementServices.getUserService();
        var userResponse = userService.update(loginId, userRequest);
        String userId = userResponse.getUserId();
        System.out.printf("User with userId: %s was successfully updated%n", userId);
    }

    public void deleteUser(String loginId) {
        var userService = managementServices.getUserService();
        userService.delete(loginId);
        System.out.printf("User with loginId: %s was successfully deleted%n", loginId);
    }

    public void loadUser(String loginId) {
        var userService = managementServices.getUserService();
        UserResponse response = userService.load(loginId);
        System.out.printf("User with loginId: %s was successfully loaded%n", loginId);
        System.out.println(response);
    }

    public void searchAllUsers() {
        var userService = managementServices.getUserService();
        List<UserResponse> response = userService.searchAll(null);
        System.out.println(response);
    }

    public void createAccessKey(String keyName, String expirationTime, List<String> roleNames, List<AssociatedTenant> keyTenants) {

        var accessKeyResponse = managementServices.getAccessKeyService().create(keyName, parseInt(expirationTime), roleNames, keyTenants);
        System.out.printf("Access key with Id: %s was successfully Created%n", accessKeyResponse.getId());
    }

    public void updateAccessKey(String keyId, String keyName) {
        var accessKeyService = managementServices.getAccessKeyService();
        var accessKeyResponse = accessKeyService.update(keyId, keyName);
        System.out.printf("Access key with Id: %s was successfully updated%n", accessKeyResponse.getId());
    }

    public void deleteAccessKey(String keyId) {
        var accessKeyService = managementServices.getAccessKeyService();
        var accessKeyResponse = accessKeyService.delete(keyId);
        System.out.printf("Access key with keyId: %s was successfully deleted%n", accessKeyResponse.getId());
    }

    public void loadAccessKey(String keyId) {
        var accessKeyService = managementServices.getAccessKeyService();
        var accessKeyResponse = accessKeyService.load(keyId);
        System.out.printf("Access key with Id: %s was successfully loaded%n", accessKeyResponse.getId());
    }

    public void searchAllAccessKey() {
        var accessKeyService = managementServices.getAccessKeyService();
        var accessKeyResponse = accessKeyService.searchAll(null);
        System.out.println(accessKeyService);
    }

    public void createTenant(String tenantName) {
        var tenantService = managementServices.getTenantService();
        var response = tenantService.create(tenantName, null);
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
        permissionService.create(permissionName, null);
        System.out.printf("Permission with Name: %s was successfully created%n", permissionName);
    }

    public void updatePermission(String permissionName, String permissionId) {
        var permissionService = managementServices.getPermissionService();
        permissionService.update(permissionName, permissionId, null);
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
        roleService.create(roleName, null, null);
        System.out.printf("Role with Name: %s was successfully created%n", roleName);
    }

    public void updateRole(String roleId, String roleName) {
        var roleService = managementServices.getRolesService();
        roleService.update(roleId, roleName, null, null);
        System.out.printf("Role Name with Role Id: %s was successfully updated%n", roleId);
    }

    public void deleteRole(String roleId) {
        var roleService = managementServices.getRolesService();
        roleService.delete(roleId);
        System.out.printf("Role with Id: %s was successfully deleted%n", roleId);
    }

    public void roleAll() {
        var roleService = managementServices.getPermissionService();
        var roleResponse = roleService.loadAll();
        System.out.println(roleResponse);
    }

    public void groupAllForTenant(String tenantId) {
        var groupService = managementServices.getGroupService();
        var response = groupService.loadAllGroups(tenantId);
        System.out.println(response);
    }

    public void groupAllForMember(String tenantId, String userIds, String loginIds) {
        var groupService = managementServices.getGroupService();
        var response = groupService.loadAllGroupsForMembers(tenantId, getValues(userIds), getValues(loginIds));
        System.out.println(response);
    }

    public void groupMembers(String tenantId, String groupId) {
        var groupService = managementServices.getGroupService();
        var response = groupService.loadAllGroupMembers(tenantId, groupId);
        System.out.println(response);

    }

    private List<String> getValues(String str) {
        return Stream.of(str.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
