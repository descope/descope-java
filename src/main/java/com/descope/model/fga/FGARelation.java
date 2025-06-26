package com.descope.model.fga;

import lombok.Data;

/**
 * Represents a Fine-Grained Authorization (FGA) relation (tuple).
 * This defines a relation between a resource and a target.
 */
@Data
public class FGARelation {    /**   * The resource ID (e.g., "doc1", "folder2").   */  private String resource;    /**
   * The type of the resource (e.g., "document", "folder").
   */
  private String resourceType;
  
  /**
   * The relation name (e.g., "owner", "editor", "viewer").
   */
  private String relation;
  
  /**
   * The target ID (e.g., "user1", "group1").
   */
  private String target;
  
  /**
   * The type of the target (e.g., "user", "group").
   */
  private String targetType;
  
  public FGARelation() {}
  
  public FGARelation(String resource, String resourceType, String relation, String target, String targetType) {
    this.resource = resource;
    this.resourceType = resourceType;
    this.relation = relation;
    this.target = target;
    this.targetType = targetType;
  }
}
