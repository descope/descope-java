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
  // Read from responses only; management requests don't accept them
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> permissions;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<String> roleIds;

  public AssociatedTenant(String tenantId, String tenantName, List<String> roleNames) {
    this(tenantId, tenantName, roleNames, null, null);
  }
}
