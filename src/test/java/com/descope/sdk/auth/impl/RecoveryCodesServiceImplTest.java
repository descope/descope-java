package com.descope.sdk.auth.impl;

import static com.descope.sdk.TestUtils.MOCK_EMAIL;
import static com.descope.sdk.TestUtils.MOCK_REFRESH_TOKEN;
import static com.descope.sdk.TestUtils.PROJECT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.recoverycodes.RecoveryCode;
import com.descope.model.recoverycodes.RecoveryCodesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.auth.RecoveryCodesService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class RecoveryCodesServiceImplTest {

  private RecoveryCodesService recoveryCodesService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.recoveryCodesService = AuthenticationServiceBuilder.buildServices(client).getRecoveryCodesService();
  }

  @Test
  void testGenerateForEmptyLoginId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> recoveryCodesService.generate("", MOCK_REFRESH_TOKEN));
    assertEquals("The loginId argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateForEmptyRefreshToken() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> recoveryCodesService.generate(MOCK_EMAIL, ""));
    assertEquals("The refreshToken argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGenerateForSuccess() throws Exception {
    RecoveryCodesResponse mockResponse = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue("{\"codes\":[{\"code\":\"code1\"},{\"code\":\"code2\"}],\"unknown\":1}",
            RecoveryCodesResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    RecoveryCodesResponse response;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      response = recoveryCodesService.generate(MOCK_EMAIL, MOCK_REFRESH_TOKEN);
    }

    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(RecoveryCodesResponse.class));
    assertThat(uri.getValue().getPath()).isEqualTo("/v1/auth/recovery-codes");
    assertThat((Map<String, Object>) body.getValue()).isEqualTo(Collections.singletonMap("loginId", MOCK_EMAIL));
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MOCK_REFRESH_TOKEN);
    assertThat(response.getCodes()).extracting(RecoveryCode::getCode).containsExactly("code1", "code2");
  }
}
