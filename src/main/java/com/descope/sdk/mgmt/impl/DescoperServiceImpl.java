package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DESCOPER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DESCOPER_LIST_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.descoper.Descoper;
import com.descope.model.descoper.DescoperAttributes;
import com.descope.model.descoper.DescoperRBAC;
import com.descope.model.descoper.DescoperResponse;
import com.descope.model.descoper.DescopersListResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.DescoperService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class DescoperServiceImpl extends ManagementsBase implements DescoperService {

  DescoperServiceImpl(Client client) {
    super(client);
  }

  @Override
  public Descoper getDescoper(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    DescoperResponse res =
        apiProxy.get(getQueryParamUri(MANAGEMENT_DESCOPER_LINK, mapOf("id", id)), DescoperResponse.class);
    return res == null ? null : res.getDescoper();
  }

  @Override
  public Descoper updateDescoper(String id, DescoperAttributes attributes, DescoperRBAC rbac)
      throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, Object> request = new HashMap<>();
    request.put("id", id);
    if (attributes != null) {
      request.put("attributes", attributes);
    }
    if (rbac != null) {
      request.put("rbac", rbac);
    }
    ApiProxy apiProxy = getApiProxy();
    DescoperResponse res = apiProxy.patch(getUri(MANAGEMENT_DESCOPER_LINK), request, DescoperResponse.class);
    return res == null ? null : res.getDescoper();
  }

  @Override
  public void deleteDescoper(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(getQueryParamUri(MANAGEMENT_DESCOPER_LINK, mapOf("id", id)), null, Void.class);
  }

  @Override
  public DescopersListResponse listDescopers() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_DESCOPER_LIST_LINK), new HashMap<String, Object>(),
        DescopersListResponse.class);
  }
}
