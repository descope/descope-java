package com.descope.model.authz;

import com.descope.enums.NodeExpressionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class NodeExpression {
  NodeExpressionType neType;
  String relationDefinition;
  String relationDefinitionNamespace;
  String targetRelationDefinition;
  String targetRelationDefinitionNamespace;
}
