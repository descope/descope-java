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
public class PolicyRuleSearchRequest {
  private List<PolicyRuleSearchField> searchFields;
  private Integer batchSize;
  private Integer page;
  private String text;
  private List<PolicyRuleSortField> sort;
  private List<String> ruleFamily;
  private List<String> actionKind;
  private List<String> effect;
  private List<String> principalType;
  private List<String> resourceType;
  private Boolean enabled;
}
