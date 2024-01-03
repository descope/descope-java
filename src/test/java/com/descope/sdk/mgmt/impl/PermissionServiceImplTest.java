package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.descope.model.client.Client;
import com.descope.model.permission.Permission;
import com.descope.model.permission.PermissionResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.PermissionService;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

class PermissionServiceImplTest {

  private final Permission mockPermission =
      Permission.builder().name("someName").description("someDesc").build();
  private final List<Permission> mockPermissionList = Arrays.asList(mockPermission);
  private final PermissionResponse permissionResponse = new PermissionResponse(mockPermissionList);
  private PermissionService permissionService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.permissionService =
        ManagementServiceBuilder.buildServices(client).getPermissionService();
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      permissionService.delete("someName");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadAllForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(permissionResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      PermissionResponse response = permissionService.loadAll();
      Assertions.assertThat(response.getPermissions().size()).isEqualTo(1);
      Assertions.assertThat(response.getPermissions().get(0).getName()).isEqualTo("someName");
      Assertions.assertThat(response.getPermissions().get(0).getDescription())
          .isEqualTo("someDesc");
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String p = TestUtils.getRandomName("p-").substring(0, 20);
    permissionService.create(p, "ttt");
    PermissionResponse permissions = permissionService.loadAll();
    assertThat(permissions.getPermissions()).isNotEmpty();
    boolean found = false;
    for (Permission perm : permissions.getPermissions()) {
      if (perm.getName().equals(p)) {
        found = true;
        assertEquals("ttt", perm.getDescription());
      }
    }
    assertTrue(found);
    permissionService.update(p, p + "1", "zzz");
    permissions = permissionService.loadAll();
    assertThat(permissions.getPermissions()).isNotEmpty();
    found = false;
    for (Permission perm : permissions.getPermissions()) {
      if (perm.getName().equals(p + "1")) {
        found = true;
        assertEquals("zzz", perm.getDescription());
      }
    }
    permissionService.delete(p + "1");
  }
}
