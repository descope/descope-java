package com.descope.client;

import static com.descope.literals.AppConstants.MANAGEMENT_KEY_ENV_VAR;
import static com.descope.literals.AppConstants.PROJECT_ID_ENV_VAR;
import static com.descope.literals.AppConstants.PUBLIC_KEY_ENV_VAR;
import static com.descope.sdk.TestUtils.MOCK_SIGNING_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ClientSetupException;
import com.descope.exception.ServerCommonException;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

class DescopeClientTest {

  @Test
  void testEnvVariables() throws Exception {
    String expectedProjectID = "P123456789012345678901234567";
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
          Assertions.assertThat(config.getProjectId()).isEqualTo("P123456789012345678901234567");
          Assertions.assertThat(config.getPublicKey()).isEqualTo(expectedPublicKey);
          Assertions.assertThat(config.getManagementKey()).isEqualTo("someManagementKey");
          Assertions.assertThat(descopeClient.getAuthenticationServices()).isNotNull();
          Assertions.assertThat(descopeClient.getManagementServices()).isNotNull();
          ApiProxy apiProxy = mock(ApiProxy.class);
          UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
          when(apiProxy.get(eq(new URI("https://api.descope.com/v1/mgmt/user?loginId=kuku")), any())).thenReturn(userResponseDetails);
          try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
            UserResponseDetails u = descopeClient.getManagementServices().getUserService().load("kuku");
            Assertions.assertThat(u).isNotNull();
          }
        });
  }

  @Test
  void testProjectRegionVariables() throws Exception {
    String expectedProjectID = "Puse1123456789012345678901234567";
    String expectedManagementKey = "someManagementKey";
    EnvironmentVariables env =
        new EnvironmentVariables(PROJECT_ID_ENV_VAR, expectedProjectID)
            .and(MANAGEMENT_KEY_ENV_VAR, expectedManagementKey);
    env.execute(
        () -> {
          DescopeClient descopeClient = new DescopeClient();
          ApiProxy apiProxy = mock(ApiProxy.class);
          UserResponseDetails userResponseDetails = mock(UserResponseDetails.class);
          when(apiProxy.get(eq(new URI("https://api.use1.descope.com/v1/mgmt/user?loginId=kuku")), any())).thenReturn(userResponseDetails);
          try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
            mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
            UserResponseDetails u = descopeClient.getManagementServices().getUserService().load("kuku");
            Assertions.assertThat(u).isNotNull();
          }
        });
  }

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
