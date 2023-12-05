package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.group.Group;
import com.descope.model.mgmt.ManagementParams;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.GroupService;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class GroupServiceImplTest {

  private final Group mockGroup = Mockito.mock(Group.class);
  private final List<Group> groups = List.of(mockGroup);
  private GroupService groupService;

  @BeforeEach
  void setUp() {
    ManagementParams authParams = TestUtils.getManagementParams();
    Client client = TestUtils.getClient();
    this.groupService =
        ManagementServiceBuilder.buildServices(client, authParams).getGroupService();
  }

  @Test
  void testLoadAllGroupsForEmptyTenantID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> groupService.loadAllGroups(""));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(groups).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Group> response = groupService.loadAllGroups("someTenantId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadAllGroupsForMembersForEmptyTenantID() {
    List<String> userIds = List.of("user1", "user2");
    List<String> loginIds = List.of("loginId1", "loginId2");
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> groupService.loadAllGroupsForMembers("", userIds, loginIds));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsForMembersForEmptyUserIds() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                groupService.loadAllGroupsForMembers(
                    "someTenantId", new ArrayList<>(), new ArrayList<>()));
    assertNotNull(thrown);
    assertEquals("The userIDs and loginIDs argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsForMembersForSuccess() {
    List<String> userIds = List.of("user1", "user2");
    List<String> loginIds = List.of("loginId1", "loginId2");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(groups).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Group> response = groupService.loadAllGroupsForMembers("someTenantId", userIds, loginIds);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadAllGroupMembersForEmptyTenantID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> groupService.loadAllGroupMembers("", "groupId"));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupMembersForEmptyGroupId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> groupService.loadAllGroupMembers("someTenantId", ""));
    assertNotNull(thrown);
    assertEquals("The GroupID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupMembers() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(groups).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Group> response = groupService.loadAllGroupMembers("someTenantId", "groupId");
      Assertions.assertThat(response).isNotNull();
    }
  }
}
