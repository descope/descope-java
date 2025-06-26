package com.descope.model.fga;

import lombok.Data;

/**
 * Represents the result of an FGA check operation.
 */
@Data
public class FGACheckResult {
  
  /**
   * Whether the relation is allowed.
   */
  private boolean allowed;
  
  /**
   * The relation that was checked.
   */
  private FGARelation relation;
  
  /**
   * Additional information about the check result.
   */
  private FGACheckInfo info;
  
  public FGACheckResult() {}
  
  public FGACheckResult(boolean allowed, FGARelation relation, FGACheckInfo info) {
    this.allowed = allowed;
    this.relation = relation;
    this.info = info;
  }
}
