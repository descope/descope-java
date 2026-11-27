package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.JwtService;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class JwtServiceImplStopImpersonationTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";

  private static Client client() {
    return Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .build();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStopImpersonationWithRefreshDuration() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new UpdateJwtResponse("original.jwt"))
        .when(apiProxy).post(any(), any(), eq(UpdateJwtResponse.class));
    AtomicReference<String> authHeader = new AtomicReference<>();

    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      JwtService jwtService = new JwtServiceImpl(client());

      String jwt = jwtService.stopImpersonation("impersonated.jwt", mapOf("k", "v"), "tenant-id", 32);

      assertEquals("original.jwt", jwt);
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uri.capture(), body.capture(), eq(UpdateJwtResponse.class));
      assertEquals("/v1/mgmt/stop/impersonation", uri.getValue().getPath());
      Map<String, Object> request = (Map<String, Object>) body.getValue();
      assertEquals("impersonated.jwt", request.get("jwt"));
      assertEquals(mapOf("k", "v"), request.get("customClaims"));
      assertEquals("tenant-id", request.get("selectedTenant"));
      assertEquals(32, request.get("refreshDuration"));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStopImpersonationWithoutRefreshDuration() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new UpdateJwtResponse("original.jwt"))
        .when(apiProxy).post(any(), any(), eq(UpdateJwtResponse.class));

    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      JwtService jwtService = new JwtServiceImpl(client());

      assertEquals("original.jwt", jwtService.stopImpersonation("impersonated.jwt", null, "tenant-id"));

      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(any(), body.capture(), eq(UpdateJwtResponse.class));
      Map<String, Object> request = (Map<String, Object>) body.getValue();
      assertEquals("impersonated.jwt", request.get("jwt"));
      assertFalse(request.containsKey("refreshDuration"));
    }
  }

  @Test
  void testStopImpersonationEmptyJwt() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      JwtService jwtService = new JwtServiceImpl(client());

      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> jwtService.stopImpersonation("", null, "tenant-id", 32));
      assertEquals("The jwt argument is invalid", thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }
}
