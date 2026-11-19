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
  // Response only: user create and update requests don't accept them
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> permissions;
  /** Not in the same order as roleNames; don't pair them by index. */
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> roleIds;

  public AssociatedTenant(String tenantId, String tenantName, List<String> roleNames) {
    this(tenantId, tenantName, roleNames, null, null);
  }
}
