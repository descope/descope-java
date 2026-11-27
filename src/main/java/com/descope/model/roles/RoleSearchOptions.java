package com.descope.model.roles;

import com.descope.model.mgmt.SortField;
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
  private String text; // full text search across relevant columns
  private List<SortField> sort;
  private Boolean includeProjectRoles; // relevant when tenantIds are set
  private List<String> roleIds;

  public RoleSearchOptions(List<String> tenantIds, List<String> roleNames, String roleNameLike,
      List<String> permissionNames) {
    this.tenantIds = tenantIds;
    this.roleNames = roleNames;
    this.roleNameLike = roleNameLike;
    this.permissionNames = permissionNames;
  }
}
