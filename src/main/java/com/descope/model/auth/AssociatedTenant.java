package com.descope.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
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
  // Response only: never sent when a tenant is passed back in a request
  @JsonProperty(access = Access.WRITE_ONLY)
  private List<String> permissions;
  @JsonProperty(access = Access.WRITE_ONLY)
  private List<String> roleIds;

  public AssociatedTenant(String tenantId, String tenantName, List<String> roleNames) {
    this(tenantId, tenantName, roleNames, null, null);
  }
}
