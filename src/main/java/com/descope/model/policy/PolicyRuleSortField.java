package com.descope.model.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRuleSortField {
  /** Required. */
  private String field;
  private Boolean desc;
}
