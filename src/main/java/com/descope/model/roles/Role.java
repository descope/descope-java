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
public class Role {
  private String name;
  private String description;
  private List<String> permissionNames;
  private Long createdTime;
}
