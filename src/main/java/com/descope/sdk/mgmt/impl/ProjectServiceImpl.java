package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_CLONE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PROJECT_UPDATE_NAME;

import java.util.HashMap;
import java.util.Map;

import com.descope.enums.ProjectTag;
import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.project.NewProjectResponse;
import com.descope.sdk.mgmt.ProjectService;

class ProjectServiceImpl extends ManagementsBase implements ProjectService {

  ProjectServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

	@Override
	public void updateName(String name) throws DescopeException {
    var apiProxy = getApiProxy();
		Map<String, Object> request = new HashMap<>();
    request.put("name", name);
    apiProxy.post(getUri(MANAGEMENT_PROJECT_UPDATE_NAME), request, Void.class);
	}

	@Override
	public NewProjectResponse clone(String name, ProjectTag tag) throws DescopeException {
		var apiProxy = getApiProxy();
		Map<String, Object> request = new HashMap<>();
    request.put("name", name);
		request.put("tag", tag);
		var resp = apiProxy.post(getUri(MANAGEMENT_PROJECT_CLONE), request, NewProjectResponse.class);
		return resp;
	}
}
