package com.descope.model.fga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FGANodeExpression {
  // One of self, targetSet, relationLeft or relationRight
  private String neType;
  private String relationDefinition;
  private String relationDefinitionNamespace;
  private String targetRelationDefinition;
  private String targetRelationDefinitionNamespace;
}
