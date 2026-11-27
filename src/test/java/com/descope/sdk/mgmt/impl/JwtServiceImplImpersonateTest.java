package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.JwtService;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class JwtServiceImplImpersonateTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";

  private ApiProxy apiProxy;
  private JwtService jwtService;
  private MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder;
  private final AtomicReference<String> authHeader = new AtomicReference<>();

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    jwtService = ManagementServiceBuilder.buildServices(client).getJwtService();
    apiProxy = mock(ApiProxy.class);
    mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class);
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
  }

  @AfterEach
  void tearDown() {
    mockedApiProxyBuilder.close();
  }

  @Test
  void testImpersonateWithRefreshDurationAndSessionCookieName() {
    when(apiProxy.post(any(URI.class), any(), eq(UpdateJwtResponse.class)))
        .thenReturn(new UpdateJwtResponse("impersonated-jwt"));
    String jwt = jwtService.impersonate("impersonator", "target", true,
        Collections.singletonMap("k", "v"), "tenant", 3600, "my-cookie");
    assertEquals("impersonated-jwt", jwt);
    assertSent("/v1/mgmt/impersonate", 3600, "my-cookie");
  }

  @Test
  void testImpersonateStepupWithRefreshDurationAndSessionCookieName() {
    when(apiProxy.post(any(URI.class), any(), eq(UpdateJwtResponse.class)))
        .thenReturn(new UpdateJwtResponse("stepup-jwt"));
    String jwt = jwtService.impersonateStepup("impersonator", "target", true,
        Collections.singletonMap("k", "v"), "tenant", 3600, "my-cookie");
    assertEquals("stepup-jwt", jwt);
    assertSent("/v1/mgmt/impersonate/stepup", 3600, "my-cookie");
  }

  @Test
  void testImpersonateOmitsUnsetFields() {
    when(apiProxy.post(any(URI.class), any(), eq(UpdateJwtResponse.class)))
        .thenReturn(new UpdateJwtResponse("impersonated-jwt"));
    jwtService.impersonate("impersonator", "target", true, Collections.singletonMap("k", "v"), "tenant");
    Map<String, Object> body = assertSent("/v1/mgmt/impersonate", null, null);
    assertThat(body).doesNotContainKeys("refreshDuration", "sessionCookieName");
  }

  @Test
  void testImpersonateStepupOmitsUnsetFields() {
    when(apiProxy.post(any(URI.class), any(), eq(UpdateJwtResponse.class)))
        .thenReturn(new UpdateJwtResponse("stepup-jwt"));
    jwtService.impersonateStepup("impersonator", "target", true, Collections.singletonMap("k", "v"), "tenant",
        null, null);
    Map<String, Object> body = assertSent("/v1/mgmt/impersonate/stepup", null, null);
    assertThat(body).doesNotContainKeys("refreshDuration", "sessionCookieName");
  }

  @Test
  void testImpersonateRequiresIds() {
    assertThrows(ServerCommonException.class,
        () -> jwtService.impersonate("impersonator", "", true, null, null, 3600, "my-cookie"));
    assertThrows(ServerCommonException.class,
        () -> jwtService.impersonate("", "target", true, null, null, 3600, "my-cookie"));
    assertThrows(ServerCommonException.class,
        () -> jwtService.impersonateStepup("impersonator", "", true, null, null, 3600, "my-cookie"));
    assertThrows(ServerCommonException.class,
        () -> jwtService.impersonateStepup("", "target", true, null, null, 3600, "my-cookie"));
    verifyNoInteractions(apiProxy);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> assertSent(String path, Integer refreshDuration, String sessionCookieName) {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(UpdateJwtResponse.class));
    assertEquals(path, uri.getValue().getPath());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    Map<String, Object> sent = (Map<String, Object>) body.getValue();
    assertEquals("target", sent.get("loginId"));
    assertEquals("impersonator", sent.get("impersonatorId"));
    assertEquals(true, sent.get("validateConsent"));
    assertEquals(Collections.singletonMap("k", "v"), sent.get("customClaims"));
    assertEquals("tenant", sent.get("selectedTenant"));
    assertEquals(refreshDuration, sent.get("refreshDuration"));
    assertEquals(sessionCookieName, sent.get("sessionCookieName"));
    return sent;
  }
}
