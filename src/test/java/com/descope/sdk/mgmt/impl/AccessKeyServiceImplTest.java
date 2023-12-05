package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseDetails;
import com.descope.model.mgmt.AccessKeyResponseList;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AccessKeyService;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

class AccessKeyServiceImplTest {
  private final List<String> mockRoles = List.of("Test");
  private final AssociatedTenant associatedTenant = new AssociatedTenant("test", "", mockRoles);
  private final List<AssociatedTenant> mockKeyTenants = List.of(associatedTenant);
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
    ManagementParams authParams = TestUtils.getManagementParams();
    Client client = TestUtils.getClient();
    this.accessKeyService =
        ManagementServiceBuilder.buildServices(client, authParams).getAccessKeyService();
  }

  @Test
  void testCreateForEmptyName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> accessKeyService.create("", 10, mockRoles, mockKeyTenants));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
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
    assertEquals("The Name argument is invalid", thrown.getMessage());
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
    assertEquals("The Id argument is invalid", thrown.getMessage());
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
