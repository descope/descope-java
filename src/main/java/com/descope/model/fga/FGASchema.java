package com.descope.model.fga;

import lombok.Data;

/**
 * Represents a Fine-Grained Authorization (FGA) schema in DSL format.
 */
@Data
public class FGASchema {
  
  /**
   * The FGA schema in DSL format (e.g., AuthZ 1.0 DSL).
   */
  private String dsl;
  
  public FGASchema() {}
  
  public FGASchema(String dsl) {
    this.dsl = dsl;
  }
}
