package com.descope.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MgmtLoginOptions {
  private String ssoAppId;
  // Session refresh duration in seconds
  private Integer refreshDuration;
  private String selectedTenantId;
  // Keep existing tenant memberships that enforce SSO in the JWT
  private Boolean ignoreEnforceSSO;
}
