package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.IMPERSONATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.IMPERSONATE_STEPUP_LINK;
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
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.JwtService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class JwtServiceImplImpersonateTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";

  private final Client client = Client.builder()
      .uri("https://api.descope.com")
      .projectId(PROJECT_ID)
      .managementKey(MANAGEMENT_KEY)
      .build();

  @FunctionalInterface
  private interface Call {
    String apply(JwtService jwtService);
  }

  @Test
  void testImpersonateWithRefreshDurationAndSessionCookieName() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("k", "v");
    assertRequest(IMPERSONATE_LINK,
        s -> s.impersonate("imp", "login", true, claims, "t1", 3600, "MyCookie"),
        expectedBody(claims, 3600, "MyCookie"));
  }

  @Test
  void testImpersonateStepupWithRefreshDurationAndSessionCookieName() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("k", "v");
    assertRequest(IMPERSONATE_STEPUP_LINK,
        s -> s.impersonateStepup("imp", "login", true, claims, "t1", 3600, "MyCookie"),
        expectedBody(claims, 3600, "MyCookie"));
  }

  @Test
  void testImpersonateExistingOverloadOmitsNewFields() {
    Map<String, Object> expected = expectedBody(null, null, null);
    assertRequest(IMPERSONATE_LINK, s -> s.impersonate("imp", "login", true, null, "t1"), expected);
    assertRequest(IMPERSONATE_STEPUP_LINK, s -> s.impersonateStepup("imp", "login", true, null, "t1"), expected);
  }

  @Test
  void testImpersonateMissingArguments() {
    JwtService jwtService = new JwtServiceImpl(client);
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> jwtService.impersonate("imp", "", true, null, null, 10, "c"));
    assertEquals("The loginId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> jwtService.impersonate("", "login", true, null, null, 10, "c"));
    assertEquals("The impersonatorId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> jwtService.impersonateStepup("imp", "", true, null, null, 10, "c"));
    assertEquals("The loginId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> jwtService.impersonateStepup("", "login", true, null, null, 10, "c"));
    assertEquals("The impersonatorId argument is invalid", thrown.getMessage());
  }

  private static Map<String, Object> expectedBody(Map<String, Object> claims, Integer refreshDuration,
      String sessionCookieName) {
    Map<String, Object> body = new HashMap<>();
    body.put("loginId", "login");
    body.put("impersonatorId", "imp");
    body.put("validateConsent", true);
    body.put("customClaims", claims);
    body.put("selectedTenant", "t1");
    if (refreshDuration != null) {
      body.put("refreshDuration", refreshDuration);
    }
    if (sessionCookieName != null) {
      body.put("sessionCookieName", sessionCookieName);
    }
    return body;
  }

  @SuppressWarnings("unchecked")
  private void assertRequest(String path, Call call, Map<String, Object> expectedBody) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new UpdateJwtResponse("new.jwt"))
        .when(apiProxy).post(any(), any(), eq(UpdateJwtResponse.class));
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      String jwt = call.apply(new JwtServiceImpl(client));
      assertEquals("new.jwt", jwt);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(UpdateJwtResponse.class));
    assertThat(uriCaptor.getValue().getPath()).isEqualTo(path);
    assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
    assertThat((Map<String, Object>) bodyCaptor.getValue()).isEqualTo(expectedBody);
  }
}
