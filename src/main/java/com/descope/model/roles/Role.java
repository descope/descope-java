package com.descope.model.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
