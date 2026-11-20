package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.model.auth.EmailVerificationStartResponse;
import com.descope.model.client.Client;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.EmailVerificationService;
import java.net.URI;
import java.util.Collections;
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
  void testStartForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new EmailVerificationStartResponse("some-nonce")).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      assertThat(emailVerificationService.start()).isEqualTo("some-nonce");
    }
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID);
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(Collections.emptyMap()), eq(EmailVerificationStartResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/emailverification/start");
  }
}
