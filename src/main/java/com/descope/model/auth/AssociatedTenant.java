package com.descope.model.auth;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedTenant {
  private String tenantId;
  private String tenantName;
  private List<String> roleNames;
  private List<String> permissions;
  /** Not in the same order as roleNames. */
  private List<String> roleIds;

  public AssociatedTenant(String tenantId, String tenantName, List<String> roleNames) {
    this.tenantId = tenantId;
    this.tenantName = tenantName;
    this.roleNames = roleNames;
  }
}
