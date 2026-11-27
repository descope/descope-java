package com.descope.model.sso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReBACGroupsMappingRelation {
  /** Resource identifier of the relation. */
  private String resource;
  /** The name of the relation definition. */
  private String relationDefinition;
  /** The namespace of the relation definition. */
  private String namespace;
}
