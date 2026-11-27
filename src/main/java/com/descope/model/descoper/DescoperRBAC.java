package com.descope.model.descoper;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The roles a descoper has across the company and its projects. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescoperRBAC {
  private Boolean isCompanyAdmin;
  private List<DescoperTagRole> tags;
  private List<DescoperProjectRole> projects;
}
