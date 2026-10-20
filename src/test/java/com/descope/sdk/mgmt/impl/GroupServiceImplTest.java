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

  // The service deserializes the response into List<Group>. Mocking the proxy proves nothing about
  // that step, so decode a literal server body through the same mapper configuration: a raw List
  // would hand back LinkedHashMaps, and the member fields only bind under the server's own names.
  @Test
  void testGroupResponseDeserialization() throws Exception {
    String body = "[{\"id\":\"g1\",\"display\":\"Engineering\",\"source\":\"scim\","
        + "\"members\":[{\"loginId\":\"alice@example.com\",\"userId\":\"U1\","
        + "\"display\":\"Alice\",\"source\":\"jit\"}]}]";
    ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    List<Group> parsed = objectMapper.readValue(body, new TypeReference<List<Group>>() {});

    assertEquals(1, parsed.size());
    Group group = parsed.get(0);
    assertEquals("g1", group.getId());
    assertEquals("scim", group.getSource());
    assertEquals(1, group.getMembers().size());
    GroupMember member = group.getMembers().get(0);
    assertEquals("alice@example.com", member.getLoginID());
    assertEquals("U1", member.getUserID());
    // a JIT member inside a SCIM-owned group is the case this field exists for
    assertEquals("jit", member.getSource());
  }

  // The three-field constructors stay so callers written against the previous shape keep compiling.
  @Test
  void testLegacyConstructorsStillCompile() {
    GroupMember member = new GroupMember("login", "user", "display");
    assertEquals("login", member.getLoginID());
    Group group = new Group("id", "display", List.of(member));
    assertEquals("id", group.getId());
  }
}
