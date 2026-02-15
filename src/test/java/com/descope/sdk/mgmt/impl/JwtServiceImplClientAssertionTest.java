package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.response.ClientAssertionResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.JwtService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class JwtServiceImplClientAssertionTest {

  @Test
  void testGenerateClientAssertionJwtSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    ClientAssertionResponse mockResponse = new ClientAssertionResponse("mock.jwt.token");
    doReturn(mockResponse).when(apiProxy).post(any(), any(), eq(ClientAssertionResponse.class));

    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      Client client = Client.builder()
          .projectId("test-project")
          .managementKey("test-key")
          .build();
      JwtService jwtService = new JwtServiceImpl(client);

      List<String> audience = Arrays.asList("https://auth.example.com/token");
      ClientAssertionResponse response = jwtService.generateClientAssertionJwt(
          "client-id",
          "client-id",
          audience,
          3600,
          null,
          null
      );

      assertNotNull(response);
      assertEquals("mock.jwt.token", response.getJwt());
    }
  }

  @Test
  void testGenerateClientAssertionJwtWithOptionalParams() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    ClientAssertionResponse mockResponse = new ClientAssertionResponse("mock.jwt.token");
    doReturn(mockResponse).when(apiProxy).post(any(), any(), eq(ClientAssertionResponse.class));

    try (MockedStatic<ApiProxyBuilder> mockedBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      Client client = Client.builder()
          .projectId("test-project")
          .managementKey("test-key")
          .build();
      JwtService jwtService = new JwtServiceImpl(client);

      List<String> audience = Arrays.asList("https://auth.example.com/token");
      ClientAssertionResponse response = jwtService.generateClientAssertionJwt(
          "client-id",
          "client-id",
          audience,
          3600,
          true,
          "RS256"
      );

      assertNotNull(response);
      assertEquals("mock.jwt.token", response.getJwt());
    }
  }

  @Test
  void testGenerateClientAssertionJwtEmptyIssuer() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    List<String> audience = Arrays.asList("https://auth.example.com/token");
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("", "client-id", audience, 3600, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The issuer argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateClientAssertionJwtEmptySubject() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    List<String> audience = Arrays.asList("https://auth.example.com/token");
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("client-id", "", audience, 3600, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The subject argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateClientAssertionJwtNullAudience() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("client-id", "client-id", null, 3600, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The audience argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateClientAssertionJwtEmptyAudience() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    List<String> audience = Collections.emptyList();
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("client-id", "client-id", audience, 3600, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The audience argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateClientAssertionJwtNullExpiresIn() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    List<String> audience = Arrays.asList("https://auth.example.com/token");
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("client-id", "client-id", audience, null, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The expiresIn argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateClientAssertionJwtZeroExpiresIn() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    List<String> audience = Arrays.asList("https://auth.example.com/token");
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("client-id", "client-id", audience, 0, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The expiresIn argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateClientAssertionJwtNegativeExpiresIn() {
    Client client = Client.builder()
        .projectId("test-project")
        .managementKey("test-key")
        .build();
    JwtService jwtService = new JwtServiceImpl(client);

    List<String> audience = Arrays.asList("https://auth.example.com/token");
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> jwtService.generateClientAssertionJwt("client-id", "client-id", audience, -1, null, null)
    );
    assertNotNull(thrown);
    assertEquals("The expiresIn argument is invalid", thrown.getMessage());
  }
}
