package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseDetails;
import com.descope.model.mgmt.AccessKeyRequest;
import com.descope.model.mgmt.AccessKeyResponseList;
import com.descope.model.mgmt.AccessKeySearchRequest;
import com.descope.model.mgmt.AccessKeySearchSort;
import com.descope.model.mgmt.AccessKeyUpdateRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AccessKeyService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AccessKeyServiceImplTest {
  private final List<String> mockRoles = Arrays.asList("Test");
  private final AssociatedTenant associatedTenant = new AssociatedTenant("test", "", mockRoles);
  private final List<AssociatedTenant> mockKeyTenants = Arrays.asList(associatedTenant);
  private final AccessKeyResponseDetails mockResponse =
      AccessKeyResponseDetails.builder()
          .keyTenants(mockKeyTenants)
          .name("name")
          .roleNames(mockRoles)
          .id("id")
          .createdBy("TestUser")
          .createdTime(123456789023L)
          .build();
  private final AccessKeyResponse mockAccessResponse = new AccessKeyResponse(mockResponse, "text");
  private AccessKeyService accessKeyService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.accessKeyService =
        ManagementServiceBuilder.buildServices(client).getAccessKeyService();
  }

  @Test
  void testCreateForEmptyName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> accessKeyService.create("", 10, mockRoles, mockKeyTenants));
    assertNotNull(thrown);
    assertEquals("The req.Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.create("Test", 10, mockRoles, mockKeyTenants);
      assertNotNull(response);
    }
  }

  @Test
  void testLoadForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.load(""));
    assertNotNull(thrown);
    assertEquals("The Id argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.load("Id");
      Assertions.assertThat(response.getKey().getId()).isNotBlank();
    }
  }

  @Test
  void testUpdateForEmptyName() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.update("Id", ""));
    assertNotNull(thrown);
    assertEquals("The req.Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.update("Test", "name");
      Assertions.assertThat(response.getKey().getId()).isNotBlank();
    }
  }

  @Test
  void testUpdateForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.update("", "Krishna"));
    assertNotNull(thrown);
    assertEquals("The req.Id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeactivateForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.deactivate(""));
    assertNotNull(thrown);
    assertEquals("The Id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeactivateForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.deactivate("Test");
      Assertions.assertThat(response.getKey().getId()).isNotBlank();
    }
  }

  @Test
  void testActivateForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.activate(""));
    assertNotNull(thrown);
    assertEquals("The Id argument is invalid", thrown.getMessage());
  }

  @Test
  void testActivateForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.activate("Test");
      Assertions.assertThat(response.getKey().getId()).isNotBlank();
    }
  }

  @Test
  void testDeleteForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.delete(""));
    assertNotNull(thrown);
    assertEquals("The Id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      accessKeyService.delete("Test");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testSearchForNullRequest() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.search(null));
    assertNotNull(thrown);
    assertEquals("The req argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchForMissingSortField() {
    AccessKeySearchRequest req = AccessKeySearchRequest.builder()
        .sort(Arrays.asList(AccessKeySearchSort.builder().desc(true).build()))
        .build();
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> accessKeyService.search(req));
    assertNotNull(thrown);
    assertEquals("The req.Sort.Field argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSearchForSuccess() {
    AccessKeyService service = ManagementServiceBuilder.buildServices(mgmtKeyClient()).getAccessKeyService();
    AccessKeySearchRequest req = AccessKeySearchRequest.builder()
        .tenantIds(Arrays.asList("t1"))
        .text("text")
        .sort(Arrays.asList(new AccessKeySearchSort("name", true)))
        .boundUserId("u1")
        .creatingUser("creator")
        .customAttributes(Collections.singletonMap("attr", (Object) "val"))
        .build();
    AccessKeyResponseList mockList = new AccessKeyResponseList(Arrays.asList(mockResponse));
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockList).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      AccessKeyResponseList response = service.search(req);
      assertEquals(mockList, response);
      ArgumentCaptor<Supplier<String>> authCaptor = ArgumentCaptor.forClass(Supplier.class);
      mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(authCaptor.capture(), any()));
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", authCaptor.getValue().get());
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AccessKeyResponseList.class));
      assertEquals("/v1/mgmt/accesskey/search", uriCaptor.getValue().getPath());
      Map<String, Object> body = new ObjectMapper().convertValue(bodyCaptor.getValue(), Map.class);
      assertEquals(Arrays.asList("t1"), body.get("tenantIds"));
      assertEquals("text", body.get("text"));
      assertEquals("u1", body.get("boundUserId"));
      assertEquals("creator", body.get("creatingUser"));
      assertEquals(Collections.singletonMap("attr", "val"), body.get("customAttributes"));
      List<Map<String, Object>> sort = (List<Map<String, Object>>) body.get("sort");
      assertEquals("name", sort.get(0).get("field"));
      assertEquals(true, sort.get(0).get("desc"));
    }
  }

  @Test
  void testCreateWithCustomAttributes() {
    AccessKeyRequest req = AccessKeyRequest.builder()
        .name("name")
        .customAttributes(Collections.singletonMap("attr", (Object) "val"))
        .build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      accessKeyService.create(req);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<AccessKeyRequest> bodyCaptor = ArgumentCaptor.forClass(AccessKeyRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AccessKeyResponse.class));
      assertEquals("/v1/mgmt/accesskey/create", uriCaptor.getValue().getPath());
      assertEquals(Collections.singletonMap("attr", "val"), bodyCaptor.getValue().getCustomAttributes());
    }
  }

  @Test
  void testUpdateWithCustomAttributes() {
    AccessKeyUpdateRequest req = AccessKeyUpdateRequest.builder()
        .id("id")
        .name("name")
        .customAttributes(Collections.singletonMap("attr", (Object) "val"))
        .build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      accessKeyService.update(req);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<AccessKeyUpdateRequest> bodyCaptor = ArgumentCaptor.forClass(AccessKeyUpdateRequest.class);
      verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(AccessKeyResponse.class));
      assertEquals("/v1/mgmt/accesskey/update", uriCaptor.getValue().getPath());
      assertEquals(Collections.singletonMap("attr", "val"), bodyCaptor.getValue().getCustomAttributes());
    }
  }

  @Test
  void testResponseParsesNewKeyFields() throws Exception {
    String json = "{\"cleartext\":\"ct\",\"key\":{\"id\":\"id\",\"boundUserId\":\"u1\","
        + "\"customClaims\":{\"c\":\"v\"},\"editable\":true,\"customAttributes\":{\"a\":\"b\"},"
        + "\"unknownField\":1}}";
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    AccessKeyResponse response = mapper.readValue(json, AccessKeyResponse.class);
    assertEquals("ct", response.getCleartext());
    assertEquals("u1", response.getKey().getBoundUserId());
    assertEquals(Collections.singletonMap("c", "v"), response.getKey().getCustomClaims());
    assertEquals(true, response.getKey().getEditable());
    assertEquals(Collections.singletonMap("a", "b"), response.getKey().getCustomAttributes());
    AccessKeyResponseList list = mapper.readValue(
        "{\"keys\":[{\"id\":\"id\",\"editable\":null,\"customAttributes\":null}]}", AccessKeyResponseList.class);
    Assertions.assertThat(list.getKeys().get(0).getEditable()).isNull();
    Assertions.assertThat(list.getKeys().get(0).getCustomAttributes()).isNull();
  }

  private static Client mgmtKeyClient() {
    Client client = TestUtils.getClient();
    client.setManagementKey("test-key");
    return client;
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String name = TestUtils.getRandomName("ak-");
    AccessKeyResponse createResult = accessKeyService.create(name, 0, null, null);
    Assertions.assertThat(createResult).isNotNull();
    Assertions.assertThat(createResult.getCleartext()).isNotBlank();
    Assertions.assertThat(createResult.getKey()).isNotNull();
    Assertions.assertThat(createResult.getKey().getId()).isNotBlank();
    Assertions.assertThat(createResult.getKey().getName()).isEqualTo(name);
    Assertions.assertThat(createResult.getKey().getStatus()).isEqualTo("active");
    Assertions.assertThat(createResult.getKey().getCreatedBy()).isNotBlank();
    AccessKeyResponse loadResult = accessKeyService.load(createResult.getKey().getId());
    Assertions.assertThat(loadResult).isNotNull();
    Assertions.assertThat(loadResult.getKey()).isNotNull();
    Assertions.assertThat(loadResult.getKey().getId()).isNotBlank();
    Assertions.assertThat(loadResult.getKey().getName()).isEqualTo(name);
    Assertions.assertThat(loadResult.getKey().getStatus()).isEqualTo("active");
    Assertions.assertThat(loadResult.getKey().getCreatedBy()).isNotBlank();
    AccessKeyResponseList searchResult = accessKeyService.searchAll(null);
    Assertions.assertThat(searchResult).isNotNull();
    Assertions.assertThat(searchResult.getKeys()).isNotEmpty();
    accessKeyService.deactivate(createResult.getKey().getId());
    AccessKeyResponse deactivateResult = accessKeyService.load(createResult.getKey().getId());
    Assertions.assertThat(deactivateResult).isNotNull();
    Assertions.assertThat(deactivateResult.getKey()).isNotNull();
    Assertions.assertThat(deactivateResult.getKey().getId()).isNotBlank();
    Assertions.assertThat(deactivateResult.getKey().getName()).isEqualTo(name);
    Assertions.assertThat(deactivateResult.getKey().getStatus()).isEqualTo("inactive");
    Assertions.assertThat(deactivateResult.getKey().getCreatedBy()).isNotBlank();
    assertDoesNotThrow(() -> accessKeyService.delete(createResult.getKey().getId()));
  }
}
