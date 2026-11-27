package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SECRET_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SECRET_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SECRET_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SECRET_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SECRET_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.secret.CreateProjectSecretRequest;
import com.descope.model.secret.LoadAllProjectSecretsResponse;
import com.descope.model.secret.ProjectSecret;
import com.descope.model.secret.ProjectSecretResponse;
import com.descope.model.secret.UpdateProjectSecretRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ProjectSecretService;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class ProjectSecretServiceImpl extends ManagementsBase implements ProjectSecretService {

  ProjectSecretServiceImpl(Client client) {
    super(client);
  }

  @Override
  public ProjectSecret createSecret(String name, String value, String description) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    if (StringUtils.isEmpty(value)) {
      throw ServerCommonException.invalidArgument("value");
    }
    CreateProjectSecretRequest request =
        CreateProjectSecretRequest.builder().name(name).description(description).value(value).build();
    ApiProxy apiProxy = getApiProxy();
    ProjectSecretResponse res =
        apiProxy.post(getUri(MANAGEMENT_SECRET_CREATE_LINK), request, ProjectSecretResponse.class);
    return res.getSecret();
  }

  @Override
  public ProjectSecret updateSecret(String id, String description, String value) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    UpdateProjectSecretRequest request = UpdateProjectSecretRequest.builder()
        .id(id)
        .description(description)
        .value(StringUtils.isEmpty(value) ? null : value)
        .build();
    ApiProxy apiProxy = getApiProxy();
    ProjectSecretResponse res =
        apiProxy.post(getUri(MANAGEMENT_SECRET_UPDATE_LINK), request, ProjectSecretResponse.class);
    return res.getSecret();
  }

  @Override
  public void deleteSecret(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_SECRET_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public ProjectSecret loadSecret(String id) throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    ProjectSecretResponse res = apiProxy.get(loadSecretUri(id), ProjectSecretResponse.class);
    return res.getSecret();
  }

  @Override
  public List<ProjectSecret> loadAllSecrets() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    LoadAllProjectSecretsResponse res =
        apiProxy.get(getUri(MANAGEMENT_SECRET_LOAD_ALL_LINK), LoadAllProjectSecretsResponse.class);
    return res.getSecrets() != null ? res.getSecrets() : Collections.emptyList();
  }

  @SneakyThrows
  private URI loadSecretUri(String id) {
    String segment = StringUtils.isBlank(id) ? "" : URLEncoder.encode(id, "UTF-8").replace("+", "%20");
    // Dot segments and "all" would reach a different route
    if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment) || "all".equals(segment)) {
      throw ServerCommonException.invalidArgument("id");
    }
    return getUri(MANAGEMENT_SECRET_LOAD_LINK + segment);
  }
}
