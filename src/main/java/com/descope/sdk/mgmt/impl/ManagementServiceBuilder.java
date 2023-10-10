package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.mgmt.ManagementServices;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ManagementServiceBuilder {
  public static ManagementServices buildServices(Client client, ManagementParams managementParams) {
    return ManagementServices.builder()
        .ssoService(new SsoServiceImpl(client, managementParams))
        .jwtService(new JwtServiceImpl(client, managementParams))
        .userService(new UserServiceImpl(client, managementParams))
        .flowService(new FlowServiceImpl(client, managementParams))
        .rolesService(new RolesServiceImpl(client, managementParams))
        .groupService(new GroupServiceImpl(client, managementParams))
        .tenantService(new TenantServiceImpl(client, managementParams))
        .accessKeyService(new AccessKeyServiceImpl(client, managementParams))
        .permissionService(new PermissionServiceImpl(client, managementParams))
        .auditService(new AuditServiceImpl(client, managementParams))
        .authzService(new AuthzServiceImpl(client, managementParams))
        .build();
  }
}
