package com.descope.model.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRuleCondition {
  public static final String OPERATOR_EQUAL = "equal";
  public static final String OPERATOR_NOT_EQUAL = "notEqual";
  public static final String OPERATOR_CONTAINS = "contains";
  public static final String OPERATOR_NOT_CONTAINS = "notContains";
  public static final String OPERATOR_IN = "in";
  public static final String OPERATOR_NOT_IN = "notIn";

  /** Required. */
  private String key;
  /** One of the OPERATOR_* constants. */
  private String operator;
  /** A scalar for equal/contains operators, or a list for in/notIn. */
  private Object value;
}
