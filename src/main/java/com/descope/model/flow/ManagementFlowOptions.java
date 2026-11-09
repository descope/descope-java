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
public class ManagementFlowOptions {
  private Map<String, Object> input;
  private Boolean preview;
  /** Tenant ID, up to 254 characters. */
  private String tenant;
}
