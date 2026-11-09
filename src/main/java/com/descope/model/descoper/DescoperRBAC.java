package com.descope.model.descoper;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescoperRBAC {
  private Boolean isCompanyAdmin;
  private List<DescoperTagRole> tags;
  private List<DescoperProjectRole> projects;
}
