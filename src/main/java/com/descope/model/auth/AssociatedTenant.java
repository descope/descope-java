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
  // Response only, never sent in requests
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> permissions;
  /** Role IDs, not guaranteed to be in the same order as {@code roleNames}. */
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> roleIds;

  // Keeps the pre-existing all-args constructor binary compatible
  public AssociatedTenant(String tenantId, String tenantName, List<String> roleNames) {
    this.tenantId = tenantId;
    this.tenantName = tenantName;
    this.roleNames = roleNames;
  }
}
