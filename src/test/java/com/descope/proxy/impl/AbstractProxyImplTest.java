package com.descope.proxy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.DescopeException;
import com.descope.model.client.SdkInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AbstractProxyImplTest {

  private long[] originalDelays;

  @BeforeEach
  void setUp() {
    originalDelays = AbstractProxyImpl.retryDelaysMs;
    AbstractProxyImpl.retryDelaysMs = new long[] {0L, 0L, 0L};
  }

  @AfterEach
  void tearDown() {
    AbstractProxyImpl.retryDelaysMs = originalDelays;
  }

  @Test
  void testRetryDelayConfig() {
    assertEquals(3, originalDelays.length);
    assertEquals(100L, originalDelays[0]);
    assertEquals(5000L, originalDelays[1]);
    assertEquals(5000L, originalDelays[2]);
  }

  @Test
  void testRetryStatusCodesConfig() {
    assertTrue(AbstractProxyImpl.retryableStatusCodes.contains(503));
    assertTrue(AbstractProxyImpl.retryableStatusCodes.contains(521));
    assertTrue(AbstractProxyImpl.retryableStatusCodes.contains(522));
    assertTrue(AbstractProxyImpl.retryableStatusCodes.contains(524));
    assertTrue(AbstractProxyImpl.retryableStatusCodes.contains(530));
    assertEquals(5, AbstractProxyImpl.retryableStatusCodes.size());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testRetryOnRetryableStatusCodes() throws IOException {
    List<Integer> retryableCodes = Arrays.asList(503, 521, 522, 524, 530);
    for (int statusCode : retryableCodes) {
      AtomicInteger callCount = new AtomicInteger(0);
      CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
      doAnswer(inv -> {
        HttpClientResponseHandler handler = (HttpClientResponseHandler) inv.getArgument(1);
        if (callCount.getAndIncrement() == 0) {
          return handler.handleResponse(retryableResponse(statusCode));
        }
        return handler.handleResponse(successResponse("{}"));
      }).when(mockClient).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));

      try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
        mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);
        ApiProxyImpl proxy = new ApiProxyImpl((SdkInfo) null);
        Object result = proxy.get(URI.create("http://localhost/test"), Map.class);
        assertEquals(2, callCount.get(), "Expected retry for status " + statusCode);
        assertTrue(result instanceof Map);
      }
    }
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testRetryUpToThreeTimes() throws IOException {
    AtomicInteger callCount = new AtomicInteger(0);
    CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      HttpClientResponseHandler handler = (HttpClientResponseHandler) inv.getArgument(1);
      callCount.getAndIncrement();
      // Use errorResponse so the entity is available when retries are exhausted on the 4th call
      return handler.handleResponse(errorResponse(503,
          "{\"errorCode\":\"E503\",\"errorDescription\":\"service unavailable\"}"));
    }).when(mockClient).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));

    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);
      ApiProxyImpl proxy = new ApiProxyImpl((SdkInfo) null);
      assertThrows(DescopeException.class,
          () -> proxy.get(URI.create("http://localhost/test"), Map.class));
      // 1 original + 3 retries = 4 total calls
      assertEquals(4, callCount.get());
    }
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testNoRetryOnNonRetryableStatusCodes() throws IOException {
    List<Integer> nonRetryableCodes = Arrays.asList(400, 401, 403, 404, 500, 502);
    for (int statusCode : nonRetryableCodes) {
      AtomicInteger callCount = new AtomicInteger(0);
      CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
      doAnswer(inv -> {
        HttpClientResponseHandler handler = (HttpClientResponseHandler) inv.getArgument(1);
        callCount.getAndIncrement();
        return handler.handleResponse(errorResponse(statusCode,
            "{\"errorCode\":\"E0\",\"errorDescription\":\"error\"}"));
      }).when(mockClient).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));

      try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
        mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);
        ApiProxyImpl proxy = new ApiProxyImpl((SdkInfo) null);
        assertThrows(DescopeException.class,
            () -> proxy.get(URI.create("http://localhost/test"), Map.class));
        assertEquals(1, callCount.get(), "Should not retry on status " + statusCode);
      }
    }
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testSuccessOnThirdRetry() throws IOException {
    AtomicInteger callCount = new AtomicInteger(0);
    CloseableHttpClient mockClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      HttpClientResponseHandler handler = (HttpClientResponseHandler) inv.getArgument(1);
      int call = callCount.getAndIncrement();
      if (call < 3) {
        return handler.handleResponse(retryableResponse(503));
      }
      return handler.handleResponse(successResponse("{}"));
    }).when(mockClient).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));

    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(mockClient);
      ApiProxyImpl proxy = new ApiProxyImpl((SdkInfo) null);
      Object result = proxy.get(URI.create("http://localhost/test"), Map.class);
      assertEquals(4, callCount.get());
      assertTrue(result instanceof Map);
    }
  }

  // --- helpers ---

  private ClassicHttpResponse retryableResponse(int statusCode) {
    ClassicHttpResponse response = mock(ClassicHttpResponse.class);
    org.mockito.Mockito.when(response.getCode()).thenReturn(statusCode);
    return response;
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

  private ClassicHttpResponse errorResponse(int statusCode, String body) throws IOException {
    ClassicHttpResponse response = mock(ClassicHttpResponse.class);
    org.mockito.Mockito.when(response.getCode()).thenReturn(statusCode);
    HttpEntity entity = mock(HttpEntity.class);
    org.mockito.Mockito.when(entity.getContent())
        .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    org.mockito.Mockito.when(response.getEntity()).thenReturn(entity);
    return response;
  }
}
