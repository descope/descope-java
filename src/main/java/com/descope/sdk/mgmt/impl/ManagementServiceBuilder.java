package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.model.magement.ManagementParams;
import com.descope.sdk.mgmt.ManagementService;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ManagementServiceBuilder {

  public static ManagementService buildService(Client client, ManagementParams managementParams) {
    return new JwtServiceImpl(client, managementParams);
  }
}
