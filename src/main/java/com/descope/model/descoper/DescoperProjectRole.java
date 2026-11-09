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
public class DescoperProjectRole {
  private List<String> projectIds;
  // Required
  private String role;
}
