package com.descope.model.fga;

import lombok.Data;

/**
 * Additional information about an FGA check result.
 */
@Data
public class FGACheckInfo {
  
  /**
   * A relation is considered "direct" if, based solely on the schema, its "allowed" state can only be
   * changed by creating or deleting relations involving its resource, its target, or both (including itself).
   */
  private boolean direct;
  
  public FGACheckInfo() {}
  
  public FGACheckInfo(boolean direct) {
    this.direct = direct;
  }
}
