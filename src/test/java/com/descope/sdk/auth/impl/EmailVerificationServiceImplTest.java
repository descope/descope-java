package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.emailverification.EmailVerificationStartResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.EmailVerificationService;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class EmailVerificationServiceImplTest {

  private static final String AUTH_MANAGEMENT_KEY = "some-auth-management-key";

  @Test
  @SuppressWarnings("unchecked")
  void testStart() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EmailVerificationStartResponse("some-nonce")).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    EmailVerificationStartResponse response;
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      response = service(null).start();
    }
    assertThat(response.getNonce()).isEqualTo("some-nonce");
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(EmailVerificationStartResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/emailverification/start");
    assertThat(body.getValue()).isInstanceOf(Map.class);
    assertThat((Map<?, ?>) body.getValue()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStartWithAuthManagementKey() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EmailVerificationStartResponse("some-nonce")).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> builder = mockStatic(ApiProxyBuilder.class)) {
      builder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      service(AUTH_MANAGEMENT_KEY).start();
    }
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + AUTH_MANAGEMENT_KEY);
  }

  private static EmailVerificationService service(String authManagementKey) {
    return AuthenticationServiceBuilder.buildServices(Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build()).getEmailVerificationService();
  }
}
