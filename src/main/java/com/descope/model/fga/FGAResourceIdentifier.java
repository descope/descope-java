package com.descope.model.fga;

import lombok.Data;

/**
 * Identifies a resource by its ID and type.
 */
@Data
public class FGAResourceIdentifier {
  
  /**
   * The resource ID.
   */
  private String resourceId;
  
  /**
   * The resource type.
   */
  private String resourceType;
  
  public FGAResourceIdentifier() {}
  
  public FGAResourceIdentifier(String resourceId, String resourceType) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
  }
}
