package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.SdkServicesBase;
import com.descope.sdk.mgmt.ManagementService;
import org.apache.commons.lang3.StringUtils;

abstract class ManagementsBase extends SdkServicesBase implements ManagementService {

  ManagementsBase(Client client) {
    super(client);
  }

  ApiProxy getApiProxy() {
    String projectId = client.getProjectId();
    String managementKey = client.getManagementKey();
    if (StringUtils.isNotBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(
          () -> String.format("Bearer %s:%s", projectId, managementKey), client);
    }
    return ApiProxyBuilder.buildProxy(client.getSdkInfo());
  }

  ApiProxy getApiProxy(String refreshToken) {
    String projectId = client.getProjectId();
    if (StringUtils.isBlank(refreshToken) || StringUtils.isNotBlank(projectId)) {
      return getApiProxy();
    }

    String token = String.format("Bearer %s:%s", projectId, refreshToken);
    return ApiProxyBuilder.buildProxy(() -> token, client);
  }
}
