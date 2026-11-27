package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.engine.Engine;
import com.descope.model.engine.LoadAllEnginesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.EngineService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class EngineServiceImplTest {

  private static final String PROJECT_ID = "P0000000000000000000000000000";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String BASE_URI = "https://api.descope.com";

  private EngineService engineService;
  private ApiProxy apiProxy;
  private final AtomicReference<String> authHeader = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri(BASE_URI)
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    engineService = new EngineServiceImpl(client);
    apiProxy = mock(ApiProxy.class);
  }

  @SuppressWarnings("unchecked")
  private MockedStatic<ApiProxyBuilder> mockProxy() {
    MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class);
    mocked.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
    return mocked;
  }

  @Test
  void testLoadAllEngines() {
    Engine e1 = Engine.builder().id("eng1").name("first").secret("").createdTime(1700000000L).build();
    Engine e2 = Engine.builder().id("eng2").name("second").createdTime(1700000001L).build();
    doReturn(new LoadAllEnginesResponse(Arrays.asList(e1, e2))).when(apiProxy).get(any(), any());
    List<Engine> engines;
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      engines = engineService.loadAllEngines();
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    verify(apiProxy).get(eq(URI.create(BASE_URI + "/v1/mgmt/engines/load")), eq(LoadAllEnginesResponse.class));
    assertEquals(2, engines.size());
    assertEquals("eng1", engines.get(0).getId());
    assertEquals("first", engines.get(0).getName());
    assertEquals("", engines.get(0).getSecret());
    assertEquals(1700000000L, engines.get(0).getCreatedTime());
    assertEquals("eng2", engines.get(1).getId());
    assertEquals("second", engines.get(1).getName());
    assertEquals(1700000001L, engines.get(1).getCreatedTime());
  }

  @Test
  void testLoadAllEnginesEmptyResponse() {
    doReturn(new LoadAllEnginesResponse()).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertTrue(engineService.loadAllEngines().isEmpty());
    }
  }

  @Test
  void testLoadAllEnginesError() {
    doThrow(ServerCommonException.class).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertThrows(ServerCommonException.class, () -> engineService.loadAllEngines());
    }
  }

  @Test
  void testLoadAllEnginesResponseParsing() throws Exception {
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    LoadAllEnginesResponse res = mapper.readValue(
        "{\"engines\":[{\"id\":\"eng1\",\"name\":\"first\",\"secret\":\"\",\"createdTime\":1700000000,"
            + "\"extra\":1},{\"id\":\"eng2\"}],\"unknown\":null}",
        LoadAllEnginesResponse.class);
    assertEquals(2, res.getEngines().size());
    assertEquals("eng1", res.getEngines().get(0).getId());
    assertEquals("first", res.getEngines().get(0).getName());
    assertEquals(1700000000L, res.getEngines().get(0).getCreatedTime());
    assertEquals("eng2", res.getEngines().get(1).getId());
  }
}
