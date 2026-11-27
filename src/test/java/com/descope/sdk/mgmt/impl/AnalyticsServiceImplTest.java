package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.model.analytics.AnalyticRecord;
import com.descope.model.analytics.AnalyticsSearchRequest;
import com.descope.model.analytics.AnalyticsSearchResponse;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AnalyticsService;
import com.descope.sdk.mgmt.impl.AnalyticsServiceImpl.ActualAnalyticRecord;
import com.descope.sdk.mgmt.impl.AnalyticsServiceImpl.ActualAnalyticsSearchResponse;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class AnalyticsServiceImplTest {
  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";

  private AnalyticsService analyticsService;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    this.analyticsService = ManagementServiceBuilder.buildServices(client).getAnalyticsService();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSearchForSuccess() throws Exception {
    ActualAnalyticsSearchResponse resp = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue("{\"analytics\":[{\"projectId\":\"p1\",\"action\":\"LoginSucceed\",\"created\":\"1700000000000\","
            + "\"device\":\"Desktop\",\"method\":\"otp\",\"geo\":\"US\",\"tenant\":\"t1\",\"referrer\":\"r1\","
            + "\"cnt\":5,\"unknown\":1}]}", ActualAnalyticsSearchResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(resp).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> header = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        header.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      AnalyticsSearchResponse response = analyticsService.search(AnalyticsSearchRequest.builder()
          .from(Instant.ofEpochMilli(1600000000000L))
          .to(Instant.ofEpochMilli(1700000000000L))
          .actions(Arrays.asList("LoginSucceed"))
          .excludedActions(Arrays.asList("LoginFailed"))
          .devices(Arrays.asList("Desktop"))
          .methods(Arrays.asList("otp"))
          .geos(Arrays.asList("US"))
          .tenants(Arrays.asList("t1"))
          .groupByAction(true)
          .groupByDevice(true)
          .groupByMethod(false)
          .groupByGeo(true)
          .groupByTenant(true)
          .groupByReferrer(true)
          .groupByCreated(AnalyticsSearchRequest.GROUP_BY_CREATED_DAY)
          .build());

      assertThat(header.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uri.capture(), body.capture(), any());
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/analytics/search");
      assertThat(toJson(body.getValue())).isEqualTo("{\"from\":\"1600000000000\",\"to\":\"1700000000000\","
          + "\"actions\":[\"LoginSucceed\"],\"excludedActions\":[\"LoginFailed\"],\"devices\":[\"Desktop\"],"
          + "\"methods\":[\"otp\"],\"geos\":[\"US\"],\"tenants\":[\"t1\"],\"groupByAction\":true,"
          + "\"groupByDevice\":true,\"groupByMethod\":false,\"groupByGeo\":true,\"groupByTenant\":true,"
          + "\"groupByReferrer\":true,\"groupByCreated\":\"d\"}");

      assertThat(response.getAnalytics()).containsExactly(new AnalyticRecord("p1", "LoginSucceed",
          Instant.ofEpochMilli(1700000000000L), "Desktop", "otp", "US", "t1", "r1", 5));
    }
  }

  @Test
  void testSearchOmitsUnsetFieldsAndHandlesEmptyResponse() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new ActualAnalyticsSearchResponse(null)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AnalyticsSearchResponse response = analyticsService.search(null);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(any(), body.capture(), any());
      assertThat(toJson(body.getValue())).isEqualTo("{}");
      assertThat(response.getAnalytics()).isEmpty();
    }
  }

  @Test
  void testSearchRecordWithMissingFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new ActualAnalyticsSearchResponse(Arrays.asList(new ActualAnalyticRecord())))
        .when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AnalyticsSearchResponse response = analyticsService.search(new AnalyticsSearchRequest());
      assertThat(response.getAnalytics()).containsExactly(new AnalyticRecord());
    }
  }

  // Same serialization as ApiProxy.post
  private static String toJson(Object body) throws Exception {
    return new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(body);
  }
}
