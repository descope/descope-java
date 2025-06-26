package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import java.util.List;

/**
 * Provides functions for managing Fine-Grained Authorization (FGA) in a project.
 * FGA allows for creating and managing schemas and relations using a Zanzibar-like model.
 */
public interface FGAService {

  /**
   * Creates or updates an FGA schema for the project.
   * The schema is provided in the AuthZ 1.0 DSL format.
   *
   * @param schema the FGA schema containing the DSL definition
   * @throws DescopeException if the operation fails
   */
  void saveSchema(FGASchema schema) throws DescopeException;

  /**
   * Loads the current FGA schema for the project.
   *
   * @return the current FGA schema
   * @throws DescopeException if the operation fails
   */
  FGASchema loadSchema() throws DescopeException;

  /**
   * Creates new FGA relations (tuples) based on the existing schema.
   *
   * @param relations list of relations to create
   * @throws DescopeException if the operation fails
   */
  void createRelations(List<FGARelation> relations) throws DescopeException;

  /**
   * Deletes existing FGA relations (tuples).
   *
   * @param relations list of relations to delete
   * @throws DescopeException if the operation fails
   */
  void deleteRelations(List<FGARelation> relations) throws DescopeException;

  /**
   * Checks if the given FGA relations are satisfied.
   * This is a read-only operation that validates whether relations exist.
   *
   * @param relations list of relations to check
   * @return list of check results indicating whether each relation is allowed
   * @throws DescopeException if the operation fails
   */
  List<FGACheckResult> check(List<FGARelation> relations) throws DescopeException;

  /**
   * Loads detailed information for the given resource identifiers.
   *
   * @param resourceIdentifiers list of resource identifiers to load details for
   * @return list of resource details
   * @throws DescopeException if the operation fails
   */
  List<FGAResourceDetails> loadResourcesDetails(List<FGAResourceIdentifier> resourceIdentifiers) throws DescopeException;

  /**
   * Saves detailed information for the given resources.
   *
   * @param resourcesDetails list of resource details to save
   * @throws DescopeException if the operation fails
   */
  void saveResourcesDetails(List<FGAResourceDetails> resourcesDetails) throws DescopeException;
}
