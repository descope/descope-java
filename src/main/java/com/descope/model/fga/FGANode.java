package com.descope.model.fga;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("checkstyle:MemberName")
public class FGANode {
  // One of child, union, intersect or sub
  @JsonProperty("nType")
  private String nType;
  private List<FGANode> children;
  private FGANodeExpression expression;
}
