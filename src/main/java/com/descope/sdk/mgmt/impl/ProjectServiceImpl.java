package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_CLONE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_EXPORT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_IMPORT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_UPDATE_NAME;
import static com.descope.utils.CollectionUtils.mapOf;

import java.util.Map;

import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.project.ExportProjectResponse;
import com.descope.model.project.NewProjectResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ProjectService;

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
  public NewProjectResponse cloneProject(String name, ProjectTag tag) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("name", name, "tag", tag);
    NewProjectResponse resp = apiProxy.post(getUri(MANAGEMENT_PROJECT_CLONE), request, NewProjectResponse.class);
    return resp;
  }

  @Override
  public ExportProjectResponse exportProject() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    ExportProjectResponse resp = apiProxy.post(getUri(MANAGEMENT_PROJECT_EXPORT), null, ExportProjectResponse.class);
    return resp;
  }

  @Override
  public void importProject(Map<String, Object> files) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("files", files);
    apiProxy.post(getUri(MANAGEMENT_PROJECT_IMPORT), request, Void.class);
  }
}
