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
public class TenantTheme {
  /**
   * The tenant's styles, keyed by style flavor (such as "light" and "dark", or "&lt;styleId&gt;-light" for a named
   * style), in the same shape the project theme export and import use. A logo is given as a base64 data URI.
   */
  private Map<String, Map<String, Object>> styles;
  /** Read-only: the flow-components version the styles render against. Ignored on write. */
  private String componentsVersion;
}
