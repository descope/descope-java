package com.descope.model.mgmt;

import com.descope.sdk.mgmt.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagementServices {
  JwtService jwtService;
  TenantService tenantService;
  UserService userService;
  AccessKeyService accessKeyService;
  PermissionService permissionService;
  RolesService rolesService;
  SsoService ssoService;
  SsoApplicationService ssoApplicationService;
  FlowService flowService;
  GroupService groupService;
  AuditService auditService;
  AuthzService authzService;
  ProjectService projectService;
  PasswordSettingsService passwordSettingsService;
  OutboundAppsService outboundAppsService;
  InboundAppsService inboundAppsService;
  UserCustomAttributesService userCustomAttributesService;
}
