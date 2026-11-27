package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CHECK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CREATE_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_DELETE_ALL_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_DELETE_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_LOAD_SCHEMA;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_MAPPABLE_RESOURCES;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_MAPPABLE_SCHEMA;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_RESOURCES_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_RESOURCES_SAVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_SAVE_SCHEMA;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_SCHEMA_DRY_RUN;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGACheckInfo;
import com.descope.model.fga.FGACheckOptions;
import com.descope.model.fga.FGACheckResponse;
import com.descope.model.fga.FGACheckResponse.FGACheckResponseTuple;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGACheckTuple;
import com.descope.model.fga.FGALoadSchemaResponse;
import com.descope.model.fga.FGAMappableResourceQuery;
import com.descope.model.fga.FGAMappableResources;
import com.descope.model.fga.FGAMappableSchema;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.fga.FGASchemaDryRunResponse;
import com.descope.model.fga.FGASearchMappableResourcesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.FGAService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class FGAServiceImpl extends ManagementsBase implements FGAService {

  FGAServiceImpl(Client client) {
    super(client);
  }

  @Override
  public void saveSchema(FGASchema schema) throws DescopeException {
    if (schema == null || StringUtils.isBlank(schema.getDsl())) {
      throw ServerCommonException.invalidArgument("FGA schema DSL");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("dsl", schema.getDsl());

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getFgaUri(MANAGEMENT_FGA_SAVE_SCHEMA), requestBody, Void.class);
  }

  @Override
  public FGASchemaDryRunResponse dryRunSchema(FGASchema schema) throws DescopeException {
    if (schema == null || StringUtils.isBlank(schema.getDsl())) {
      throw ServerCommonException.invalidArgument("FGA schema DSL");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("dsl", schema.getDsl());

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_FGA_SCHEMA_DRY_RUN), requestBody, FGASchemaDryRunResponse.class);
  }

  @Override
  public FGASchema loadSchema() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    FGALoadSchemaResponse response = apiProxy.get(getUri(MANAGEMENT_FGA_LOAD_SCHEMA), FGALoadSchemaResponse.class);

    FGASchema schema = new FGASchema();
    schema.setConditions(new ArrayList<>());
    if (response == null) {
      return schema;
    }
    schema.setDsl(response.getDsl());
    schema.setVersion(response.getVersion());
    if (response.getSchema() != null && response.getSchema().getConditions() != null) {
      schema.setConditions(response.getSchema().getConditions());
    }
    return schema;
  }

  @Override
  public void createRelations(List<FGARelation> relations) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tuples", relations);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getFgaUri(MANAGEMENT_FGA_CREATE_RELATIONS), requestBody, Void.class);
  }

  @Override
  public void deleteRelations(List<FGARelation> relations) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tuples", relations);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getFgaUri(MANAGEMENT_FGA_DELETE_RELATIONS), requestBody, Void.class);
  }

  @Override
  public List<FGACheckResult> check(List<FGARelation> relations) throws DescopeException {
    return check(relations, null);
  }

  @Override
  public List<FGACheckResult> check(List<FGARelation> relations, Map<String, Object> context)
      throws DescopeException {
    return check(relations, context, null);
  }

  @Override
  public List<FGACheckResult> check(List<FGARelation> relations, Map<String, Object> context,
      FGACheckOptions options) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tuples", relations);
    return postCheck(requestBody, context, options);
  }

  @Override
  public List<FGACheckResult> checkTuples(List<FGACheckTuple> checkTuples, Map<String, Object> context,
      FGACheckOptions options) throws DescopeException {
    if (checkTuples == null || checkTuples.isEmpty()) {
      throw ServerCommonException.invalidArgument("check tuples list");
    }
    for (FGACheckTuple checkTuple : checkTuples) {
      if (checkTuple == null || checkTuple.getTuple() == null) {
        throw ServerCommonException.invalidArgument("check tuple");
      }
    }

    List<Map<String, Object>> tuples = new ArrayList<>();
    for (FGACheckTuple checkTuple : checkTuples) {
      Map<String, Object> tuple = new HashMap<>();
      tuple.put("tuple", checkTuple.getTuple());
      if (checkTuple.getContext() != null && !checkTuple.getContext().isEmpty()) {
        tuple.put("context", checkTuple.getContext());
      }
      tuples.add(tuple);
    }
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("checkTuples", tuples);
    return postCheck(requestBody, context, options);
  }

  private List<FGACheckResult> postCheck(Map<String, Object> requestBody, Map<String, Object> context,
      FGACheckOptions options) {
    if (context != null && !context.isEmpty()) {
      requestBody.put("context", context);
    }
    if (options != null && options.getComputePaths() != null) {
      requestBody.put("computePaths", options.getComputePaths());
    }
    if (options != null && options.getListConditions() != null) {
      requestBody.put("listConditions", options.getListConditions());
    }

    ApiProxy apiProxy = getApiProxy();
    FGACheckResponse response = apiProxy.post(getFgaUri(MANAGEMENT_FGA_CHECK), requestBody, FGACheckResponse.class);

    List<FGACheckResult> results = new ArrayList<>();
    if (response == null || response.getTuples() == null) {
      return results;
    }
    for (FGACheckResponseTuple tuple : response.getTuples()) {
      FGACheckInfo info = tuple.getInfo() == null ? new FGACheckInfo() : tuple.getInfo();
      if (info.getMissingContext() == null) {
        info.setMissingContext(new ArrayList<>());
      }
      // Version is per response, so every result carries it
      info.setSchemaVersion(response.getSchemaVersion());
      results.add(new FGACheckResult(tuple.isAllowed(), tuple.getTuple(), info));
    }
    return results;
  }

  @Override
  public void deleteAllRelations() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(getUri(MANAGEMENT_FGA_DELETE_ALL_RELATIONS), null, Void.class);
  }

  @Override
  public List<FGAMappableResources> searchMappableResources(String tenantId,
      List<FGAMappableResourceQuery> resourcesQueries, Long resourcesLimit) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenantId", tenantId);
    if (resourcesQueries != null) {
      requestBody.put("resourcesQueries", resourcesQueries);
    }
    if (resourcesLimit != null && resourcesLimit > 0) {
      requestBody.put("resourcesLimit", String.valueOf(resourcesLimit));
    }

    ApiProxy apiProxy = getApiProxy();
    FGASearchMappableResourcesResponse response = apiProxy.post(getUri(MANAGEMENT_FGA_MAPPABLE_RESOURCES),
        requestBody, FGASearchMappableResourcesResponse.class);
    if (response == null || response.getMappableResources() == null) {
      return new ArrayList<>();
    }
    return response.getMappableResources();
  }

  @Override
  public FGAMappableSchema loadMappableSchema(String tenantId, Long resourcesLimit) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, String> params = new HashMap<>();
    params.put("tenantId", tenantId);
    if (resourcesLimit != null && resourcesLimit > 0) {
      params.put("resourcesLimit", String.valueOf(resourcesLimit));
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_FGA_MAPPABLE_SCHEMA, params), FGAMappableSchema.class);
  }

  @Override
  public List<FGAResourceDetails> loadResourcesDetails(List<FGAResourceIdentifier> resourceIdentifiers) 
      throws DescopeException {
    if (resourceIdentifiers == null || resourceIdentifiers.isEmpty()) {
      throw ServerCommonException.invalidArgument("resource identifiers list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("resourceIdentifiers", resourceIdentifiers);

    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> response = apiProxy.postAndGetArray(getUri(MANAGEMENT_FGA_RESOURCES_LOAD), 
        requestBody, new TypeReference<Map<String, Object>>() {});

    if (response.containsKey("resourcesDetails")) {
      // Convert the response to FGAResourceDetails objects
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> details = (List<Map<String, Object>>) response.get("resourcesDetails");
      List<FGAResourceDetails> results = new ArrayList<>();
      for (Map<String, Object> detail : details) {
        FGAResourceDetails resourceDetail = new FGAResourceDetails();
        if (detail.containsKey("resourceId")) {
          resourceDetail.setResourceId((String) detail.get("resourceId"));
        }
        if (detail.containsKey("resourceType")) {
          resourceDetail.setResourceType((String) detail.get("resourceType"));
        }
        if (detail.containsKey("displayName")) {
          resourceDetail.setDisplayName((String) detail.get("displayName"));
        }
        results.add(resourceDetail);
      }
      return results;
    }
    return new ArrayList<>();
  }

  @Override
  public void saveResourcesDetails(List<FGAResourceDetails> resourcesDetails) throws DescopeException {
    if (resourcesDetails == null || resourcesDetails.isEmpty()) {
      throw ServerCommonException.invalidArgument("resources details list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("resourcesDetails", resourcesDetails);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_FGA_RESOURCES_SAVE), requestBody, Void.class);
  }
}
