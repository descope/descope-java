package com.descope.sdk.auth.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.recoverycodes.RecoveryCode;
import com.descope.model.recoverycodes.RecoveryCodesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.RecoveryCodesService;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class RecoveryCodesServiceImplTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String AUTH_MANAGEMENT_KEY = "some-auth-management-key";
  private static final String REFRESH_TOKEN = "some-refresh-token";
  private static final String LOGIN_ID = "username@domain.com";

  @Test
  void testGenerate() {
    RecoveryCodesResponse expected = new RecoveryCodesResponse(
        Arrays.asList(new RecoveryCode("code-1"), new RecoveryCode("code-2")));
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(expected).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    RecoveryCodesResponse response;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(headerOf(inv.getArgument(0)));
        return apiProxy;
      });
      response = service(null).generate(LOGIN_ID, REFRESH_TOKEN);
    }
    verify(apiProxy).post(
        eq(URI.create("https://api.descope.com/v1/auth/recovery-codes")),
        eq(mapOf("loginId", LOGIN_ID)),
        eq(RecoveryCodesResponse.class));
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN);
    assertThat(response.getCodes()).extracting(RecoveryCode::getCode).containsExactly("code-1", "code-2");
  }

  @Test
  void testGenerateWithAuthManagementKey() {
    AtomicReference<String> header = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(headerOf(inv.getArgument(0)));
        return mock(ApiProxy.class);
      });
      service(AUTH_MANAGEMENT_KEY).generate(LOGIN_ID, REFRESH_TOKEN);
    }
    assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + REFRESH_TOKEN + ":" + AUTH_MANAGEMENT_KEY);
  }

  @Test
  void testGenerateForEmptyLoginId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> service(null).generate("", REFRESH_TOKEN));
    assertThat(thrown.getMessage()).isEqualTo("The loginId argument is invalid");
  }

  @Test
  void testGenerateForEmptyRefreshToken() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> service(null).generate(LOGIN_ID, ""));
    assertThat(thrown.getMessage()).isEqualTo("The refreshToken argument is invalid");
  }

  private static RecoveryCodesService service(String authManagementKey) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return AuthenticationServiceBuilder.buildServices(client).getRecoveryCodesService();
  }

  @SuppressWarnings("unchecked")
  private static String headerOf(Object supplier) {
    return ((Supplier<String>) supplier).get();
  }
}
