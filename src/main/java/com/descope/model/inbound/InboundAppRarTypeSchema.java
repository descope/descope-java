package com.descope.model.inbound;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The schema of one authorization details type. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppRarTypeSchema {
  /** Required. */
  private String type;
  private Map<String, Object> schema;
}
