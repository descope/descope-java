package com.descope.model.policy;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRuleResourceTarget {
  /** Required. */
  private String type;
  /** Matches every resource of the type; otherwise ids lists the specific resources. */
  private Boolean allOfType;
  private List<String> ids;
}
