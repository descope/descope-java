package com.descope.model.descoper;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A role a descoper has in the given projects. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescoperProjectRole {
  private List<String> projectIds;
  /** One of the {@link DescoperRole} values (required). */
  private String role;
}
