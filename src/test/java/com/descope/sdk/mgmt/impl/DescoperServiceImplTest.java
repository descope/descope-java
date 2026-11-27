package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.descoper.Descoper;
import com.descope.model.descoper.DescoperAttributes;
import com.descope.model.descoper.DescoperListResponse;
import com.descope.model.descoper.DescoperProjectRole;
import com.descope.model.descoper.DescoperRBAC;
import com.descope.model.descoper.DescoperResponse;
import com.descope.model.descoper.DescoperRole;
import com.descope.model.descoper.DescoperStatus;
import com.descope.model.descoper.DescoperTagRole;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.DescoperService;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class DescoperServiceImplTest {

  private static final String PROJECT_ID = "P0000000000000000000000000000";
  private static final String MANAGEMENT_KEY = "some-management-key";

  private final Descoper mockDescoper = Descoper.builder()
      .id("D1")
      .loginIds(Collections.singletonList("user@example.com"))
      .attributes(DescoperAttributes.builder().displayName("Name").email("user@example.com").phone("+1555").build())
      .rbac(DescoperRBAC.builder()
          .isCompanyAdmin(true)
          .tags(Collections.singletonList(
              DescoperTagRole.builder().tags(Collections.singletonList("t1")).role(DescoperRole.ADMIN).build()))
          .projects(Collections.singletonList(
              DescoperProjectRole.builder().projectIds(Collections.singletonList("P1")).role(DescoperRole.SUPPORT)
                  .build()))
          .build())
      .status(DescoperStatus.ENABLED)
      .build();

  private DescoperService descoperService;
  private ApiProxy apiProxy;
  private final AtomicReference<String> authHeader = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    descoperService = ManagementServiceBuilder.buildServices(client).getDescoperService();
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

  private void assertDescoper(Descoper d) {
    assertEquals(mockDescoper, d);
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
  }

  @Test
  void testLoadSuccess() {
    doReturn(new DescoperResponse(mockDescoper)).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertDescoper(descoperService.load("D1"));
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), any());
    assertEquals("/v1/mgmt/descoper", uri.getValue().getPath());
    assertEquals("id=D1", uri.getValue().getQuery());
  }

  @Test
  void testLoadMissingDescoper() {
    doReturn(new DescoperResponse()).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertNull(descoperService.load("D1"));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateSuccess() {
    doReturn(new DescoperResponse(mockDescoper)).when(apiProxy).patch(any(), any(), any());
    DescoperAttributes attributes = DescoperAttributes.builder().displayName("New").build();
    DescoperRBAC rbac = DescoperRBAC.builder().isCompanyAdmin(true).build();
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      assertDescoper(descoperService.update("D1", attributes, rbac));
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).patch(uri.capture(), body.capture(), any());
    assertEquals("/v1/mgmt/descoper", uri.getValue().getPath());
    Map<String, Object> req = (Map<String, Object>) body.getValue();
    assertEquals("D1", req.get("id"));
    assertEquals(attributes, req.get("attributes"));
    assertEquals(rbac, req.get("rbac"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateOmitsUnsetFields() {
    doReturn(new DescoperResponse(mockDescoper)).when(apiProxy).patch(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      descoperService.update("D1", null, null);
    }
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).patch(any(), body.capture(), any());
    Map<String, Object> req = (Map<String, Object>) body.getValue();
    assertEquals(Collections.singletonMap("id", "D1"), req);
  }

  @Test
  void testDeleteSuccess() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      descoperService.delete("D1");
    }
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).delete(uri.capture(), isNull(), any());
    assertEquals("/v1/mgmt/descoper", uri.getValue().getPath());
    assertEquals("id=D1", uri.getValue().getQuery());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, authHeader.get());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testListSuccess() {
    DescoperListResponse response = new DescoperListResponse(Arrays.asList(mockDescoper, mockDescoper), 2);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    DescoperListResponse res;
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      res = descoperService.list();
    }
    assertEquals(2, res.getTotal());
    assertEquals(2, res.getDescopers().size());
    assertDescoper(res.getDescopers().get(0));
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uri.capture(), body.capture(), any());
    assertEquals("/v1/mgmt/descoper/list", uri.getValue().getPath());
    assertTrue(((Map<String, Object>) body.getValue()).isEmpty());
  }

  @Test
  void testMissingId() {
    try (MockedStatic<ApiProxyBuilder> mocked = mockProxy()) {
      ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> descoperService.load(""));
      assertEquals("The id argument is invalid", thrown.getMessage());
      thrown = assertThrows(ServerCommonException.class, () -> descoperService.update(null, null, null));
      assertEquals("The id argument is invalid", thrown.getMessage());
      thrown = assertThrows(ServerCommonException.class, () -> descoperService.delete(" "));
      assertEquals("The id argument is invalid", thrown.getMessage());
    }
    verifyNoInteractions(apiProxy);
    assertNull(authHeader.get());
  }
}
