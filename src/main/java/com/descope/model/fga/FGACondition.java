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
}
