package com.descope.model.fga;

import lombok.Data;

/**
 * Represents detailed information about a resource including metadata.
 */
@Data
public class FGAResourceDetails {
  
  /**
   * The resource ID.
   */
  private String resourceId;
  
  /**
   * The resource type.
   */
  private String resourceType;
  
  /**
   * The display name for the resource.
   */
  private String displayName;
  
  public FGAResourceDetails() {}
  
  public FGAResourceDetails(String resourceId, String resourceType, String displayName) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
    this.displayName = displayName;
  }
}
