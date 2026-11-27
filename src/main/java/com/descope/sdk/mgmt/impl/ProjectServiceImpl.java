package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_CLONE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_EXPORT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_IMPORT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_LIST;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_SNAPSHOT_EXPORT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_SNAPSHOT_IMPORT;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_SNAPSHOT_VALIDATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_UPDATE_NAME;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_UPDATE_TAGS;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.enums.ProjectEnvironment;
import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.project.ExportProjectResponse;
import com.descope.model.project.ExportSnapshotRequest;
import com.descope.model.project.ExportSnapshotResponse;
import com.descope.model.project.ImportSnapshotRequest;
import com.descope.model.project.NewProjectResponse;
import com.descope.model.project.Project;
import com.descope.model.project.ProjectsResponse;
import com.descope.model.project.ValidateSnapshotResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ProjectService;
import java.util.HashMap;
import java.util.List;
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
  public NewProjectResponse cloneProject(String name, ProjectTag tag) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("name", name, "tag", tag);
    NewProjectResponse resp = apiProxy.post(getUri(MANAGEMENT_PROJECT_CLONE), request, NewProjectResponse.class);
    return resp;
  }

  @Override
  public NewProjectResponse cloneProject(String name, ProjectEnvironment environment, List<String> tags)
      throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("name", name);
    if (environment != null) {
      request.put("environment", environment.getValue());
    }
    if (tags != null) {
      request.put("tags", tags);
    }
    return apiProxy.post(getUri(MANAGEMENT_PROJECT_CLONE), request, NewProjectResponse.class);
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

  @Override
  public ExportSnapshotResponse exportSnapshot(ExportSnapshotRequest request) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Object body = request != null ? request : new HashMap<>();
    return apiProxy.post(getUri(MANAGEMENT_PROJECT_SNAPSHOT_EXPORT), body, ExportSnapshotResponse.class);
  }

  @Override
  public void importSnapshot(ImportSnapshotRequest request) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PROJECT_SNAPSHOT_IMPORT), request, Void.class);
  }

  @Override
  public ValidateSnapshotResponse validateSnapshot(ImportSnapshotRequest request) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_PROJECT_SNAPSHOT_VALIDATE), request, ValidateSnapshotResponse.class);
  }

  @Override
  public void updateTags(List<String> tags) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Map<String, Object> request = mapOf("tags", tags);
    apiProxy.post(getUri(MANAGEMENT_PROJECT_UPDATE_TAGS), request, Void.class);
  }

  @Override
  public void deleteProject() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_PROJECT_DELETE), new HashMap<>(), Void.class);
  }

  @Override
  public List<Project> listProjects() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    ProjectsResponse resp = apiProxy.post(getUri(MANAGEMENT_PROJECT_LIST), null, ProjectsResponse.class);
    return resp == null ? null : resp.getProjects();
  }
}
