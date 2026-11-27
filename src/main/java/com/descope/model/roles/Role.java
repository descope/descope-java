package com.descope.model.roles;

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
public class Role {
  private String name;
  private String tenantId;
  private String description;
  private List<String> permissionNames;
  private Long createdTime;
  // Read only: createBatch does not accept an id
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String id;
  @JsonProperty("default")
  private Boolean defaultRole;
  @JsonProperty("private")
  private Boolean privateRole;

  public Role(String name, String tenantId, String description, List<String> permissionNames, Long createdTime) {
    this.name = name;
    this.tenantId = tenantId;
    this.description = description;
    this.permissionNames = permissionNames;
    this.createdTime = createdTime;
  }
}
