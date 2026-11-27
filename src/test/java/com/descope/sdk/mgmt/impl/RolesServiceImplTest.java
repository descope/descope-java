package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.mgmt.SortField;
import com.descope.model.roles.Role;
import com.descope.model.roles.RoleResponse;
import com.descope.model.roles.RoleSearchOptions;
import com.descope.model.roles.RoleUpdateRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.PermissionService;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.TenantService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

class RolesServiceImplTest {

  private final List<String> mockPermissionNames = Arrays.asList("permission1", "permission2");
  private final List<Role> mockRole = Arrays.asList(Role.builder().name("someName").permissionNames(mockPermissionNames)
      .description("someDesc").createdTime(1245667L).build());
  private final RoleResponse mockRoleResponse = new RoleResponse(mockRole);
  private RolesService rolesService;
  private PermissionService permissionService;
  private TenantService tenantService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.rolesService = mgmtServices.getRolesService();
    this.permissionService = mgmtServices.getPermissionService();
    this.tenantService = mgmtServices.getTenantService();
  }

  @Test
  void testRolesForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.create("", "someDesc", mockPermissionNames));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testRolesCreateSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      rolesService.create("krishna", "", mockPermissionNames);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.update("", "", "", mockPermissionNames));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      rolesService.update("Test", "name", "10", mockPermissionNames);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyNewName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.update("krishna", "", "", mockPermissionNames));
    assertNotNull(thrown);
    assertEquals("The NewName argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> rolesService.delete(""));
    assertNotNull(thrown);
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      rolesService.delete("someName");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadAllForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockRoleResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      RoleResponse response = rolesService.loadAll();
      Assertions.assertThat(response.getRoles().size()).isEqualTo(1);
      Assertions.assertThat(response.getRoles().get(0).getName()).isEqualTo("someName");
      Assertions.assertThat(response.getRoles().get(0).getDescription()).isEqualTo("someDesc");
      Assertions.assertThat(response.getRoles().get(0).getPermissionNames().size()).isEqualTo(2);
      Assertions.assertThat(response.getRoles().get(0).getPermissionNames().get(0)).isEqualTo("permission1");
      Assertions.assertThat(response.getRoles().get(0).getPermissionNames().get(1)).isEqualTo("permission2");
    }
  }

  @Test
  void testCreateBatchForEmpty() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> rolesService.createBatch(null));
    assertNotNull(thrown);
    assertEquals("The roles argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateBatchForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockRoleResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      RoleResponse response = rolesService.createBatch(mockRole);
      assertThat(response.getRoles()).hasSize(1);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateBatchForEmpty() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> rolesService.updateBatch(null));
    assertNotNull(thrown);
    assertEquals("The roles argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateBatchForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockRoleResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      RoleResponse response = rolesService.updateBatch(Arrays.asList(
          RoleUpdateRequest.builder().name("someName").newName("newName").build()));
      assertThat(response.getRoles()).hasSize(1);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testDeleteBatchForEmpty() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.deleteBatch(null, "", null));
    assertNotNull(thrown);
    assertEquals("The roleNames argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteBatchForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      rolesService.deleteBatch(Arrays.asList("someName"), "", null);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  private static final String MOCK_PROJECT_ID = "P123456789012345678901234567";
  private static final String MOCK_MANAGEMENT_KEY = "some-management-key";
  private static final String ROLE_JSON = "{\"name\":\"r1\",\"description\":\"d1\",\"permissionNames\":[\"p1\"],"
      + "\"createdTime\":1700000000,\"tenantId\":\"t1\",\"default\":true,\"private\":true,\"id\":\"ROL1\","
      + "\"unknownField\":\"x\"}";
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  /** What a call sent through the proxy, and what it returned. */
  private static class Sent {
    String authHeader;
    URI uri;
    JsonNode body;
    Object result;
  }

  @SuppressWarnings("unchecked")
  private static Sent send(String responseJson, Function<RolesService, Object> call) throws Exception {
    Client client = Client.builder().uri("https://api.descope.com").projectId(MOCK_PROJECT_ID)
        .managementKey(MOCK_MANAGEMENT_KEY).sdkInfo(SdkInfo.builder().name("java").build()).build();
    RolesService service = ManagementServiceBuilder.buildServices(client).getRolesService();
    Sent sent = new Sent();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doAnswer(inv -> {
      sent.uri = inv.getArgument(0);
      sent.body = MAPPER.valueToTree(inv.getArgument(1));
      return MAPPER.readValue(responseJson, (Class<?>) inv.getArgument(2));
    }).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.authHeader = ((Supplier<String>) inv.getArgument(0)).get();
        return apiProxy;
      });
      sent.result = call.apply(service);
    }
    return sent;
  }

  private static void assertFullRole(Role role) {
    assertThat(role.getName()).isEqualTo("r1");
    assertThat(role.getDescription()).isEqualTo("d1");
    assertThat(role.getPermissionNames()).containsExactly("p1");
    assertThat(role.getCreatedTime()).isEqualTo(1700000000L);
    assertThat(role.getTenantId()).isEqualTo("t1");
    assertThat(role.getDefaultRole()).isTrue();
    assertThat(role.getPrivateRole()).isTrue();
    assertThat(role.getId()).isEqualTo("ROL1");
  }

  @Test
  void testCreateWithFlagsForSuccess() throws Exception {
    Sent sent = send(ROLE_JSON,
        rs -> rs.create("r1", "t1", "d1", Arrays.asList("p1"), true, false));
    assertThat(sent.authHeader).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
    assertThat(sent.uri.getPath()).isEqualTo("/v1/mgmt/role/create");
    assertThat(sent.body).isEqualTo(MAPPER.readTree("{\"name\":\"r1\",\"tenantId\":\"t1\",\"description\":\"d1\","
        + "\"permissionNames\":[\"p1\"],\"default\":true,\"private\":false}"));
    assertFullRole((Role) sent.result);
  }

  @Test
  void testCreateWithFlagsOmitsUnset() throws Exception {
    Sent sent = send("{}", rs -> rs.create("r1", null, null, null, null, null));
    assertThat(sent.body).isEqualTo(MAPPER.readTree("{\"name\":\"r1\"}"));
  }

  @Test
  void testCreateWithFlagsForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.create("", "t1", "d1", mockPermissionNames, true, true));
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateWithFlagsForSuccess() throws Exception {
    Sent sent = send(ROLE_JSON,
        rs -> rs.update("r0", "t1", "r1", "d1", Arrays.asList("p1"), false, true));
    assertThat(sent.authHeader).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
    assertThat(sent.uri.getPath()).isEqualTo("/v1/mgmt/role/update");
    assertThat(sent.body).isEqualTo(MAPPER.readTree("{\"name\":\"r0\",\"newName\":\"r1\",\"tenantId\":\"t1\","
        + "\"description\":\"d1\",\"permissionNames\":[\"p1\"],\"default\":false,\"private\":true}"));
    assertFullRole((Role) sent.result);
  }

  @Test
  void testUpdateWithFlagsForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.update("", "t1", "r1", "d1", mockPermissionNames, true, true));
    assertEquals("The Name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateWithFlagsForEmptyNewName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.update("r0", "t1", "", "d1", mockPermissionNames, true, true));
    assertEquals("The NewName argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateWithIdForSuccess() throws Exception {
    Sent sent = send(ROLE_JSON,
        rs -> rs.updateWithId("ROL1", "t1", "r1", "d1", Arrays.asList("p1"), true, true));
    assertThat(sent.authHeader).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
    assertThat(sent.uri.getPath()).isEqualTo("/v1/mgmt/role/update");
    assertThat(sent.body).isEqualTo(MAPPER.readTree("{\"id\":\"ROL1\",\"newName\":\"r1\",\"tenantId\":\"t1\","
        + "\"description\":\"d1\",\"permissionNames\":[\"p1\"],\"default\":true,\"private\":true}"));
    assertFullRole((Role) sent.result);
  }

  @Test
  void testUpdateWithIdForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.updateWithId("", "t1", "r1", "d1", mockPermissionNames, true, true));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateWithIdForEmptyNewName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> rolesService.updateWithId("ROL1", "t1", "", "d1", mockPermissionNames, true, true));
    assertEquals("The NewName argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchWithNewOptionsForSuccess() throws Exception {
    RoleSearchOptions options = RoleSearchOptions.builder()
        .tenantIds(Arrays.asList("t1"))
        .text("r")
        .sort(Arrays.asList(SortField.builder().field("createdTime").desc(true).build()))
        .includeProjectRoles(false)
        .roleIds(Arrays.asList("ROL1"))
        .build();
    Sent sent = send("{\"roles\":[" + ROLE_JSON + "]}", rs -> rs.search(options));
    assertThat(sent.authHeader).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
    assertThat(sent.uri.getPath()).isEqualTo("/v1/mgmt/role/search");
    assertThat(sent.body).isEqualTo(MAPPER.readTree("{\"tenantIds\":[\"t1\"],\"text\":\"r\","
        + "\"sort\":[{\"field\":\"createdTime\",\"desc\":true}],\"includeProjectRoles\":false,"
        + "\"roleIds\":[\"ROL1\"]}"));
    RoleResponse response = (RoleResponse) sent.result;
    assertThat(response.getRoles()).hasSize(1);
    assertFullRole(response.getRoles().get(0));
  }

  @Test
  void testCreateBatchNeverSendsRoleId() throws Exception {
    Role role = Role.builder().name("r1").id("ROL1").defaultRole(true).privateRole(false).build();
    Sent sent = send("{\"roles\":[]}", rs -> rs.createBatch(Arrays.asList(role)));
    assertThat(sent.uri.getPath()).isEqualTo("/v1/mgmt/role/create/batch");
    assertThat(sent.body).isEqualTo(
        MAPPER.readTree("{\"roles\":[{\"name\":\"r1\",\"default\":true,\"private\":false}]}"));
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
    roles = rolesService.search(RoleSearchOptions.builder().roleNames(Arrays.asList(r1 + "1")).build());
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

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleWithTenantId() {
    String p1 = TestUtils.getRandomName("pt-").substring(0, 20);
    String p2 = TestUtils.getRandomName("pt-").substring(0, 20);
    String r1 = TestUtils.getRandomName("rt-").substring(0, 20);
    permissionService.create(p1, "p1");
    permissionService.create(p2, "p2");
    String tid = tenantService.create(TestUtils.getRandomName("tnfr-").substring(0, 20), null);
    rolesService.create(r1, tid, "ttt", Arrays.asList(p1, p2));
    RoleResponse roles = rolesService.loadAll();
    assertThat(roles.getRoles()).isNotEmpty();
    boolean found = false;
    for (Role r : roles.getRoles()) {
      if (r.getName().equals(r1)) {
        found = true;
        assertEquals("ttt", r.getDescription());
        assertEquals(tid, r.getTenantId());
        assertThat(r.getPermissionNames()).contains(p1, p2);
      }
    }
    assertTrue(found);
    rolesService.update(r1, tid, r1 + "1", "zzz", Arrays.asList(p1));
    roles = rolesService.search(RoleSearchOptions.builder().tenantIds(Arrays.asList(tid)).build());
    assertThat(roles.getRoles()).isNotEmpty();
    found = false;
    for (Role r : roles.getRoles()) {
      if (r.getName().equals(r1 + "1")) {
        found = true;
        assertEquals("zzz", r.getDescription());
        assertEquals(tid, r.getTenantId());
        assertThat(r.getPermissionNames()).containsExactly(p1);
      }
    }
    assertTrue(found);
    permissionService.delete(p1);
    permissionService.delete(p2);
    rolesService.delete(r1 + "1", tid);
    found = false;
    roles = rolesService.loadAll();
    for (Role r : roles.getRoles()) {
      if (r.getName().equals(r1 + "1") && r.getTenantId().equals(tid)) {
        found = true;
      }
    }
    assertFalse(found);
    tenantService.delete(tid);
  }
}
