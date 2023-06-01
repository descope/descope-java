package com.descope.sdk.impl;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AssociatedTenant;
import com.descope.model.client.Client;
import com.descope.model.mgmt.AccessKeyResponse;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AccessKeyService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

// TODO - need to do functional testing
class AccessKeyServiceImplTest {
  private final List<String> mockRoles = List.of("Test");
  private final AssociatedTenant associatedTenant = new AssociatedTenant("test", mockRoles);
  private final List<AssociatedTenant> mockKeyTenants = List.of(associatedTenant);
  private final AccessKeyResponse mockResponse =
      AccessKeyResponse.builder()
          .keyTenants(mockKeyTenants)
          .name("name")
          .roleNames(mockRoles)
          .id("id")
          .createdBy("TestUSer")
          .createdTime(123456789023L)
          .build();
  private AccessKeyService accessKeyService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
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
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.create("Test", 10, mockRoles, mockKeyTenants);
      Assertions.assertThat(response.getId()).isNotBlank();
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
    doReturn(mockResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.load("Id");
      Assertions.assertThat(response.getId()).isNotBlank();
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
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.update("Test", "name");
      Assertions.assertThat(response.getId()).isNotBlank();
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
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.deactivate("Test");
      Assertions.assertThat(response.getId()).isNotBlank();
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
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      AccessKeyResponse response = accessKeyService.activate("Test");
      Assertions.assertThat(response.getId()).isNotBlank();
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
      AccessKeyResponse response = accessKeyService.delete("Test");
      Assertions.assertThat(response.getId()).isNotBlank();
    }
  }
}
