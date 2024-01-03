package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_CLONE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_UPDATE_NAME;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.project.NewProjectResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ProjectService;
import java.util.Map;

class ProjectServiceImpl extends ManagementsBase implements ProjectService {

  ProjectServiceImpl(Client client) {
    super(client);
  }

  @Override
  public void updateName(String name) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("name", name);
    apiProxy.post(getUri(MANAGEMENT_PROJECT_UPDATE_NAME), request, Void.class);
  }

  @Override
  public NewProjectResponse clone(String name, ProjectTag tag) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("name", name, "tag", tag);
    NewProjectResponse resp = apiProxy.post(getUri(MANAGEMENT_PROJECT_CLONE), request, NewProjectResponse.class);
    return resp;
  }
}
