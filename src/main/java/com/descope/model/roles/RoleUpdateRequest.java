package com.descope.model.roles;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleUpdateRequest {
  private String name;
  private String id;
  private String newName;
  private String description;
  private List<String> permissionNames;
  private String tenantId;
}
