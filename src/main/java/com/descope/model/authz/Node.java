package com.descope.model.authz;

import com.descope.enums.NodeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonInclude(Include.NON_NULL)
public class Node {
  @JsonProperty("nType")
  NodeType nType;
  List<Node> children;
  NodeExpression expression;
}
