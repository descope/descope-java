package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.group.Group;
import com.descope.model.group.GroupMember;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.GroupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class GroupServiceImplTest {

  private final Group mockGroup = Mockito.mock(Group.class);
  private final List<Group> groups = Arrays.asList(mockGroup);
  private GroupService groupService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.groupService =
        ManagementServiceBuilder.buildServices(client).getGroupService();
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
    doReturn(groups).when(apiProxy).postAndGetArray(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Group> response = groupService.loadAllGroups("someTenantId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadAllGroupsForMembersForEmptyTenantID() {
    List<String> userIds = Arrays.asList("user1", "user2");
    List<String> loginIds = Arrays.asList("loginId1", "loginId2");
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
    List<String> userIds = Arrays.asList("user1", "user2");
    List<String> loginIds = Arrays.asList("loginId1", "loginId2");
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(groups).when(apiProxy).postAndGetArray(any(), any(), any());
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
    doReturn(groups).when(apiProxy).postAndGetArray(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Group> response = groupService.loadAllGroupMembers("someTenantId", "groupId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadAllGroupsReturnsGroups() {
    assertGroups(loadFromServerJson(() -> groupService.loadAllGroups("someTenantId")));
  }

  @Test
  void testLoadAllGroupsForMembersReturnsGroups() {
    assertGroups(loadFromServerJson(() -> groupService.loadAllGroupsForMembers(
        "someTenantId", Arrays.asList("user1"), Arrays.asList("loginId1"))));
  }

  @Test
  void testLoadAllGroupMembersReturnsGroups() {
    assertGroups(
        loadFromServerJson(() -> groupService.loadAllGroupMembers("someTenantId", "groupId")));
  }

  // The endpoints return a bare JSON array of groups
  private static final String GROUPS_JSON = "[{\"id\":\"g1\",\"display\":\"Group 1\","
      + "\"source\":\"scim\",\"ssoId\":\"default_ssoid\","
      + "\"members\":[{\"loginId\":\"l1\",\"userId\":\"u1\",\"display\":\"User 1\"}]}]";

  private List<Group> loadFromServerJson(Supplier<List<Group>> call) {
    // Deserialize like the real proxy does, for whichever proxy method is called
    ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doAnswer(inv -> objectMapper.readValue(GROUPS_JSON, (Class<?>) inv.getArgument(2)))
        .when(apiProxy).post(any(), any(), any());
    doAnswer(inv -> objectMapper.readValue(GROUPS_JSON, (TypeReference<?>) inv.getArgument(2)))
        .when(apiProxy).postAndGetArray(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      return call.get();
    }
  }

  private static void assertGroups(List<Group> response) {
    Assertions.assertThat(response).hasSize(1);
    Group group = response.get(0);
    assertEquals("g1", group.getId());
    assertEquals("Group 1", group.getDisplay());
    Assertions.assertThat(group.getMembers()).hasSize(1);
    GroupMember member = group.getMembers().get(0);
    assertEquals("l1", member.getLoginID());
    assertEquals("u1", member.getUserID());
    assertEquals("User 1", member.getDisplay());
  }
}
