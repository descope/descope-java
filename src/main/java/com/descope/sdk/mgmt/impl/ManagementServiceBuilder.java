package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ManagementServiceBuilder {
  public static ManagementServices buildServices(Client client) {
    return ManagementServices.builder()
        .ssoService(new SsoServiceImpl(client))
        .jwtService(new JwtServiceImpl(client))
        .userService(new UserServiceImpl(client))
        .flowService(new FlowServiceImpl(client))
        .rolesService(new RolesServiceImpl(client))
        .groupService(new GroupServiceImpl(client))
        .tenantService(new TenantServiceImpl(client))
        .accessKeyService(new AccessKeyServiceImpl(client))
        .permissionService(new PermissionServiceImpl(client))
        .auditService(new AuditServiceImpl(client))
        .authzService(new AuthzServiceImpl(client))
        .projectService(new ProjectServiceImpl(client))
        .passwordSettingsService(new PasswordSettingsServiceImpl(client))
        .ssoApplicationService(new SsoApplicationServiceImpl(client))
        .outboundAppsService(new OutboundAppsServiceImpl(client))
        .inboundAppsService(new InboundAppsServiceImpl(client))
        .build();
  }
}
