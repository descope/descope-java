package com.descope.sdk.impl;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.permission.Permission;
import com.descope.model.permission.PermissionResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.PermissionService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PermissionServiceImplTest {

  private final Permission mockPermission =
      Permission.builder().name("someName").description("somneDesc").build();
  private final List<Permission> mockPermissionList = List.of(mockPermission);
  private final PermissionResponse permissionResponse = new PermissionResponse(mockPermissionList);
  private PermissionService permissionService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.permissionService =
        ManagementServiceBuilder.buildServices(client, authParams).getPermissionService();
  }

  @Test
  void testPermissionForEmptyName() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> permissionService.create("", ""));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testPermissionCreateSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      permissionService.create("someName", "someDesc");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyName() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> permissionService.update("", "", ""));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      permissionService.update("Test", "name", "10");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyNewName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> permissionService.update("krishna", "", ""));
    assertNotNull(thrown);
    assertEquals("The NewName argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForEmptyName() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> permissionService.delete(""));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      permissionService.delete("someName");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadAllForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(permissionResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      PermissionResponse response = permissionService.loadAll();
      Assertions.assertThat(response.getPermissions().size()).isEqualTo(1);
      Assertions.assertThat(response.getPermissions().get(0).getName()).isEqualTo("someName");
      Assertions.assertThat(response.getPermissions().get(0).getDescription())
          .isEqualTo("somneDesc");
    }
  }
}
