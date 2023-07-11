package com.descope.sdk.impl;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;
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
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.roles.Role;
import com.descope.model.roles.RoleResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class RolesServiceImplTest {

  private final List<String> mockPermissionNames = List.of("permission1", "permission2");
  private final List<Role> mockRole =
      List.of(
          Role.builder()
              .name("someName")
              .permissionNames(mockPermissionNames)
              .description("someDesc")
              .createdTime(1245667L)
              .build());
  private final RoleResponse mockRoleResponse = new RoleResponse(mockRole);
  private RolesService rolesService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.rolesService =
        ManagementServiceBuilder.buildServices(client, authParams).getRolesService();
  }

  @Test
  void testRolesForEmptyName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> rolesService.create("", "someDesc", mockPermissionNames));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testRolesCreateSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      rolesService.create("krishna", "", mockPermissionNames);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> rolesService.update("", "", "", mockPermissionNames));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      rolesService.update("Test", "name", "10", mockPermissionNames);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyNewName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> rolesService.update("krishna", "", "", mockPermissionNames));
    assertNotNull(thrown);
    assertEquals("The NewName argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForEmptyName() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> rolesService.delete(""));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      rolesService.delete("someName");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadAllForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockRoleResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      RoleResponse response = rolesService.loadAll();
      Assertions.assertThat(response.getRoles().size()).isEqualTo(1);
      Assertions.assertThat(response.getRoles().get(0).getName()).isEqualTo("someName");
      Assertions.assertThat(response.getRoles().get(0).getDescription()).isEqualTo("someDesc");
      Assertions.assertThat(response.getRoles().get(0).getPermissionNames().size()).isEqualTo(2);
      Assertions.assertThat(response.getRoles().get(0).getPermissionNames().get(0))
          .isEqualTo("permission1");
      Assertions.assertThat(response.getRoles().get(0).getPermissionNames().get(1))
          .isEqualTo("permission2");
    }
  }
}
