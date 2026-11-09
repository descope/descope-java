package com.descope.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MgmtSignInOptions {
  private String ssoAppId;
  private Integer refreshDuration;
  private String selectedTenantId;
  private Boolean ignoreEnforceSSO;
}
