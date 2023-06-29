package com.descope;

import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.UserRequest;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class ManagementCLI {

    public static void main(String[] args) {
        final String DESCOPE_BASE_URL = System.getenv("DESCOPE_BASE_URL");
        final String DESCOPE_PROJECT_ID = System.getenv("DESCOPE_PROJECT_ID");
        final String DESCOPE_MANAGEMENT_KEY = System.getenv("DESCOPE_MANAGEMENT_KEY");

        if (StringUtils.isAnyBlank(DESCOPE_PROJECT_ID, DESCOPE_MANAGEMENT_KEY)) {
            throw new UnsupportedOperationException("Project ID / Management ID is not set");
        }

        Client.ClientBuilder clientBuilder = Client.builder();
        if (StringUtils.isNoneBlank(DESCOPE_BASE_URL)) {
            clientBuilder.uri(DESCOPE_BASE_URL);
        }
        var client = clientBuilder.build();

        var managementParams =
                ManagementParams.builder()
                        .projectId(DESCOPE_PROJECT_ID)
                        .managementKey(DESCOPE_MANAGEMENT_KEY)
                        .build();
        var managementServices = ManagementServiceBuilder.buildServices(client, managementParams);
        var cliService = CLIService.builder().managementServices(managementServices).build();

        String operation = args[0];
        switch (operation) {
            case "user-create" -> {
                String loginId = System.getProperty("loginId");
                String email = System.getProperty("email");
                String phone = System.getProperty("phone");
                String name = System.getProperty("name");

                if (StringUtils.isAnyBlank(loginId, email, phone, name)) {
                    throw new IllegalArgumentException(
                            "Login ID, Email, Phone and Name are Required. Please pass these arguments as `-D<key>=<value>`");
                }

                var userRequest =
                        UserRequest.builder()
                                .loginId(loginId)
                                .email(email)
                                .phone(phone)
                                .displayName(name)
                                .roleNames(Collections.emptyList())
                                .tenants(Collections.emptyList())
                                .customAttributes(Collections.emptyMap())
                                .picture("")
                                .build();
                cliService.createUser(loginId, userRequest);
            }

            case "user-update" -> {
                String loginId = System.getProperty("loginId");
                String email = System.getProperty("email");
                String phone = System.getProperty("phone");
                String name = System.getProperty("name");

                if (StringUtils.isAnyBlank(loginId, email, phone, name)) {
                    throw new IllegalArgumentException(
                            "Login ID, Email, Phone and Name are Required. Please pass these arguments as `-D<key>=<value>`");
                }

                var userRequest =
                        UserRequest.builder()
                                .loginId(loginId)
                                .email(email)
                                .phone(phone)
                                .displayName(name)
                                .roleNames(Collections.emptyList())
                                .tenants(Collections.emptyList())
                                .customAttributes(Collections.emptyMap())
                                .picture("")
                                .invite(false)
                                .test(false)
                                .build();
                cliService.updateUser(loginId, userRequest);
            }

            case "user-delete" -> {
                String loginId = System.getProperty("loginId");

                if (StringUtils.isAnyBlank(loginId)) {
                    throw new IllegalArgumentException(
                            "Login ID is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.deleteUser(loginId);
            }

            case "user-load" -> {
                String loginId = System.getProperty("loginId");

                if (StringUtils.isAnyBlank(loginId)) {
                    throw new IllegalArgumentException(
                            "Login ID is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.loadUser(loginId);
            }
            case "user-search-all" -> cliService.searchAllUsers();

            case "access-key-create" -> {
                String keyName = System.getProperty("keyName");
                String expirationTime = System.getProperty("expirationTime");

                if (StringUtils.isAnyBlank(keyName, expirationTime)) {
                    throw new IllegalArgumentException(
                            "Key Name and Expiration Time are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                AssociatedTenant associatedTenant = new AssociatedTenant();
                associatedTenant.setRoleNames(Collections.emptyList());
                associatedTenant.setTenantId("");
                cliService.createAccessKey(keyName, expirationTime, Collections.emptyList(), List.of(associatedTenant));
            }
            case "access-key-update" -> {
                String keyId = System.getProperty("keyId");
                String keyName = System.getProperty("keyName");

                if (StringUtils.isAnyBlank(keyId, keyName)) {
                    throw new IllegalArgumentException(
                            "Key ID, Key Name are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.updateAccessKey(keyId, keyName);
            }
            case "access-key-delete" -> {
                String keyId = System.getProperty("keyId");

                if (StringUtils.isAnyBlank(keyId)) {
                    throw new IllegalArgumentException(
                            "Key ID is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.deleteAccessKey(keyId);
            }
            case "access-key-load" -> {
                String keyId = System.getProperty("keyId");

                if (StringUtils.isAnyBlank(keyId)) {
                    throw new IllegalArgumentException(
                            "Key ID is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.loadAccessKey(keyId);
            }
            case "access-key-search-all" -> cliService.searchAllAccessKey();

            case "tenant-create" -> {
                String tenantName = System.getProperty("tenantName");

                if (StringUtils.isAnyBlank(tenantName)) {
                    throw new IllegalArgumentException(
                            "Tenant Name is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.createTenant(tenantName);
            }
            case "tenant-update" -> {
                String tenantId = System.getProperty("tenantId");
                String tenantName = System.getProperty("tenantName");

                if (StringUtils.isAnyBlank(tenantId, tenantName)) {
                    throw new IllegalArgumentException(
                            "Tenant ID, Tenant Name are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.updateTenant(tenantId, tenantName);
            }
            case "tenant-delete" -> {
                String tenantId = System.getProperty("tenantId");

                if (StringUtils.isAnyBlank(tenantId)) {
                    throw new IllegalArgumentException(
                            "Tenant ID is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.deleteTenant(tenantId);
            }
            case "tenant-search-all" -> cliService.searchAllTenant();

            case "group-all-for-tenant" -> {
                String tenantId = System.getProperty("tenantId");

                if (StringUtils.isAnyBlank(tenantId)) {
                    throw new IllegalArgumentException(
                            "Tenant Id is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.groupAllForTenant(tenantId);
            }

            case "group-all-for-member" -> {
                String tenantId = System.getProperty("tenantId");
                String userIds = System.getProperty("userIds");
                String loginIds = System.getProperty("loginIds");

                if (StringUtils.isAnyBlank(tenantId, userIds, loginIds)) {
                    throw new IllegalArgumentException(
                            "Tenant ID, userIds and loginIds are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.groupAllForMember(tenantId, userIds, loginIds);
            }
            case "group-members" -> {
                String tenantId = System.getProperty("tenantId");
                String groupId = System.getProperty("groupId");

                if (StringUtils.isAnyBlank(tenantId, groupId)) {
                    throw new IllegalArgumentException(
                            "Tenant ID, Group Id are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.groupMembers(tenantId, groupId);
            }

            case "permission-create" -> {
                String permissionName = System.getProperty("permissionName");

                if (StringUtils.isAnyBlank(permissionName)) {
                    throw new IllegalArgumentException(
                            "Permission Name Is Required. Please pass these arguments as `-D<key>=<value>`");
                }

                cliService.createPermission(permissionName);
            }
            case "permission-update" -> {
                String permissionName = System.getProperty("permissionName");
                String permissionId = System.getProperty("permissionId");

                if (StringUtils.isAnyBlank(permissionName, permissionId)) {
                    throw new IllegalArgumentException(
                            "Permission Name, Permission Id are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.updatePermission(permissionName, permissionId);
            }
            case "permission-delete" -> {
                String permissionId = System.getProperty("permissionId");

                if (StringUtils.isAnyBlank(permissionId)) {
                    throw new IllegalArgumentException(
                            "Permission Id is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.deletePermission(permissionId);
            }
            case "permission-all" -> cliService.permissionAll();

            case "role-create" -> {
                String roleName = System.getProperty("roleName");

                if (StringUtils.isAnyBlank(roleName)) {
                    throw new IllegalArgumentException(
                            "Role Name Is Required. Please pass these arguments as `-D<key>=<value>`");
                }

                cliService.createRole(roleName);
            }
            case "role-update" -> {
                String roleName = System.getProperty("roleName");
                String roleId = System.getProperty("roleId");

                if (StringUtils.isAnyBlank(roleId, roleName)) {
                    throw new IllegalArgumentException(
                            "Role Name, Role Id are Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.updateRole(roleId, roleName);
            }
            case "role-delete" -> {
                String roleId = System.getProperty("roleId");

                if (StringUtils.isAnyBlank(roleId)) {
                    throw new IllegalArgumentException(
                            "Role Id is Required. Please pass these arguments as `-D<key>=<value>`");
                }
                cliService.deleteRole(roleId);
            }
            case "role-all" -> cliService.roleAll();

            default -> throw new UnsupportedOperationException(
                    "Invalid Operation %s".formatted(operation));
        }
    }
}
