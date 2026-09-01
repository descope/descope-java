package com.descope.proxy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.model.client.Client;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LicenseHeaderTest {

  private static final String LICENSE_HEADER = "x-descope-license";

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testLicenseHeaderSentOnManagementRequest() throws IOException {
    Header header = captureLicenseHeader(client("tier4", "mgmt-key"),
        "http://localhost/v1/mgmt/user/create");
    assertNotNull(header, "management request must carry the license header");
    assertEquals("tier4", header.getValue());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testLicenseHeaderNotSentOnNonManagementRequest() throws IOException {
    Header header = captureLicenseHeader(client("tier4", "mgmt-key"),
        "http://localhost/v1/auth/otp/verify");
    assertNull(header, "auth requests must not carry the license header");
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testLicenseHeaderNotSentWhenTierMissing() throws IOException {
    Header header = captureLicenseHeader(client("", "mgmt-key"),
        "http://localhost/v1/mgmt/user/create");
    assertNull(header, "a failed handshake must leave the header off entirely");
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testLicenseHeaderNotSentWithoutManagementKey() throws IOException {
    Header header = captureLicenseHeader(client("tier4", ""),
        "http://localhost/v1/mgmt/user/create");
    assertNull(header, "without a management key there is no management session to tier");
  }

  // --- helpers ---

  private Client client(String rateLimitTier, String managementKey) {
    return Client.builder()
        .uri("http://localhost")
        .projectId("P123")
        .managementKey(managementKey)
        .rateLimitTier(rateLimitTier)
        .keys(new AtomicReference<>(new java.util.HashMap<>()))
        .build();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Header captureLicenseHeader(Client client, String uri) throws IOException {
    AtomicReference<ClassicHttpRequest> captured = new AtomicReference<>();
    CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      captured.set(inv.getArgument(0));
      HttpClientResponseHandler handler = (HttpClientResponseHandler) inv.getArgument(1);
      return handler.handleResponse(successResponse("{}"));
    }).when(mockClient).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));

    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);
      ApiProxyImpl proxy = new ApiProxyImpl(() -> "Bearer P123:mgmt-key", client);
      proxy.get(URI.create(uri), Map.class);
    }
    return captured.get().getFirstHeader(LICENSE_HEADER);
  }

  private ClassicHttpResponse successResponse(String body) throws IOException {
    ClassicHttpResponse response = mock(ClassicHttpResponse.class);
    org.mockito.Mockito.when(response.getCode()).thenReturn(200);
    HttpEntity entity = mock(HttpEntity.class);
    org.mockito.Mockito.when(entity.getContent())
        .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    org.mockito.Mockito.when(response.getEntity()).thenReturn(entity);
    return response;
  }
}
