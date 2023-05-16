package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_JWT_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.request.UpdateJwtRequest;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.mgmt.ManagementParams;
import com.descope.sdk.mgmt.JwtService;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class JwtServiceImpl extends ManagementsBase implements JwtService {

  JwtServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public String updateJWTWithCustomClaims(String jwt, Map<String, Object> customClaims)
      throws DescopeException {
    if (StringUtils.isBlank(jwt)) {
      throw ServerCommonException.invalidArgument("JWT");
    }

    // customClaims can be nil, it will mean that this JWT will be validated, and updated authz data
    // will be set
    var updateJwtRequest = new UpdateJwtRequest(jwt, customClaims);
    URI updateJwtUri = composeUpdateJwtUri();
    var apiProxy = getApiProxy();

    var jwtResponse = apiProxy.post(updateJwtUri, updateJwtRequest, JWTResponse.class);
    return jwtResponse.getSessionJwt();
  }

  private URI composeUpdateJwtUri() {
    return getUri(UPDATE_JWT_LINK);
  }
}
