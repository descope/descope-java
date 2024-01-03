package com.descope.client;

import static com.descope.literals.AppConstants.MANAGEMENT_KEY_ENV_VAR;
import static com.descope.literals.AppConstants.PROJECT_ID_ENV_VAR;
import static com.descope.literals.AppConstants.PUBLIC_KEY_ENV_VAR;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;

import com.descope.exception.ClientSetupException;
import com.descope.exception.ServerCommonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

class DescopeClientTest {

  @Test
  void testEnvVariables() throws Exception {
    String expectedProjectID = "someProject";
    ObjectMapper objectMapper = new ObjectMapper();
    String expectedPublicKey = objectMapper.writeValueAsString(MOCK_SIGNING_KEY);
    String expectedManagementKey = "someManagementKey";
    EnvironmentVariables env =
        new EnvironmentVariables(PROJECT_ID_ENV_VAR, expectedProjectID)
            .and(PUBLIC_KEY_ENV_VAR, expectedPublicKey)
            .and(MANAGEMENT_KEY_ENV_VAR, expectedManagementKey);
    env.execute(
        () -> {
          DescopeClient descopeClient = new DescopeClient();
          Config config = descopeClient.getConfig();
          Assertions.assertThat(config.getProjectId()).isEqualTo("someProject");
          Assertions.assertThat(config.getPublicKey()).isEqualTo(expectedPublicKey);
          Assertions.assertThat(config.getManagementKey()).isEqualTo("someManagementKey");
          Assertions.assertThat(descopeClient.getAuthenticationServices()).isNotNull();
          Assertions.assertThat(descopeClient.getManagementServices()).isNotNull();
        });
  }

  // TODO - TestConcurrentClients | 17/04/23 | by keshavram

  @Test
  void testEmptyProjectID() throws Exception {
    EnvironmentVariables env =
        new EnvironmentVariables(PROJECT_ID_ENV_VAR, "");
    env.execute(
        () -> {
          Assertions.assertThatThrownBy(DescopeClient::new)
              .isInstanceOf(ClientSetupException.class)
              .hasMessage("Missing project ID");
        });
  }

  @Test
  void testEmptyConfig() {
    Assertions.assertThatThrownBy(() -> new DescopeClient(null))
        .isInstanceOf(ServerCommonException.class)
        .hasMessage("The Config argument is invalid");
  }
}
