package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.group.Group;
import com.descope.model.group.GroupMember;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.GroupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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
    assertEquals("default_ssoid", group.getSsoId());
  }

  @Test
  void testLoadAllGroupsWithSsoId() throws Exception {
    ClassicHttpRequest request =
        sendRequest(gs -> assertGroups(gs.loadAllGroups("someTenantId", "sso-1")));
    Map<?, ?> body = assertRequest(request, "/v1/mgmt/group/all");
    assertEquals("someTenantId", body.get("tenantId"));
    assertEquals("sso-1", body.get("ssoId"));
  }

  @Test
  void testLoadAllGroupsWithoutSsoIdOmitsIt() throws Exception {
    ClassicHttpRequest request = sendRequest(gs -> gs.loadAllGroups("someTenantId"));
    assertFalse(assertRequest(request, "/v1/mgmt/group/all").containsKey("ssoId"));
    request = sendRequest(gs -> gs.loadAllGroups("someTenantId", ""));
    assertFalse(assertRequest(request, "/v1/mgmt/group/all").containsKey("ssoId"));
  }

  @Test
  void testLoadAllGroupsWithSsoIdForEmptyTenantID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> groupService.loadAllGroups("", "sso-1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupsForMembersWithSsoId() throws Exception {
    ClassicHttpRequest request = sendRequest(gs -> assertGroups(gs.loadAllGroupsForMembers(
        "someTenantId", Arrays.asList("user1"), Arrays.asList("loginId1"), "sso-1")));
    Map<?, ?> body = assertRequest(request, "/v1/mgmt/group/member/all");
    assertEquals("someTenantId", body.get("tenantId"));
    assertEquals(Arrays.asList("user1"), body.get("userIds"));
    assertEquals(Arrays.asList("loginId1"), body.get("loginIds"));
    assertEquals("sso-1", body.get("ssoId"));
  }

  @Test
  void testLoadAllGroupsForMembersWithoutSsoIdOmitsIt() throws Exception {
    ClassicHttpRequest request = sendRequest(gs -> gs.loadAllGroupsForMembers(
        "someTenantId", Arrays.asList("user1"), Collections.emptyList()));
    assertFalse(assertRequest(request, "/v1/mgmt/group/member/all").containsKey("ssoId"));
  }

  @Test
  void testLoadAllGroupsForMembersWithSsoIdForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> groupService.loadAllGroupsForMembers("", Arrays.asList("user1"), Arrays.asList("l1"), "sso-1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> groupService.loadAllGroupsForMembers(
        "someTenantId", new ArrayList<>(), new ArrayList<>(), "sso-1"));
    assertEquals("The userIDs and loginIDs argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllGroupMembersWithSsoId() throws Exception {
    ClassicHttpRequest request =
        sendRequest(gs -> assertGroups(gs.loadAllGroupMembers("someTenantId", "groupId", "sso-1")));
    Map<?, ?> body = assertRequest(request, "/v1/mgmt/group/members");
    assertEquals("someTenantId", body.get("tenantId"));
    assertEquals("groupId", body.get("groupId"));
    assertEquals("sso-1", body.get("ssoId"));
  }

  @Test
  void testLoadAllGroupMembersWithoutSsoIdOmitsIt() throws Exception {
    ClassicHttpRequest request = sendRequest(gs -> gs.loadAllGroupMembers("someTenantId", "groupId"));
    assertFalse(assertRequest(request, "/v1/mgmt/group/members").containsKey("ssoId"));
  }

  @Test
  void testLoadAllGroupMembersWithSsoIdForInvalidArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> groupService.loadAllGroupMembers("", "groupId", "sso-1"));
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> groupService.loadAllGroupMembers("someTenantId", "", "sso-1"));
    assertEquals("The GroupID argument is invalid", thrown.getMessage());
  }

  private interface GroupCall {
    void run(GroupService gs);
  }

  // Runs the call through a mocked HTTP client answering GROUPS_JSON and returns the sent request
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static ClassicHttpRequest sendRequest(GroupCall call) throws IOException {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("test-key")
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    GroupService gs = ManagementServiceBuilder.buildServices(client).getGroupService();
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream(GROUPS_JSON.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      call.run(gs);
    }
    return captor.getValue();
  }

  private static Map<?, ?> assertRequest(ClassicHttpRequest request, String path) throws Exception {
    assertEquals("POST", request.getMethod());
    Assertions.assertThat(request.getRequestUri()).endsWith(path);
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", request.getHeader("Authorization").getValue());
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }
}
