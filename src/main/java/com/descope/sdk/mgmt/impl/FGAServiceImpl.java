package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CHECK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CREATE_RELATIONS;
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
import com.descope.model.fga.FGACheckRequest;
import com.descope.model.fga.FGACheckResponse;
import com.descope.model.fga.FGACheckResponse.FGACheckResponseTuple;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGALoadSchemaResponse;
import com.descope.model.fga.FGAMappableResources;
import com.descope.model.fga.FGAMappableResourcesQuery;
import com.descope.model.fga.FGAMappableResourcesResponse;
import com.descope.model.fga.FGAMappableSchema;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.fga.FGASchemaDryRunResponse;
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
    if (response.getSchema() != null) {
      if (response.getSchema().getConditions() != null) {
        schema.setConditions(response.getSchema().getConditions());
      }
      schema.setName(response.getSchema().getName());
      schema.setNamespaces(response.getSchema().getNamespaces());
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
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tuples", relations);
    if (context != null && !context.isEmpty()) {
      requestBody.put("context", context);
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
      results.add(new FGACheckResult(tuple.isAllowed(), tuple.getTuple(), info));
    }
    return results;
  }

  @Override
  public FGACheckResponse checkWithRequest(FGACheckRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("check request");
    }
    boolean hasTuples = request.getTuples() != null && !request.getTuples().isEmpty();
    boolean hasCheckTuples = request.getCheckTuples() != null && !request.getCheckTuples().isEmpty();
    if (hasTuples == hasCheckTuples) {
      throw ServerCommonException.invalidArgument("tuples or checkTuples");
    }

    Map<String, Object> requestBody = new HashMap<>();
    if (hasTuples) {
      requestBody.put("tuples", request.getTuples());
    } else {
      requestBody.put("checkTuples", request.getCheckTuples());
    }
    if (request.getContext() != null && !request.getContext().isEmpty()) {
      requestBody.put("context", request.getContext());
    }
    if (request.getComputePaths() != null) {
      requestBody.put("computePaths", request.getComputePaths());
    }
    if (request.getListConditions() != null) {
      requestBody.put("listConditions", request.getListConditions());
    }

    ApiProxy apiProxy = getApiProxy();
    FGACheckResponse response = apiProxy.post(getFgaUri(MANAGEMENT_FGA_CHECK), requestBody, FGACheckResponse.class);
    if (response == null) {
      response = new FGACheckResponse();
    }
    if (response.getTuples() == null) {
      response.setTuples(new ArrayList<>());
    }
    for (FGACheckResponseTuple tuple : response.getTuples()) {
      if (tuple.getInfo() == null) {
        tuple.setInfo(new FGACheckInfo());
      }
      if (tuple.getInfo().getMissingContext() == null) {
        tuple.getInfo().setMissingContext(new ArrayList<>());
      }
    }
    return response;
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

  @Override
  public FGAMappableSchema loadMappableSchema(String tenantId, Long resourcesLimit) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, String> params = new HashMap<>();
    params.put("tenantId", tenantId);
    if (resourcesLimit != null) {
      params.put("resourcesLimit", resourcesLimit.toString());
    }

    ApiProxy apiProxy = getApiProxy();
    FGAMappableSchema response =
        apiProxy.get(getQueryParamUri(MANAGEMENT_FGA_MAPPABLE_SCHEMA, params), FGAMappableSchema.class);
    return response == null ? new FGAMappableSchema() : response;
  }

  @Override
  public List<FGAMappableResources> searchMappableResources(String tenantId,
      List<FGAMappableResourcesQuery> resourcesQueries, Long resourcesLimit) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tenantId", tenantId);
    if (resourcesQueries != null) {
      requestBody.put("resourcesQueries", resourcesQueries);
    }
    if (resourcesLimit != null) {
      requestBody.put("resourcesLimit", resourcesLimit.toString());
    }

    ApiProxy apiProxy = getApiProxy();
    FGAMappableResourcesResponse response = apiProxy.post(getUri(MANAGEMENT_FGA_MAPPABLE_RESOURCES), requestBody,
        FGAMappableResourcesResponse.class);
    if (response == null || response.getMappableResources() == null) {
      return new ArrayList<>();
    }
    return response.getMappableResources();
  }
}
