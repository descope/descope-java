package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.list.DescopeList;
import com.descope.model.list.ListCheckResponse;
import com.descope.model.list.ListRequest;
import com.descope.model.list.ListResponse;
import com.descope.model.list.ListType;
import com.descope.model.list.ListsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.ListService;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ListServiceImplTest {

  private static final String BASE_URI = "https://api.descope.com";
  private static final String MANAGEMENT_KEY = "test-key";
  private static final DescopeList MOCK_LIST = DescopeList.builder()
      .id("listId")
      .name("Blocked IPs")
      .description("desc")
      .type(ListType.IPS)
      .data(Arrays.asList("1.1.1.1", "10.0.0.0/8"))
      .build();

  private ListService listService;
  private ApiProxy apiProxy;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri(BASE_URI)
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    listService = new ListServiceImpl(client);
    apiProxy = mock(ApiProxy.class);
  }

  @Test
  void testCreateSuccess() {
    doReturn(new ListResponse(MOCK_LIST)).when(apiProxy).post(any(), any(), any());
    ListRequest request = ListRequest.builder()
        .name("Blocked IPs")
        .description("desc")
        .type(ListType.IPS)
        .data(Arrays.asList("1.1.1.1"))
        .build();
    AtomicReference<DescopeList> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.create(request)));
    assertAuth(auth);
    assertSame(MOCK_LIST, result.get());
    verify(apiProxy).post(eq(uri("/v1/mgmt/list")), eq(request), eq(ListResponse.class));
  }

  @Test
  void testUpdateSuccess() {
    doReturn(new ListResponse(MOCK_LIST)).when(apiProxy).post(any(), any(), any());
    ListRequest request = ListRequest.builder()
        .name("Blocked IPs")
        .description("desc")
        .type(ListType.IPS)
        .data(Arrays.asList("1.1.1.1"))
        .build();
    AtomicReference<DescopeList> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.update("listId", request)));
    assertAuth(auth);
    assertSame(MOCK_LIST, result.get());
    Map<String, Object> body = capturePost("/v1/mgmt/list/update", ListResponse.class);
    assertEquals(mapOf("id", "listId", "name", "Blocked IPs", "description", "desc", "type", ListType.IPS,
        "data", Arrays.asList("1.1.1.1")), body);
  }

  @Test
  void testUpdateOmitsUnsetFields() {
    doReturn(new ListResponse(MOCK_LIST)).when(apiProxy).post(any(), any(), any());
    call(() -> listService.update("listId", ListRequest.builder().description("new").build()));
    Map<String, Object> body = capturePost("/v1/mgmt/list/update", ListResponse.class);
    assertEquals(mapOf("id", "listId", "description", "new"), body);
  }

  @Test
  void testDeleteSuccess() {
    String auth = call(() -> listService.delete("listId"));
    assertAuth(auth);
    assertEquals(mapOf("id", "listId"), capturePost("/v1/mgmt/list/delete", Void.class));
  }

  @Test
  void testLoadSuccess() {
    doReturn(new ListResponse(MOCK_LIST)).when(apiProxy).get(any(), any());
    AtomicReference<DescopeList> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.load("listId")));
    assertAuth(auth);
    assertSame(MOCK_LIST, result.get());
    verify(apiProxy).get(eq(uri("/v1/mgmt/list/listId")), eq(ListResponse.class));
  }

  @Test
  void testLoadByNameEscapesName() {
    doReturn(new ListResponse(MOCK_LIST)).when(apiProxy).get(any(), any());
    AtomicReference<DescopeList> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.loadByName("Blocked IPs/v2")));
    assertAuth(auth);
    assertSame(MOCK_LIST, result.get());
    ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(captor.capture(), eq(ListResponse.class));
    assertEquals("/v1/mgmt/list/name/Blocked%20IPs%2Fv2", captor.getValue().getRawPath());
  }

  @Test
  void testLoadAllSuccess() {
    doReturn(new ListsResponse(Arrays.asList(MOCK_LIST))).when(apiProxy).get(any(), any());
    AtomicReference<List<DescopeList>> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.loadAll()));
    assertAuth(auth);
    assertEquals(Arrays.asList(MOCK_LIST), result.get());
    verify(apiProxy).get(eq(uri("/v1/mgmt/list/all")), eq(ListsResponse.class));
  }

  @Test
  void testLoadAllEmptyResponse() {
    doReturn(new ListsResponse()).when(apiProxy).get(any(), any());
    AtomicReference<List<DescopeList>> result = new AtomicReference<>();
    call(() -> result.set(listService.loadAll()));
    assertEquals(Collections.emptyList(), result.get());
  }

  @Test
  void testImportSuccess() {
    List<DescopeList> lists = Arrays.asList(MOCK_LIST);
    String auth = call(() -> listService.importLists(lists));
    assertAuth(auth);
    assertEquals(mapOf("lists", lists), capturePost("/v1/mgmt/list/import", Void.class));
  }

  @Test
  void testClearSuccess() {
    String auth = call(() -> listService.clear("listId"));
    assertAuth(auth);
    assertEquals(mapOf("id", "listId"), capturePost("/v1/mgmt/list/clear", Void.class));
  }

  @Test
  void testAddIPsSuccess() {
    List<String> ips = Arrays.asList("1.1.1.1", "10.0.0.0/8");
    String auth = call(() -> listService.addIPs("listId", ips));
    assertAuth(auth);
    assertEquals(mapOf("id", "listId", "ips", ips), capturePost("/v1/mgmt/list/ip/add", Void.class));
  }

  @Test
  void testRemoveIPsSuccess() {
    List<String> ips = Arrays.asList("1.1.1.1");
    String auth = call(() -> listService.removeIPs("listId", ips));
    assertAuth(auth);
    assertEquals(mapOf("id", "listId", "ips", ips), capturePost("/v1/mgmt/list/ip/remove", Void.class));
  }

  @Test
  void testCheckIPSuccess() {
    doReturn(new ListCheckResponse(true)).when(apiProxy).post(any(), any(), any());
    AtomicReference<Boolean> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.checkIP("listId", "1.1.1.1")));
    assertAuth(auth);
    assertTrue(result.get());
    assertEquals(mapOf("id", "listId", "ip", "1.1.1.1"),
        capturePost("/v1/mgmt/list/ip/check", ListCheckResponse.class));
  }

  @Test
  void testCheckIPMissingExists() {
    doReturn(new ListCheckResponse()).when(apiProxy).post(any(), any(), any());
    AtomicReference<Boolean> result = new AtomicReference<>();
    call(() -> result.set(listService.checkIP("listId", "1.1.1.1")));
    assertFalse(result.get());
  }

  @Test
  void testAddTextsSuccess() {
    List<String> texts = Arrays.asList("a", "b");
    String auth = call(() -> listService.addTexts("listId", texts));
    assertAuth(auth);
    assertEquals(mapOf("id", "listId", "texts", texts), capturePost("/v1/mgmt/list/text/add", Void.class));
  }

  @Test
  void testRemoveTextsSuccess() {
    List<String> texts = Arrays.asList("a");
    String auth = call(() -> listService.removeTexts("listId", texts));
    assertAuth(auth);
    assertEquals(mapOf("id", "listId", "texts", texts), capturePost("/v1/mgmt/list/text/remove", Void.class));
  }

  @Test
  void testCheckTextSuccess() {
    doReturn(new ListCheckResponse(true)).when(apiProxy).post(any(), any(), any());
    AtomicReference<Boolean> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.checkText("listId", "a")));
    assertAuth(auth);
    assertTrue(result.get());
    assertEquals(mapOf("id", "listId", "text", "a"),
        capturePost("/v1/mgmt/list/text/check", ListCheckResponse.class));
  }

  @Test
  void testCheckTextsSuccess() {
    doReturn(new ListCheckResponse(false)).when(apiProxy).post(any(), any(), any());
    List<String> texts = Arrays.asList("a", "b");
    AtomicReference<Boolean> result = new AtomicReference<>();
    String auth = call(() -> result.set(listService.checkTexts("listId", texts, false)));
    assertAuth(auth);
    assertFalse(result.get());
    assertEquals(mapOf("id", "listId", "texts", texts, "matchAll", false),
        capturePost("/v1/mgmt/list/text/check", ListCheckResponse.class));
  }

  @Test
  void testCheckTextsMatchAll() {
    doReturn(new ListCheckResponse(true)).when(apiProxy).post(any(), any(), any());
    List<String> texts = Arrays.asList("a", "b");
    AtomicReference<Boolean> result = new AtomicReference<>();
    call(() -> result.set(listService.checkTexts("listId", texts, true)));
    assertTrue(result.get());
    assertEquals(mapOf("id", "listId", "texts", texts, "matchAll", true),
        capturePost("/v1/mgmt/list/text/check", ListCheckResponse.class));
  }

  @Test
  void testInvalidArguments() {
    ListRequest named = ListRequest.builder().name("n").build();
    assertInvalid("request", () -> listService.create(null));
    assertInvalid("request.name", () -> listService.create(ListRequest.builder().build()));
    assertInvalid("id", () -> listService.update("", named));
    assertInvalid("request", () -> listService.update("listId", null));
    assertInvalid("id", () -> listService.delete(""));
    assertInvalid("id", () -> listService.load(""));
    assertInvalid("name", () -> listService.loadByName(""));
    assertInvalid("id", () -> listService.clear(""));
    List<String> values = Arrays.asList("a");
    assertInvalid("id", () -> listService.addIPs("", values));
    assertInvalid("ips", () -> listService.addIPs("listId", null));
    assertInvalid("ips", () -> listService.addIPs("listId", Collections.emptyList()));
    assertInvalid("id", () -> listService.removeIPs("", values));
    assertInvalid("ips", () -> listService.removeIPs("listId", Collections.emptyList()));
    assertInvalid("id", () -> listService.checkIP("", "1.1.1.1"));
    assertInvalid("ip", () -> listService.checkIP("listId", ""));
    assertInvalid("id", () -> listService.addTexts("", values));
    assertInvalid("texts", () -> listService.addTexts("listId", Collections.emptyList()));
    assertInvalid("id", () -> listService.removeTexts("", values));
    assertInvalid("texts", () -> listService.removeTexts("listId", null));
    assertInvalid("id", () -> listService.checkText("", "a"));
    assertInvalid("id", () -> listService.checkTexts("", values, true));
    verify(apiProxy, never()).post(any(), any(), any());
    verify(apiProxy, never()).get(any(), any());
  }

  private void assertInvalid(String arg, Executable executable) {
    call(() -> {
      ServerCommonException thrown = assertThrows(ServerCommonException.class, executable);
      assertEquals("The " + arg + " argument is invalid", thrown.getMessage());
    });
  }

  private static void assertAuth(String auth) {
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY, auth);
  }

  private static URI uri(String path) {
    return URI.create(BASE_URI + path);
  }

  @SuppressWarnings("unchecked")
  private <R> Map<String, Object> capturePost(String path, Class<R> returnClz) {
    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
    verify(apiProxy).post(eq(uri(path)), captor.capture(), eq(returnClz));
    return captor.getValue();
  }

  /** Runs the call with the mocked proxy and returns the Authorization header it built. */
  @SuppressWarnings("unchecked")
  private String call(Runnable runnable) {
    AtomicReference<String> auth = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        auth.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      runnable.run();
    }
    return auth.get();
  }
}
