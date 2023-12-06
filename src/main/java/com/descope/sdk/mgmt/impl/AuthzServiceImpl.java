package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_NS_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_NS_SAVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RD_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RD_SAVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_DELETE_RESOURCES;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_HAS_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_RESOURCE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_TARGETS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_TARGET_ALL;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_RE_WHO;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_SCHEMA_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_SCHEMA_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTHZ_SCHEMA_SAVE;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.authz.HasRelationsResponse;
import com.descope.model.authz.LoadSchemaResponse;
import com.descope.model.authz.Namespace;
import com.descope.model.authz.Relation;
import com.descope.model.authz.RelationDefinition;
import com.descope.model.authz.RelationQuery;
import com.descope.model.authz.RelationsResponse;
import com.descope.model.authz.Schema;
import com.descope.model.authz.WhoCanAccessResponse;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.AuthzService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class AuthzServiceImpl extends ManagementsBase implements AuthzService {

  AuthzServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public void saveSchema(Schema schema, boolean upgrade) throws DescopeException {
    if (schema == null) {
      throw ServerCommonException.invalidArgument("schema");
    }
    if (schema.getNamespaces() == null || schema.getNamespaces().isEmpty()) {
      throw ServerCommonException.invalidArgument("schema");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("schema", schema, "upgrade", upgrade);
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_SCHEMA_SAVE), request, Void.class);
  }

  @Override
  public void deleteSchema() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = Collections.emptyMap();
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_SCHEMA_DELETE), request, Void.class);
  }

  @Override
  public Schema loadSchema() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = Collections.emptyMap();
    LoadSchemaResponse resp = apiProxy.post(getUri(MANAGEMENT_AUTHZ_SCHEMA_LOAD), request, LoadSchemaResponse.class);
    return resp.getSchema();
  }

  @Override
  public void saveNamespace(Namespace namespace, String oldName, String schemaName) throws DescopeException {
    if (namespace == null || StringUtils.isBlank(namespace.getName()) || namespace.getRelationDefinitions() == null
        || namespace.getRelationDefinitions().isEmpty()) {
      throw ServerCommonException.invalidArgument("namespace");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = new HashMap<>(mapOf("namespace", namespace));
    if (!StringUtils.isBlank(oldName)) {
      request.put("oldName", oldName);
    }
    if (!StringUtils.isBlank(schemaName)) {
      request.put("schemaName", schemaName);
    }
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_NS_SAVE), request, Void.class);
  }

  @Override
  public void deleteNamespace(String name, String schemaName) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = new HashMap<>(mapOf("name", name));
    if (!StringUtils.isBlank(schemaName)) {
      request.put("schemaName", schemaName);
    }
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_NS_DELETE), request, Void.class);
  }

  @Override
  public void saveRelationDefinition(RelationDefinition relationDefinition, String namespace, String oldName,
      String schemaName) throws DescopeException {
    if (relationDefinition == null || StringUtils.isBlank(relationDefinition.getName())) {
      throw ServerCommonException.invalidArgument("relationDefinition");
    }
    if (StringUtils.isBlank(namespace)) {
      throw ServerCommonException.invalidArgument("namespace");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request =
        new HashMap<>(mapOf("relationDefinition", relationDefinition, "namespace", namespace));
    if (!StringUtils.isBlank(oldName)) {
      request.put("oldName", oldName);
    }
    if (!StringUtils.isBlank(schemaName)) {
      request.put("schemaName", schemaName);
    }
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_RD_SAVE), request, Void.class);
  }

  @Override
  public void deleteRelationDefinition(String name, String namespace, String schemaName) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    if (StringUtils.isBlank(namespace)) {
      throw ServerCommonException.invalidArgument("namespace");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = new HashMap<>(mapOf("name", name, "namespace", namespace));
    if (!StringUtils.isBlank(schemaName)) {
      request.put("schemaName", schemaName);
    }
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_RD_DELETE), request, Void.class);
  }

  @Override
  public void createRelations(List<Relation> relations) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("relations", relations);
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_CREATE), request, Void.class);
  }

  @Override
  public void deleteRelations(List<Relation> relations) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("relations", relations);
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_DELETE), request, Void.class);
  }

  @Override
  public void deleteRelationsForResources(List<String> resources) throws DescopeException {
    if (resources == null || resources.isEmpty()) {
      throw ServerCommonException.invalidArgument("resources");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("resources", resources);
    apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_DELETE_RESOURCES), request, Void.class);
  }

  @Override
  public List<RelationQuery> hasRelations(List<RelationQuery> relationQueries) throws DescopeException {
    if (relationQueries == null || relationQueries.isEmpty()) {
      throw ServerCommonException.invalidArgument("relationQueries");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("relationQueries", relationQueries);
    HasRelationsResponse resp =
        apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_HAS_RELATIONS), request, HasRelationsResponse.class);
    return resp.getRelationQueries();
  }

  @Override
  public List<String> whoCanAccess(String resource, String relationDefinition, String namespace)
      throws DescopeException {
    if (StringUtils.isBlank(resource)) {
      throw ServerCommonException.invalidArgument("resource");
    }
    if (StringUtils.isBlank(relationDefinition)) {
      throw ServerCommonException.invalidArgument("relationDefinition");
    }
    if (StringUtils.isBlank(namespace)) {
      throw ServerCommonException.invalidArgument("namespace");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request =
        mapOf("resource", resource, "relationDefinition", relationDefinition, "namespace", namespace);
    WhoCanAccessResponse resp = apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_WHO), request, WhoCanAccessResponse.class);
    return resp.getTargets();
  }

  @Override
  public List<Relation> resourceRelations(String resource) throws DescopeException {
    if (StringUtils.isBlank(resource)) {
      throw ServerCommonException.invalidArgument("resource");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("resource", resource);
    RelationsResponse resp = apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_RESOURCE), request, RelationsResponse.class);
    return resp.getRelations();
  }

  @Override
  public List<Relation> targetsRelations(List<String> targets) throws DescopeException {
    if (targets == null || targets.isEmpty()) {
      throw ServerCommonException.invalidArgument("targets");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("targets", targets);
    RelationsResponse resp = apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_TARGETS), request, RelationsResponse.class);
    return resp.getRelations();
  }

  @Override
  public List<Relation> whatCanTargetAccess(String target) throws DescopeException {
    if (StringUtils.isBlank(target)) {
      throw ServerCommonException.invalidArgument("user");
    }
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("target", target);
    RelationsResponse resp = apiProxy.post(getUri(MANAGEMENT_AUTHZ_RE_TARGET_ALL), request, RelationsResponse.class);
    return resp.getRelations();
  }
}
