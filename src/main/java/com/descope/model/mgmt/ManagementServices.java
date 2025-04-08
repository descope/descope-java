package com.descope.model.mgmt;

import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.AuditService;
import com.descope.sdk.mgmt.AuthzService;
import com.descope.sdk.mgmt.FlowService;
import com.descope.sdk.mgmt.GroupService;
import com.descope.sdk.mgmt.InboundAppsService;
import com.descope.sdk.mgmt.JwtService;
import com.descope.sdk.mgmt.OutboundAppsService;
import com.descope.sdk.mgmt.PasswordSettingsService;
import com.descope.sdk.mgmt.PermissionService;
import com.descope.sdk.mgmt.ProjectService;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.SsoApplicationService;
import com.descope.sdk.mgmt.SsoService;
import com.descope.sdk.mgmt.TenantService;
import com.descope.sdk.mgmt.UserService;
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
}
