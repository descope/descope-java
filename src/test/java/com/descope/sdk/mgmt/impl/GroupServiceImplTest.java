package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.group.Group;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.GroupService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

  private static final String GROUPS_JSON =
      "[{\"id\":\"g1\",\"display\":\"Group 1\",\"source\":\"scim\",\"ssoId\":\"sso1\","
          + "\"members\":[{\"loginId\":\"l1\",\"userId\":\"u1\",\"display\":\"User 1\"}]}]";

  @Test
  void testLoadAllGroupsWithSsoIdForEmptyTenantID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> groupService.loadAllGroups("", "sso1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsWithSsoIdForSuccess() throws Exception {
    Map<String, Object> body =
        callAndCapture(
            "{\"groups\":" + GROUPS_JSON + "}",
            "/v1/mgmt/group/all",
            gs -> gs.loadAllGroups("someTenantId", "sso1"));
    Assertions.assertThat(body)
        .containsOnlyKeys("tenantId", "ssoId")
        .containsEntry("tenantId", "someTenantId")
        .containsEntry("ssoId", "sso1");
  }

  @Test
  void testLoadAllGroupsWithBlankSsoIdOmitsIt() throws Exception {
    Map<String, Object> body =
        callAndCapture(
            GROUPS_JSON, "/v1/mgmt/group/all", gs -> gs.loadAllGroups("someTenantId", ""));
    Assertions.assertThat(body).containsOnlyKeys("tenantId");
  }

  @Test
  void testLoadAllGroupsForMembersWithSsoIdForEmptyTenantID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> groupService.loadAllGroupsForMembers(
                "", Arrays.asList("u1"), Arrays.asList("l1"), "sso1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsForMembersWithSsoIdForEmptyIds() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> groupService.loadAllGroupsForMembers("someTenantId", null, null, "sso1"));
    assertEquals("The userIDs and loginIDs argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsForMembersWithSsoIdForSuccess() throws Exception {
    Map<String, Object> body =
        callAndCapture(
            "{\"groups\":" + GROUPS_JSON + "}",
            "/v1/mgmt/group/member/all",
            gs -> gs.loadAllGroupsForMembers(
                "someTenantId", Arrays.asList("u1"), Arrays.asList("l1"), "sso1"));
    Assertions.assertThat(body)
        .containsOnlyKeys("tenantId", "userIds", "loginIds", "ssoId")
        .containsEntry("tenantId", "someTenantId")
        .containsEntry("userIds", Arrays.asList("u1"))
        .containsEntry("loginIds", Arrays.asList("l1"))
        .containsEntry("ssoId", "sso1");
  }

  @Test
  void testLoadAllGroupMembersWithSsoIdForEmptyTenantID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> groupService.loadAllGroupMembers("", "g1", "sso1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupMembersWithSsoIdForEmptyGroupId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> groupService.loadAllGroupMembers("someTenantId", "", "sso1"));
    assertEquals("The GroupID argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupMembersWithSsoIdForSuccess() throws Exception {
    Map<String, Object> body =
        callAndCapture(
            GROUPS_JSON,
            "/v1/mgmt/group/members",
            gs -> gs.loadAllGroupMembers("someTenantId", "g1", "sso1"));
    Assertions.assertThat(body)
        .containsOnlyKeys("tenantId", "groupId", "ssoId")
        .containsEntry("tenantId", "someTenantId")
        .containsEntry("groupId", "g1")
        .containsEntry("ssoId", "sso1");
  }

  /** Checks route, auth header and parsed response; returns the request body. */
  @SuppressWarnings("unchecked")
  private Map<String, Object> callAndCapture(
      String responseJson, String path, Function<GroupService, List<Group>> call) throws Exception {
    Client client = TestUtils.getClient();
    client.setManagementKey("someManagementKey");
    GroupService service = ManagementServiceBuilder.buildServices(client).getGroupService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    JsonNode res = new ObjectMapper().readTree(responseJson);
    doReturn(res).when(apiProxy).post(any(), any(), eq(JsonNode.class));
    AtomicReference<String> authHeader = new AtomicReference<>();
    List<Group> response;
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      response = call.apply(service);
    }
    assertEquals("Bearer " + client.getProjectId() + ":someManagementKey", authHeader.get());
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
    verify(apiProxy).post(uri.capture(), body.capture(), eq(JsonNode.class));
    assertEquals(path, uri.getValue().getPath());
    Assertions.assertThat(response).hasSize(1);
    Group g = response.get(0);
    assertEquals("g1", g.getId());
    assertEquals("Group 1", g.getDisplay());
    assertEquals("scim", g.getSource());
    assertEquals("sso1", g.getSsoId());
    Assertions.assertThat(g.getMembers()).hasSize(1);
    assertEquals("l1", g.getMembers().get(0).getLoginID());
    assertEquals("u1", g.getMembers().get(0).getUserID());
    assertEquals("User 1", g.getMembers().get(0).getDisplay());
    return body.getValue();
  }
}
