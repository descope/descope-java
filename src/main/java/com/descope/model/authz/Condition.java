package com.descope.model.authz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A named CEL condition defined in the schema. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Condition {
  /** Condition name, up to 100 characters. */
  String name;
  List<ConditionParam> params;
  /** CEL expression, up to 10000 characters. */
  String expression;
  /** True for conditions auto-expanded from built-in constraint declarations. */
  Boolean builtin;
  /** Base64 of the serialized type-checked CEL program (exprpb.CheckedExpr), kept as text so it parses leniently. */
  String checkedExpr;
  /** Short ID, renumbered on schema change. */
  Integer id;
}
