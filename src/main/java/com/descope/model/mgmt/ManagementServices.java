package com.descope.model.mgmt;

import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.JwtService;
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
}
