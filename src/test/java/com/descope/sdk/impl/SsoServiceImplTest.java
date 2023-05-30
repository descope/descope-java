package com.descope.sdk.impl;

import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.sdk.mgmt.SsoService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import org.junit.jupiter.api.BeforeEach;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;

public class SsoServiceImplTest {

  private SsoService ssoService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.ssoService = ManagementServiceBuilder.buildServices(client, authParams).getSsoService();
  }
}
