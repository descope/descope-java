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

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.AccessKeyResponseDetails;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AccessKeyService;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AccessKeyServiceImplTest {
  private final List<String> mockRoles = List.of("Test");
  private final AssociatedTenant associatedTenant = new AssociatedTenant("test", mockRoles);
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
    var authParams = TestMgmtUtils.getManagementParams();
    var client = TestMgmtUtils.getClient();
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockAccessResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      accessKeyService.delete("Test");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testFunctionalFullCycle() {
    String name = TestMgmtUtils.getRandomName("ak-");
    var createResult = accessKeyService.create(name, 0, null, null);
    Assertions.assertThat(createResult).isNotNull();
    Assertions.assertThat(createResult.getCleartext()).isNotBlank();
    Assertions.assertThat(createResult.getKey()).isNotNull();
    Assertions.assertThat(createResult.getKey().getId()).isNotBlank();
    Assertions.assertThat(createResult.getKey().getName()).isEqualTo(name);
    Assertions.assertThat(createResult.getKey().getStatus()).isEqualTo("active");
    Assertions.assertThat(createResult.getKey().getCreatedBy()).isNotBlank();
    var loadResult = accessKeyService.load(createResult.getKey().getId());
    Assertions.assertThat(loadResult).isNotNull();
    Assertions.assertThat(loadResult.getKey()).isNotNull();
    Assertions.assertThat(loadResult.getKey().getId()).isNotBlank();
    Assertions.assertThat(loadResult.getKey().getName()).isEqualTo(name);
    Assertions.assertThat(loadResult.getKey().getStatus()).isEqualTo("active");
    Assertions.assertThat(loadResult.getKey().getCreatedBy()).isNotBlank();
    var searchResult = accessKeyService.searchAll(null);
    Assertions.assertThat(searchResult).isNotNull();
    Assertions.assertThat(searchResult.getKeys()).isNotEmpty();
    accessKeyService.deactivate(createResult.getKey().getId());
    var deactivateResult = accessKeyService.load(createResult.getKey().getId());
    Assertions.assertThat(deactivateResult).isNotNull();
    Assertions.assertThat(deactivateResult.getKey()).isNotNull();
    Assertions.assertThat(deactivateResult.getKey().getId()).isNotBlank();
    Assertions.assertThat(deactivateResult.getKey().getName()).isEqualTo(name);
    Assertions.assertThat(deactivateResult.getKey().getStatus()).isEqualTo("inactive");
    Assertions.assertThat(deactivateResult.getKey().getCreatedBy()).isNotBlank();
    assertDoesNotThrow(() -> accessKeyService.delete(createResult.getKey().getId()));
  }
}
