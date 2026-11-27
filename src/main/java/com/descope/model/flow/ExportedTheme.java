package com.descope.model.flow;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportedTheme {
  /** The theme styles. Required on import. */
  private Map<String, Object> styles;
  private String componentsVersion;
  private ThemeReferences references;
}
