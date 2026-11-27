package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.fga.FGACheckOptions;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGACheckTuple;
import com.descope.model.fga.FGAMappableResourceQuery;
import com.descope.model.fga.FGAMappableResources;
import com.descope.model.fga.FGAMappableSchema;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.fga.FGASchemaDryRunResponse;
import java.util.List;
import java.util.Map;

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
   * Validates an FGA schema without saving it, and reports what saving it would delete
   * from the current schema.
   *
   * @param schema the FGA schema containing the DSL definition
   * @return a preview of the relations and types that would be deleted
   * @throws DescopeException if the operation fails
   */
  FGASchemaDryRunResponse dryRunSchema(FGASchema schema) throws DescopeException;

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
   * Checks if the given FGA relations are satisfied, evaluating schema conditions against
   * the given context.
   *
   * <p>Context keys become variables available to the CEL conditions defined in the schema,
   * on top of any attributes the backend already holds. Values must be JSON serializable.
   *
   * @param relations list of relations to check
   * @param context extra context for condition evaluation, may be null or empty
   * @return list of check results indicating whether each relation is allowed
   * @throws DescopeException if the operation fails
   */
  List<FGACheckResult> check(List<FGARelation> relations, Map<String, Object> context) throws DescopeException;

  /**
   * Checks if the given FGA relations are satisfied, with options that add details to each result.
   *
   * @param relations list of relations to check
   * @param context extra context for condition evaluation, may be null or empty
   * @param options optional flags, may be null: {@code computePaths} sets each result's
   *     {@link com.descope.model.fga.FGACheckInfo#getPath() path}, {@code listConditions} sets its
   *     {@code trueConditions} and {@code falseConditions}
   * @return list of check results indicating whether each relation is allowed
   * @throws DescopeException if the operation fails
   */
  List<FGACheckResult> check(List<FGARelation> relations, Map<String, Object> context, FGACheckOptions options)
      throws DescopeException;

  /**
   * Checks if the given FGA relations are satisfied, each with its own condition context.
   *
   * @param checkTuples list of relations to check, each with an optional context that overrides
   *     the request-level context
   * @param context extra context for condition evaluation of tuples without their own, may be null or empty
   * @param options optional flags, may be null, as in
   *     {@link #check(List, Map, FGACheckOptions)}
   * @return list of check results indicating whether each relation is allowed
   * @throws DescopeException if the operation fails
   */
  List<FGACheckResult> checkTuples(List<FGACheckTuple> checkTuples, Map<String, Object> context,
      FGACheckOptions options) throws DescopeException;

  /**
   * Deletes all FGA relations (tuples) in the project. This cannot be undone.
   *
   * @throws DescopeException if the operation fails
   */
  void deleteAllRelations() throws DescopeException;

  /**
   * Searches the resources a tenant can map, by resource type.
   *
   * @param tenantId the tenant ID, required
   * @param resourcesQueries the resource types to search, each with optional search queries
   * @param resourcesLimit the maximum number of resources to return per type, may be null for no limit
   * @return the matching resources, grouped by type
   * @throws DescopeException if the operation fails
   */
  List<FGAMappableResources> searchMappableResources(String tenantId, List<FGAMappableResourceQuery> resourcesQueries,
      Long resourcesLimit) throws DescopeException;

  /**
   * Loads the FGA schema a tenant can map, together with its mappable resources.
   *
   * @param tenantId the tenant ID, required
   * @param resourcesLimit the maximum number of resources to return per type, may be null for no limit
   * @return the mappable schema and resources
   * @throws DescopeException if the operation fails
   */
  FGAMappableSchema loadMappableSchema(String tenantId, Long resourcesLimit) throws DescopeException;

  /**
   * Loads detailed information for the given resource identifiers.
   *
   * @param resourceIdentifiers list of resource identifiers to load details for
   * @return list of resource details
   * @throws DescopeException if the operation fails
   */
  List<FGAResourceDetails> loadResourcesDetails(List<FGAResourceIdentifier> resourceIdentifiers)
      throws DescopeException;

  /**
   * Saves detailed information for the given resources.
   *
   * @param resourcesDetails list of resource details to save
   * @throws DescopeException if the operation fails
   */
  void saveResourcesDetails(List<FGAResourceDetails> resourcesDetails) throws DescopeException;
}
