package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.mgmt.AccessKeyRequest;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseDetails;
import com.descope.model.mgmt.AccessKeyResponseList;
import com.descope.model.mgmt.AccessKeySearchRequest;
import com.descope.model.mgmt.AccessKeySearchSort;
import com.descope.model.mgmt.AccessKeyUpdateRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AccessKeyService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AccessKeyServiceImplTest {
  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
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
  void testCreateWithCustomAttributes() throws Exception {
    Map<String, Object> customAttributes = Collections.singletonMap("attr1", "value1");
    AccessKeyRequest req = AccessKeyRequest.builder().name("name").customAttributes(customAttributes).build();
    Captured captured = capturePost(mockKeyResponse(), svc -> svc.create(req));
    assertEquals("https://api.descope.com/v1/mgmt/accesskey/create", captured.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, captured.authHeader);
    JsonNode body = captured.body;
    assertEquals("name", body.get("name").asText());
    assertEquals("value1", body.get("customAttributes").get("attr1").asText());
    assertEquals(customAttributes, ((AccessKeyResponse) captured.response).getKey().getCustomAttributes());
  }

  @Test
  void testCreateOmitsUnsetCustomAttributes() throws Exception {
    AccessKeyRequest req = AccessKeyRequest.builder().name("name").build();
    Captured captured = capturePost(mockKeyResponse(), svc -> svc.create(req));
    assertFalse(captured.body.has("customAttributes"));
  }

  @Test
  void testSearchWithFilters() throws Exception {
    Map<String, Object> customAttributes = Collections.singletonMap("attr1", "value1");
    AccessKeySearchRequest req = AccessKeySearchRequest.builder()
        .tenantIds(Arrays.asList("t1"))
        .text("some text")
        .sort(Arrays.asList(new AccessKeySearchSort("name", true)))
        .boundUserId("u1")
        .creatingUser("u2")
        .customAttributes(customAttributes)
        .build();
    AccessKeyResponseList mockList = new AccessKeyResponseList(Arrays.asList(mockKeyResponse().getKey()));
    Captured captured = capturePost(mockList, svc -> svc.search(req));
    assertEquals("https://api.descope.com/v1/mgmt/accesskey/search", captured.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, captured.authHeader);
    JsonNode body = captured.body;
    assertEquals("t1", body.get("tenantIds").get(0).asText());
    assertEquals("some text", body.get("text").asText());
    assertEquals("name", body.get("sort").get(0).get("field").asText());
    assertTrue(body.get("sort").get(0).get("desc").asBoolean());
    assertEquals("u1", body.get("boundUserId").asText());
    assertEquals("u2", body.get("creatingUser").asText());
    assertEquals("value1", body.get("customAttributes").get("attr1").asText());
    AccessKeyResponseList response = (AccessKeyResponseList) captured.response;
    assertEquals(customAttributes, response.getKeys().get(0).getCustomAttributes());
  }

  @Test
  void testSearchWithNullRequest() throws Exception {
    Captured captured = capturePost(new AccessKeyResponseList(), svc -> svc.search(null));
    assertEquals("https://api.descope.com/v1/mgmt/accesskey/search", captured.uri.toString());
    assertEquals(0, captured.body.size());
  }

  @Test
  void testUpdateWithCustomAttributes() throws Exception {
    Map<String, Object> customAttributes = Collections.singletonMap("attr1", "value1");
    AccessKeyUpdateRequest req =
        AccessKeyUpdateRequest.builder().id("id").name("name").customAttributes(customAttributes).build();
    Captured captured = capturePost(mockKeyResponse(), svc -> svc.update(req));
    assertEquals("https://api.descope.com/v1/mgmt/accesskey/update", captured.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, captured.authHeader);
    JsonNode body = captured.body;
    assertEquals("id", body.get("id").asText());
    assertEquals("name", body.get("name").asText());
    assertEquals("value1", body.get("customAttributes").get("attr1").asText());
    assertEquals(customAttributes, ((AccessKeyResponse) captured.response).getKey().getCustomAttributes());
  }

  @Test
  void testResponseCustomAttributesParsed() throws Exception {
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    AccessKeyResponse response = mapper.readValue(
        "{\"key\":{\"id\":\"id\",\"customAttributes\":{\"attr1\":\"value1\"},\"somethingNew\":1}}",
        AccessKeyResponse.class);
    assertEquals("value1", response.getKey().getCustomAttributes().get("attr1"));
    AccessKeyResponse nullAttributes =
        mapper.readValue("{\"key\":{\"id\":\"id\",\"customAttributes\":null}}", AccessKeyResponse.class);
    assertNull(nullAttributes.getKey().getCustomAttributes());
  }

  private AccessKeyResponse mockKeyResponse() {
    AccessKeyResponseDetails details = AccessKeyResponseDetails.builder()
        .id("id")
        .name("name")
        .customAttributes(Collections.singletonMap("attr1", "value1"))
        .build();
    return new AccessKeyResponse(details, "text");
  }

  private interface ServiceCall {
    Object call(AccessKeyService svc);
  }

  private static class Captured {
    URI uri;
    JsonNode body;
    String authHeader;
    Object response;
  }

  @SuppressWarnings("unchecked")
  private Captured capturePost(Object mockResult, ServiceCall call) throws Exception {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    AccessKeyService svc = ManagementServiceBuilder.buildServices(client).getAccessKeyService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResult).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    Captured captured = new Captured();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      captured.response = call.call(svc);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), any());
    captured.uri = uriCaptor.getValue();
    captured.body = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).valueToTree(bodyCaptor.getValue());
    captured.authHeader = authHeader.get();
    return captured;
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
