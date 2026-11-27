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
  /** Input data passed to the flow. */
  private Map<String, Object> input;
  /** Run the flow in preview mode. */
  private Boolean preview;
  /** Tenant ID to run the flow in. */
  private String tenant;
}
