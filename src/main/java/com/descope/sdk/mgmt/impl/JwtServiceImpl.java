package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.model.magement.ManagementParams;
import com.descope.sdk.mgmt.JwtService;

class JwtServiceImpl extends ManagementsBase implements JwtService {

  JwtServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }
}
