package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Mirror of AuthenticationsBaseTest: management requests carry the management key and must never
 * carry the auth management key.
 */
class ManagementsBaseTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String AUTH_MANAGEMENT_KEY = "some-auth-management-key";

  @Test
  void testManagementKeyIsSent() {
    assertThat(authHeader(client(MANAGEMENT_KEY, null), ManagementsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
  }

  @Test
  void testAuthManagementKeyIsNeverSentOnManagementRequests() {
    assertThat(authHeader(client(MANAGEMENT_KEY, AUTH_MANAGEMENT_KEY), ManagementsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
    assertThat(authHeader(client(null, AUTH_MANAGEMENT_KEY), ManagementsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testBearerOnlyProxy() {
    assertThat(authHeader(client(MANAGEMENT_KEY, null), base -> base.getApiProxyWithBearer("a-jwt")))
        .isEqualTo("Bearer a-jwt");
  }

  private static Client client(String managementKey, String authManagementKey) {
    return Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(managementKey)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
  }

  /** Runs the given call against a management service and returns the header it built. */
  @SuppressWarnings("unchecked")
  private static String authHeader(Client client, Function<ManagementsBase, ApiProxy> call) {
    ManagementsBase base =
        (ManagementsBase) ManagementServiceBuilder.buildServices(client).getTenantService();
    AtomicReference<String> captured = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.set(((Supplier<String>) inv.getArgument(0)).get());
        return mock(ApiProxy.class);
      });
      call.apply(base);
    }
    return captured.get();
  }
}
