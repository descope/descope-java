package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.PROJECT_ID;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class EmailVerificationServiceImplTest {

  @Test
  void testStart() {
    assertStart(null, "Bearer " + PROJECT_ID);
  }

  @Test
  void testStartWithAuthManagementKey() {
    assertStart("some-auth-management-key", "Bearer " + PROJECT_ID + ":some-auth-management-key");
  }

  @SuppressWarnings("unchecked")
  private static void assertStart(String authManagementKey, String expectedAuthHeader) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .authManagementKey(authManagementKey)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    EmailVerificationService service =
        AuthenticationServiceBuilder.buildServices(client).getEmailVerificationService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    EmailVerificationStartResponse mockResponse = new EmailVerificationStartResponse("some-nonce");
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      EmailVerificationStartResponse response = service.start();
      Assertions.assertThat(response.getNonce()).isEqualTo("some-nonce");
    }
    Assertions.assertThat(authHeader.get()).isEqualTo(expectedAuthHeader);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(EmailVerificationStartResponse.class));
    Assertions.assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/emailverification/start");
    Assertions.assertThat((Map<String, Object>) body.getValue()).isEmpty();
  }
}
