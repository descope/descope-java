package com.descope.model.roles;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSearchOptions {
  private List<String> tenantIds;
  private List<String> roleNames;
  private String roleNameLike; // match role names that contain the given string case insensitive
  private List<String> permissionNames;
}
