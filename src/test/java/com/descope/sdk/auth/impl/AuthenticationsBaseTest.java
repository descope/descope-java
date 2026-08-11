package com.descope.sdk.auth.impl;

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
 * Covers the Authorization header that authentication requests carry, and in particular that the
 * auth management key rides along with them while the management key never does.
 */
class AuthenticationsBaseTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String AUTH_MANAGEMENT_KEY = "some-auth-management-key";
  private static final String REFRESH_TOKEN = "some-refresh-token";

  @Test
  void testNoAuthManagementKey() {
    assertThat(authHeader(client(null, null), AuthenticationsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID);
  }

  @Test
  void testWithAuthManagementKey() {
    assertThat(authHeader(client(null, AUTH_MANAGEMENT_KEY), AuthenticationsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY);
  }

  @Test
  void testWithRefreshTokenAndAuthManagementKey() {
    assertThat(authHeader(client(null, AUTH_MANAGEMENT_KEY), base -> base.getApiProxy(REFRESH_TOKEN)))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY);
  }

  @Test
  void testWithRefreshTokenOnly() {
    assertThat(authHeader(client(null, null), base -> base.getApiProxy(REFRESH_TOKEN)))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN);
  }

  @Test
  void testManagementKeyIsNeverSentOnAuthRequests() {
    assertThat(authHeader(client(MANAGEMENT_KEY, null), AuthenticationsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID);
    assertThat(authHeader(client(MANAGEMENT_KEY, AUTH_MANAGEMENT_KEY), AuthenticationsBase::getApiProxy))
        .isEqualTo("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY);
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

  /** Runs the given call against an auth service and returns the Authorization header it built. */
  @SuppressWarnings("unchecked")
  private static String authHeader(Client client, Function<AuthenticationsBase, ApiProxy> call) {
    AuthenticationsBase base =
        (AuthenticationsBase) AuthenticationServiceBuilder.buildServices(client).getOtpService();
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
