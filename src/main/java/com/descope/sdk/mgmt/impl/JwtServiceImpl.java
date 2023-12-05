package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_JWT_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.request.UpdateJwtRequest;
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.JwtService;
import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class JwtServiceImpl extends ManagementsBase implements JwtService {

  JwtServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public Token updateJWTWithCustomClaims(String jwt, Map<String, Object> customClaims)
      throws DescopeException {
    if (StringUtils.isBlank(jwt)) {
      throw ServerCommonException.invalidArgument("JWT");
    }

    // customClaims can be nil, it will mean that this JWT will be validated, and updated authz data
    // will be set
    UpdateJwtRequest updateJwtRequest = new UpdateJwtRequest(jwt, customClaims);
    URI updateJwtUri = composeUpdateJwtUri();
    ApiProxy apiProxy = getApiProxy();

    UpdateJwtResponse jwtResponse = apiProxy.post(updateJwtUri, updateJwtRequest, UpdateJwtResponse.class);
    return validateAndCreateToken(jwtResponse.getJwt());
  }

  private URI composeUpdateJwtUri() {
    return getUri(UPDATE_JWT_LINK);
  }
}
