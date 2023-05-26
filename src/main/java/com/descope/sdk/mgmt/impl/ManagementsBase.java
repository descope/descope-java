package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.SdkServicesBase;
import com.descope.sdk.mgmt.ManagementService;
import org.apache.commons.lang3.StringUtils;

abstract class ManagementsBase extends SdkServicesBase implements ManagementService {

  private final ManagementParams managementParams;

  ManagementsBase(Client client, ManagementParams managementParams) {
    super(client);
    this.managementParams = managementParams;
  }

  ApiProxy getApiProxy() {
    String projectId = managementParams.getProjectId();
    if (StringUtils.isNotBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId);
    }
    return ApiProxyBuilder.buildProxy();
  }

  ApiProxy getApiProxy(String refreshToken) {
    String projectId = managementParams.getProjectId();
    if (StringUtils.isBlank(refreshToken) || StringUtils.isNotBlank(projectId)) {
      return getApiProxy();
    }

    String token = String.format("Bearer %s:%s", projectId, refreshToken);
    return ApiProxyBuilder.buildProxy(() -> token);
  }
}
