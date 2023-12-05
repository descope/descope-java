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
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.roles.Role;
import com.descope.model.roles.RoleResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.PermissionService;
import com.descope.sdk.mgmt.RolesService;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

class RolesServiceImplTest {

  private final List<String> mockPermissionNames = Arrays.asList("permission1", "permission2");
  private final List<Role> mockRole =
      Arrays.asList(
          Role.builder()
              .name("someName")
              .permissionNames(mockPermissionNames)
              .description("someDesc")
              .createdTime(1245667L)
              .build());
  private final RoleResponse mockRoleResponse = new RoleResponse(mockRole);
  private RolesService rolesService;
  private PermissionService permissionService;

  @BeforeEach
  void setUp() {
    ManagementParams authParams = TestUtils.getManagementParams();
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
    this.rolesService = mgmtServices.getRolesService();
    this.permissionService = mgmtServices.getPermissionService();
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      rolesService.delete("someName");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadAllForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockRoleResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
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

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String p1 = TestUtils.getRandomName("p-").substring(0, 20);
    String p2 = TestUtils.getRandomName("p-").substring(0, 20);
    String r1 = TestUtils.getRandomName("r-").substring(0, 20);
    permissionService.create(p1, "p1");
    permissionService.create(p2, "p2");
    rolesService.create(r1, "ttt", Arrays.asList(p1, p2));
    RoleResponse roles = rolesService.loadAll();
    assertThat(roles.getRoles()).isNotEmpty();
    boolean found = false;
    for (Role r : roles.getRoles()) {
      if (r.getName().equals(r1)) {
        found = true;
        assertEquals("ttt", r.getDescription());
        assertThat(r.getPermissionNames()).contains(p1, p2);
      }
    }
    assertTrue(found);
    rolesService.update(r1, r1 + "1", "zzz", Arrays.asList(p1));
    roles = rolesService.loadAll();
    assertThat(roles.getRoles()).isNotEmpty();
    found = false;
    for (Role r : roles.getRoles()) {
      if (r.getName().equals(r1 + "1")) {
        found = true;
        assertEquals("zzz", r.getDescription());
        assertThat(r.getPermissionNames()).containsExactly(p1);
      }
    }
    assertTrue(found);
    permissionService.delete(p1);
    permissionService.delete(p2);
    rolesService.delete(r1 + "1");
  }
}
