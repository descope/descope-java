package com.descope.client;

import static com.descope.literals.AppConstants.MANAGEMENT_KEY_ENV_VAR;
import static com.descope.literals.AppConstants.PROJECT_ID_ENV_VAR;
import static com.descope.literals.AppConstants.PUBLIC_KEY_ENV_VAR;

import com.descope.exception.ClientSetupException;
import com.descope.exception.ServerCommonException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DescopeClientTest {

  @Test
  void testEnvVariables() {
    String expectedProjectID = "someProjectId";
    String expectedPublicKey = "somePublicKey";
    String expectedManagementKey = "someManagementKey";

    System.setProperty(PROJECT_ID_ENV_VAR, expectedProjectID);
    System.setProperty(PUBLIC_KEY_ENV_VAR, expectedPublicKey);
    System.setProperty(MANAGEMENT_KEY_ENV_VAR, expectedManagementKey);

    var descopeClient = new DescopeClient();
    var config = descopeClient.getConfig();
    Assertions.assertThat(config.getProjectId()).isEqualTo(expectedProjectID);
    Assertions.assertThat(config.getPublicKey()).isEqualTo(expectedPublicKey);
    Assertions.assertThat(descopeClient.getAuthenticationService()).isNotNull();
    Assertions.assertThat(descopeClient.getManagementService()).isNotNull();
  }

  // TODO - TestConcurrentClients | 17/04/23 | by keshavram

  @Test
  void testEmptyProjectID() {
    Assertions.assertThatThrownBy(DescopeClient::new)
        .isInstanceOf(ClientSetupException.class)
        .hasMessage("Missing project ID");
  }

  @Test
  void testEmptyConfig() {
    Assertions.assertThatThrownBy(() -> new DescopeClient(null))
        .isInstanceOf(ServerCommonException.class)
        .hasMessage("The Config argument is invalid");
  }

  // TODO - TestDescopeSDKMock | 17/04/23 | by keshavram
}
