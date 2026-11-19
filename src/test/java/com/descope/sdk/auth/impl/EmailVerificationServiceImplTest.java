package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.model.client.Client;
import com.descope.model.emailverification.EmailVerificationStartResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.EmailVerificationService;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class EmailVerificationServiceImplTest {

  private EmailVerificationService emailVerificationService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.emailVerificationService =
        AuthenticationServiceBuilder.buildServices(client).getEmailVerificationService();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStart() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EmailVerificationStartResponse("some-nonce")).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });

      EmailVerificationStartResponse response = emailVerificationService.start();

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(EmailVerificationStartResponse.class));
      assertThat(uriCaptor.getValue().getPath()).isEqualTo("/v1/auth/emailverification/start");
      assertThat(bodyCaptor.getValue()).isEmpty();
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
      assertThat(response.getNonce()).isEqualTo("some-nonce");
    }
  }
}
