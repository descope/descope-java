package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_ENGINES_LOAD_LINK;

import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.engine.Engine;
import com.descope.model.engine.LoadAllEnginesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.EngineService;
import java.util.Collections;
import java.util.List;

public class EngineServiceImpl extends ManagementsBase implements EngineService {

  EngineServiceImpl(Client client) {
    super(client);
  }

  @Override
  public List<Engine> loadAllEngines() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    LoadAllEnginesResponse res = apiProxy.get(getUri(MANAGEMENT_ENGINES_LOAD_LINK), LoadAllEnginesResponse.class);
    if (res == null || res.getEngines() == null) {
      return Collections.emptyList();
    }
    return res.getEngines();
  }
}
