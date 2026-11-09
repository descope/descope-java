package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINE_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINE_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINE_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINE_ROTATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINE_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.engine.Engine;
import com.descope.model.engine.EngineResponse;
import com.descope.model.engine.EngineSecretResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.EngineService;
import org.apache.commons.lang3.StringUtils;

class EngineServiceImpl extends ManagementsBase implements EngineService {

  EngineServiceImpl(Client client) {
    super(client);
  }

  @Override
  public Engine create(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    EngineResponse res = apiProxy.post(getUri(MANAGEMENT_ENGINE_CREATE_LINK), mapOf("name", name),
        EngineResponse.class);
    return res == null ? null : res.getEngine();
  }

  @Override
  public Engine update(String id, String name) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    EngineResponse res = apiProxy.post(getUri(MANAGEMENT_ENGINE_UPDATE_LINK), mapOf("id", id, "name", name),
        EngineResponse.class);
    return res == null ? null : res.getEngine();
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_ENGINE_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public Engine load(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    EngineResponse res = apiProxy.get(getQueryParamUri(MANAGEMENT_ENGINE_LOAD_LINK, mapOf("id", id)),
        EngineResponse.class);
    return res == null ? null : res.getEngine();
  }

  @Override
  public String rotateSecret(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    EngineSecretResponse res = apiProxy.post(getUri(MANAGEMENT_ENGINE_ROTATE_LINK), mapOf("id", id),
        EngineSecretResponse.class);
    return res == null ? null : res.getSecret();
  }
}
