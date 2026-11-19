package com.descope.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
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
  // Response only: never sent back when a loaded tenant is reused in a request
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> permissions;
  /** Role IDs, not in the same order as roleNames. */
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> roleIds;

  public AssociatedTenant(String tenantId, String tenantName, List<String> roleNames) {
    this.tenantId = tenantId;
    this.tenantName = tenantName;
    this.roleNames = roleNames;
  }
}
