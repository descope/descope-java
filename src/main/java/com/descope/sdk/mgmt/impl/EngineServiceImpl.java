package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINE_LOAD_ALL_LINK;

import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.engine.Engine;
import com.descope.model.engine.Engines;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.EngineService;
import java.util.Collections;
import java.util.List;

class EngineServiceImpl extends ManagementsBase implements EngineService {

  EngineServiceImpl(Client client) {
    super(client);
  }

  @Override
  public List<Engine> loadAll() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    Engines res = apiProxy.get(getUri(MANAGEMENT_ENGINE_LOAD_ALL_LINK), Engines.class);
    if (res == null || res.getEngines() == null) {
      return Collections.emptyList();
    }
    return res.getEngines();
  }
}
