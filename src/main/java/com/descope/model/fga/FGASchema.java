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
public class FGASchema {
  private String dsl;
  private List<FGACondition> conditions;
  private String version;
  // Returned by loadSchema, ignored by saveSchema
  private String name;
  // Returned by loadSchema, ignored by saveSchema
  private List<FGANamespace> namespaces;

  public FGASchema(String dsl) {
    this.dsl = dsl;
  }

  public FGASchema(String dsl, List<FGACondition> conditions, String version) {
    this.dsl = dsl;
    this.conditions = conditions;
    this.version = version;
  }
}
