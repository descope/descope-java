package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.engine.Engine;
import com.descope.model.engine.EngineResponse;
import com.descope.model.engine.EngineSecretResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.EngineService;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class EngineServiceImplTest {

  private static final String PROJECT_ID = "P0000000000000000000000000000";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String BASE_URI = "https://api.descope.com";

  private final Engine mockEngine =
      Engine.builder().id("eid").name("ename").secret("esecret").createdTime(1700000000).build();
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

  private void assertEngine(Engine engine) {
    assertEquals("eid", engine.getId());
    assertEquals("ename", engine.getName());
    assertEquals("esecret", engine.getSecret());
    assertEquals(1700000000, engine.getCreatedTime());
  }

  @Test
  void testValidation() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> engineService.createEngine(""));
      assertEquals("The name argument is invalid", thrown.getMessage());

      thrown = assertThrows(ServerCommonException.class, () -> engineService.updateEngine("", "n"));
      assertEquals("The id argument is invalid", thrown.getMessage());
      thrown = assertThrows(ServerCommonException.class, () -> engineService.updateEngine("eid", null));
      assertEquals("The name argument is invalid", thrown.getMessage());

      thrown = assertThrows(ServerCommonException.class, () -> engineService.deleteEngine(null));
      assertEquals("The id argument is invalid", thrown.getMessage());

      thrown = assertThrows(ServerCommonException.class, () -> engineService.loadEngine(""));
      assertEquals("The id argument is invalid", thrown.getMessage());

      thrown = assertThrows(ServerCommonException.class, () -> engineService.rotateEngineSecret(" "));
      assertEquals("The id argument is invalid", thrown.getMessage());
    }
    verifyNoInteractions(apiProxy);
  }

  @Test
  void testCreateEngine() {
    doReturn(new EngineResponse(mockEngine)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertEngine(engineService.createEngine("ename"));
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    verify(apiProxy).post(eq(URI.create(BASE_URI + "/v1/mgmt/engine/create")), eq(mapOf("name", "ename")),
        eq(EngineResponse.class));
  }

  @Test
  void testUpdateEngine() {
    doReturn(new EngineResponse(mockEngine)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertEngine(engineService.updateEngine("eid", "ename"));
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    verify(apiProxy).post(eq(URI.create(BASE_URI + "/v1/mgmt/engine/update")),
        eq(mapOf("id", "eid", "name", "ename")), eq(EngineResponse.class));
  }

  @Test
  void testDeleteEngine() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      engineService.deleteEngine("eid");
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    verify(apiProxy).post(eq(URI.create(BASE_URI + "/v1/mgmt/engine/delete")), eq(mapOf("id", "eid")),
        eq(Void.class));
  }

  @Test
  void testLoadEngine() {
    doReturn(new EngineResponse(mockEngine)).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertEngine(engineService.loadEngine("eid"));
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), eq(EngineResponse.class));
    assertEquals("/v1/mgmt/engine/load", uri.getValue().getPath());
    assertEquals("id=eid", uri.getValue().getQuery());
  }

  @Test
  void testLoadEngineEmptyResponse() {
    doReturn(new EngineResponse()).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertNull(engineService.loadEngine("eid"));
    }
  }

  @Test
  void testRotateEngineSecret() {
    doReturn(new EngineSecretResponse("newsecret")).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertEquals("newsecret", engineService.rotateEngineSecret("eid"));
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
    verify(apiProxy).post(eq(URI.create(BASE_URI + "/v1/mgmt/engine/rotate")), eq(mapOf("id", "eid")),
        eq(EngineSecretResponse.class));
  }
}
