package com.descope.sdk.mgmt.impl;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.SdkServicesBase;
import com.descope.sdk.mgmt.ManagementService;
import com.descope.utils.AuthUtils;
import com.descope.utils.UriUtils;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;

abstract class ManagementsBase extends SdkServicesBase implements ManagementService {

  ManagementsBase(Client client) {
    super(client);
  }

  ApiProxy getApiProxy() {
    return getApiProxy(null);
  }

  // Management requests always carry the management key, never the auth management key.
  ApiProxy getApiProxy(String refreshToken) {
    String projectId = client.getProjectId();
    if (StringUtils.isBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(client.getSdkInfo());
    }

    String token = AuthUtils.getBearerHeader(projectId, refreshToken, client.getManagementKey());
    return ApiProxyBuilder.buildProxy(() -> token, client);
  }

  ApiProxy getApiProxyWithBearer(String bearerJwt) {
    if (StringUtils.isBlank(bearerJwt)) {
      throw ServerCommonException.invalidArgument("bearerJwt");
    }
    String token = AuthUtils.getBearerHeader(bearerJwt);
    return ApiProxyBuilder.buildProxy(() -> token, client);
  }

  // FGA calls that the FGA cache serves go to it when one is configured, everything else
  // stays on the Descope base URL.
  URI getFgaUri(String path) {
    String fgaCacheUri = client.getFgaCacheUri();
    if (StringUtils.isBlank(fgaCacheUri)) {
      return getUri(path);
    }
    return UriUtils.getUri(StringUtils.removeEnd(fgaCacheUri, "/"), path);
  }
}
