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
  // True for conditions expanded from built-in constraint declarations
  private boolean builtin;
  // Base64 of the serialized type-checked CEL program (exprpb.CheckedExpr)
  private String checkedExpr;
  // Short ID referenced by FGACheckInfo, renumbered on schema change
  private Integer id;

  public FGACondition(String name, List<FGAConditionParam> params, String expression) {
    this.name = name;
    this.params = params;
    this.expression = expression;
  }
}
