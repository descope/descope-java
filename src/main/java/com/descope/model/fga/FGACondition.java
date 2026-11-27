package com.descope.model.fga;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGACondition {
  private String name;
  private List<FGAConditionParam> params;
  private String expression;
  // Auto-expanded from a built-in constraint declaration
  private boolean builtin;
  // Serialized CEL CheckedExpr, base64 encoded
  private String checkedExpr;
  // Short ID, renumbered on schema change
  private Integer id;

  public FGACondition(String name, List<FGAConditionParam> params, String expression) {
    this.name = name;
    this.params = params;
    this.expression = expression;
  }
}
