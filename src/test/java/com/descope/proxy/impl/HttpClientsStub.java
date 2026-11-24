package com.descope.proxy.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.mockito.MockedStatic;

public final class HttpClientsStub {

  private HttpClientsStub() {}

  /** Makes HttpClients.custom()...build() return the given client; returns the mocked builder. */
  public static HttpClientBuilder stub(MockedStatic<HttpClients> mocked, CloseableHttpClient client) {
    HttpClientBuilder builder = mock(HttpClientBuilder.class, RETURNS_SELF);
    when(builder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(builder);
    when(builder.build()).thenReturn(client);
    mocked.when(HttpClients::custom).thenReturn(builder);
    return builder;
  }
}
