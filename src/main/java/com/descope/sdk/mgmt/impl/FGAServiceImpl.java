package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CHECK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CREATE_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_DELETE_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_LOAD_SCHEMA;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_RESOURCES_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_RESOURCES_SAVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_SAVE_SCHEMA;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
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
    apiProxy.post(getUri(MANAGEMENT_FGA_SAVE_SCHEMA), requestBody, Void.class);
  }

  @Override
  public FGASchema loadSchema() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> response = apiProxy.getArray(getUri(MANAGEMENT_FGA_LOAD_SCHEMA), 
        new TypeReference<Map<String, Object>>() {});

    FGASchema schema = new FGASchema();
    if (response.containsKey("dsl")) {
      schema.setDsl((String) response.get("dsl"));
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
    apiProxy.post(getUri(MANAGEMENT_FGA_CREATE_RELATIONS), requestBody, Void.class);
  }

  @Override
  public void deleteRelations(List<FGARelation> relations) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tuples", relations);

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_FGA_DELETE_RELATIONS), requestBody, Void.class);
  }

  @Override
  public List<FGACheckResult> check(List<FGARelation> relations) throws DescopeException {
    if (relations == null || relations.isEmpty()) {
      throw ServerCommonException.invalidArgument("relations list");
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("tuples", relations);

    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> response = apiProxy.postAndGetArray(getUri(MANAGEMENT_FGA_CHECK), 
        requestBody, new TypeReference<Map<String, Object>>() {});

    if (response.containsKey("tuples")) {
      // Convert the response tuples to FGACheckResult objects
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> tuples = (List<Map<String, Object>>) response.get("tuples");
      List<FGACheckResult> results = new ArrayList<>();
      for (Map<String, Object> tuple : tuples) {
        FGACheckResult result = new FGACheckResult();
        if (tuple.containsKey("allowed")) {
          result.setAllowed((Boolean) tuple.get("allowed"));
        }
        results.add(result);
      }
      return results;
    }
    return new ArrayList<>();
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
