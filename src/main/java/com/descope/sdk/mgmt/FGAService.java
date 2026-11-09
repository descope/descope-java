package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.fga.FGACheckRequest;
import com.descope.model.fga.FGACheckResponse;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGAMappableResources;
import com.descope.model.fga.FGAMappableResourcesQuery;
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
   * Checks if the given FGA relations are satisfied, with per-tuple context and optional
   * check paths and evaluated conditions in the results.
   *
   * <p>Set either {@code tuples} or {@code checkTuples} on the request, not both. A check tuple's
   * context overrides the request-level context.
   *
   * @param request the relations to check and the check options
   * @return the check results and the schema version the condition IDs refer to
   * @throws DescopeException if the operation fails
   */
  FGACheckResponse checkWithRequest(FGACheckRequest request) throws DescopeException;

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

  /**
   * Loads the mappable schema for a tenant (only listing the relation definitions of each
   * namespace), along with a list of mappable resources.
   *
   * @param tenantId the tenant to load the mappable schema for
   * @param resourcesLimit maximum number of resources to return per type, may be null
   * @return the mappable schema and resources
   * @throws DescopeException if the operation fails
   */
  FGAMappableSchema loadMappableSchema(String tenantId, Long resourcesLimit) throws DescopeException;

  /**
   * Searches for mappable resources of a tenant.
   *
   * @param tenantId the tenant to search in
   * @param resourcesQueries the resource types and queries to search for, may be null; each query
   *     is at most 100 characters
   * @param resourcesLimit maximum number of resources to return per type, may be null
   * @return the matching mappable resources, grouped by type
   * @throws DescopeException if the operation fails
   */
  List<FGAMappableResources> searchMappableResources(String tenantId,
      List<FGAMappableResourcesQuery> resourcesQueries, Long resourcesLimit) throws DescopeException;
}
