package com.descope.client;

import static com.descope.literals.AppConstants.MANAGEMENT_KEY_ENV_VAR;
import static com.descope.literals.AppConstants.PROJECT_ID_ENV_VAR;
import static com.descope.literals.AppConstants.PUBLIC_KEY_ENV_VAR;

import com.descope.exception.ClientSetupException;
import com.descope.exception.ServerCommonException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

class DescopeClientTest {

  @Test
  void testEnvVariables() throws Exception {
    String expectedProjectID = "someProject";
    String expectedPublicKey = "somePublicKey";
    String expectedManagementKey = "someManagementKey";
    EnvironmentVariables env = new EnvironmentVariables(PROJECT_ID_ENV_VAR, expectedProjectID)
        .and(PUBLIC_KEY_ENV_VAR, expectedPublicKey)
        .and(MANAGEMENT_KEY_ENV_VAR, expectedManagementKey);
    env.execute(() -> {
      var descopeClient = new DescopeClient();
      var config = descopeClient.getConfig();
      Assertions.assertThat(config.getProjectId()).isEqualTo("someProject");
      Assertions.assertThat(config.getPublicKey()).isEqualTo("somePublicKey");
      Assertions.assertThat(config.getManagementKey()).isEqualTo("someManagementKey");
      Assertions.assertThat(descopeClient.getAuthenticationServices()).isNotNull();
      Assertions.assertThat(descopeClient.getManagementServices()).isNotNull();
    });
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
