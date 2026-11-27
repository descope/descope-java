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
public class PolicyRuleSearchField {
  private String field;
  private List<String> valArr;
  private String valStr;
  private Boolean negative;
  private String valType;
}
