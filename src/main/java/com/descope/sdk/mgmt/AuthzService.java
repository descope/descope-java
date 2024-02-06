package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.authz.Modified;
import com.descope.model.authz.Namespace;
import com.descope.model.authz.Relation;
import com.descope.model.authz.RelationDefinition;
import com.descope.model.authz.RelationQuery;
import com.descope.model.authz.Schema;
import java.time.Instant;
import java.util.List;

/** Provides ReBAC authorization service APIs. */
public interface AuthzService {
  /**
   * Save (create or update) the given schema.
   * In case of update, will update only given namespaces and will not delete namespaces unless upgrade flag is true.
   * Schema name can be used for projects to track versioning.
   *
   * @param schema {@link Schema} to save.
   * @param upgrade Should we upgrade existing schema or ignore any namespace not provided.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void saveSchema(Schema schema, boolean upgrade) throws DescopeException;

  /**
   * Delete the schema for the project which will also delete all relations.
   *
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteSchema() throws DescopeException;

  /**
   * Load the schema for the project.
   *
   * @return {@link Schema}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Schema loadSchema() throws DescopeException;

  /**
   * Save (create or update) the given namespace.
   * Will not delete relation definitions not mentioned in the namespace.
   *
   * @param namespace {@link Namespace} to save.
   * @param oldName if we are changing the namespace name, what was the old name we are updating.
   * @param schemaName optional and used to track the current schema version.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void saveNamespace(Namespace namespace, String oldName, String schemaName) throws DescopeException;

  /**
   * Delete the given namespace.
   * Will also delete the relevant relations.
   *
   * @param name to delete.
   * @param schemaName optional and used to track the current schema version.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteNamespace(String name, String schemaName) throws DescopeException;

  /**
   * Save (create or update) the given relation definition.
   *
   * @param relationDefinition {@link RelationDefinition} to save.
   * @param namespace that it belongs to.
   * @param oldName if we are changing the relation definition name, what was the old name we are updating.
   * @param schemaName optional and used to track the current schema version.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void saveRelationDefinition(RelationDefinition relationDefinition, String namespace, String oldName,
      String schemaName) throws DescopeException;

  /**
   * Delete the given relation definition.
   * Will also delete the relevant relations.
   *
   * @param name to delete.
   * @param namespace it belongs to.
   * @param schemaName optional and used to track the current schema version.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteRelationDefinition(String name, String namespace, String schemaName) throws DescopeException;

  /**
   * Create the given relations.
   *
   * @param relations {@link List} of {@link Relation} to create.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void createRelations(List<Relation> relations) throws DescopeException;

  /**
   * Delete the given relations.
   *
   * @param relations {@link List} of {@link Relation} to delete.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteRelations(List<Relation> relations) throws DescopeException;

  /**
   * Delete the given relations.
   *
   * @param resources {@link List} of resources to delete.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteRelationsForResources(List<String> resources) throws DescopeException;

  /**
   * Query relations to see what relations exists.
   *
   * @param relationQueries {@link List} of {@link RelationQuery} to check.
   * @return {@link List} of {@link RelationQuery} responses with the boolean flag indicating if relation exists
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<RelationQuery> hasRelations(List<RelationQuery> relationQueries) throws DescopeException;

  /**
   * List all the users that have the given relation definition to the given resource.
   *
   * @param resource The resource we are checking
   * @param relationDefinition The relation definition we are querying
   * @param namespace The namespace for the relation definition
   * @return {@link List} of users who have the given relation definition
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<String> whoCanAccess(String resource, String relationDefinition, String namespace) throws DescopeException;

  /**
   * Return the list of all defined relations (not recursive) on the given resource.
   *
   * @param resource The resource we are checking
   * @return {@link List} of {@link Relation} that exist for the given resource
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<Relation> resourceRelations(String resource) throws DescopeException;

  /**
   * Return the list of all defined relations (not recursive) for the given targets.
   *
   * @param targets {@link List} of targets we want to check
   * @return {@link List} of {@link Relation} that exist for the given targets
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<Relation> targetsRelations(List<String> targets) throws DescopeException;

  /**
   * Return the list of all relations for the given target including derived relations from the schema tree.
   *
   * @param target The target to check relations for
   * @return {@link List} of {@link Relation} that exist for the given target
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<Relation> whatCanTargetAccess(String target) throws DescopeException;

  /**
   * Return list of targets and resources changed since the given date.
   * Should be used to invalidate local caches.
   *
   * @param since return the changes since this instant
   * @return {@link Modified} including resources and targets changed
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  Modified getModified(Instant since) throws DescopeException;
}
