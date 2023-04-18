package com.descope.sdk.mgmt.impl;

import com.descope.model.client.Client;
import com.descope.model.magement.ManagementParams;
import com.descope.sdk.mgmt.ManagementService;

abstract class ManagementsBase implements ManagementService {
  private final Client client;
  private final ManagementParams managementParams;

  ManagementsBase(Client client, ManagementParams managementParams) {
    this.client = client;
    this.managementParams = managementParams;
  }
}
