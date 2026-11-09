package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowMetadata;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.ManagementFlowOptions;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.model.flow.ThemeResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FlowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

class FlowServiceImplTest {

  private FlowService flowService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.flowService = ManagementServiceBuilder.buildServices(client).getFlowService();
  }

  @Test
  void testExportFlowForEmptyFlowID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.exportFlow(""));
    assertNotNull(thrown);
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testExportFlowForSuccess() {
    FlowResponse flowResponse = mock(FlowResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(flowResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      FlowResponse response = flowService.exportFlow("someFlowID");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testImportFlowForEmptyFlowID() {
    Flow flow = mock(Flow.class);
    Screen screen = mock(Screen.class);
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> flowService.importFlow("", flow, Arrays.asList(screen)));
    assertNotNull(thrown);
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testImportFlowForSuccess() {
    FlowResponse flowResponse = mock(FlowResponse.class);
    Flow flow = mock(Flow.class);
    Screen screen = mock(Screen.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(flowResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      FlowResponse response = flowService.importFlow("someFlowID", flow, Arrays.asList(screen));
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testExportThemeForSuccess() {
    Theme theme = mock(Theme.class);
    ThemeResponse themeResponse = new ThemeResponse(theme);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(themeResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Theme response = flowService.exportTheme();
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testImportThemeForEmptyTheme() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.importTheme(null));
    assertNotNull(thrown);
    assertEquals("The Theme argument is invalid", thrown.getMessage());
  }

  @Test
  void testImportThemeForSuccess() {
    Theme theme = mock(Theme.class);
    ThemeResponse themeResponse = new ThemeResponse(theme);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(themeResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Theme response = flowService.importTheme(theme);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testListFlowsWithIdsForSuccess() throws IOException {
    String body = "{\"flows\":[{\"id\":\"sign-in\",\"name\":\"Sign In\",\"description\":\"desc\",\"disabled\":true,"
        + "\"version\":3,\"dsl\":{\"a\":\"b\"},\"modifiedTime\":\"1700000000000\",\"etag\":\"e1\",\"translate\":true,"
        + "\"translateConnectorID\":\"c1\",\"translateSourceLang\":\"en\",\"translateTargetLangs\":[\"fr\"],"
        + "\"fingerprint\":true,\"widget\":false,\"sharedInteractions\":[{\"id\":\"i1\",\"type\":\"t\","
        + "\"label\":\"l\",\"icon\":\"ic\",\"subType\":\"st\"}],\"management\":true,\"managementConfig\":"
        + "{\"triggers\":{\"enabled\":true,\"triggerType\":\"event\",\"eventTriggers\":"
        + "{\"triggeringEventTypes\":[\"user.created\"]}}},\"referencesGlobalStrings\":true,"
        + "\"clientValidationTracking\":false}],\"total\":1}";
    Recorded rec = new Recorded();
    HttpServer server = serverReturning("/v1/mgmt/flow/list", body, rec);
    try {
      FlowsResponse resp = flowService(server).listFlows(Arrays.asList("sign-in", "sign-up"));
      rec.assertRequest("/v1/mgmt/flow/list");
      assertEquals("[\"sign-in\",\"sign-up\"]", rec.body.get().get("ids").toString());
      assertEquals(1, rec.body.get().size());
      assertEquals(1, resp.getTotal());
      FlowMetadata f = resp.getFlows().get(0);
      assertEquals("sign-in", f.getId());
      assertEquals("Sign In", f.getName());
      assertEquals("desc", f.getDescription());
      assertEquals(true, f.isDisabled());
      assertEquals(3, f.getVersion());
      assertEquals(Collections.singletonMap("a", "b"), f.getDsl());
      assertEquals(1700000000000L, f.getModifiedTime());
      assertEquals("e1", f.getEtag());
      assertEquals(true, f.getTranslate());
      assertEquals("c1", f.getTranslateConnectorID());
      assertEquals("en", f.getTranslateSourceLang());
      assertEquals(Arrays.asList("fr"), f.getTranslateTargetLangs());
      assertEquals(true, f.getFingerprint());
      assertEquals(false, f.getWidget());
      assertEquals("i1", f.getSharedInteractions().get(0).getId());
      assertEquals("t", f.getSharedInteractions().get(0).getType());
      assertEquals("l", f.getSharedInteractions().get(0).getLabel());
      assertEquals("ic", f.getSharedInteractions().get(0).getIcon());
      assertEquals("st", f.getSharedInteractions().get(0).getSubType());
      assertEquals(true, f.getManagement());
      assertEquals(true, f.getManagementConfig().getTriggers().getEnabled());
      assertEquals("event", f.getManagementConfig().getTriggers().getTriggerType());
      assertEquals(Arrays.asList("user.created"),
          f.getManagementConfig().getTriggers().getEventTriggers().getTriggeringEventTypes());
      assertEquals(true, f.getReferencesGlobalStrings());
      assertEquals(false, f.getClientValidationTracking());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testListFlowsWithNullIdsSendsEmptyBody() throws IOException {
    Recorded rec = new Recorded();
    HttpServer server = serverReturning("/v1/mgmt/flow/list", "{\"flows\":null}", rec);
    try {
      FlowsResponse resp = flowService(server).listFlows(null);
      rec.assertRequest("/v1/mgmt/flow/list");
      assertEquals(0, rec.body.get().size());
      assertNull(resp.getFlows());
      assertNull(resp.getTotal());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testRunManagementFlowForSuccess() throws IOException {
    Recorded rec = new Recorded();
    HttpServer server = serverReturning("/v1/mgmt/flow/run", "{\"output\":{\"key\":\"value\"}}", rec);
    try {
      Map<String, Object> input = new HashMap<>();
      input.put("email", "test@example.com");
      ManagementFlowOptions options =
          ManagementFlowOptions.builder().input(input).preview(true).tenant("T1").build();
      Map<String, Object> output = flowService(server).runManagementFlow("my-flow", options);
      rec.assertRequest("/v1/mgmt/flow/run");
      JsonNode req = rec.body.get();
      assertEquals("my-flow", req.get("flowId").asText());
      assertEquals("test@example.com", req.get("options").get("input").get("email").asText());
      assertEquals(true, req.get("options").get("preview").asBoolean());
      assertEquals("T1", req.get("options").get("tenant").asText());
      assertEquals(Collections.singletonMap("key", "value"), output);
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testRunManagementFlowWithoutOptionsOmitsThem() throws IOException {
    Recorded rec = new Recorded();
    HttpServer server = serverReturning("/v1/mgmt/flow/run", "{}", rec);
    try {
      Map<String, Object> output = flowService(server).runManagementFlow("my-flow", null);
      rec.assertRequest("/v1/mgmt/flow/run");
      assertEquals("{\"flowId\":\"my-flow\"}", rec.body.get().toString());
      assertNull(output);
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testRunManagementFlowForEmptyFlowID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.runManagementFlow("", null));
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRunManagementFlowAsyncForSuccess() throws IOException {
    Recorded rec = new Recorded();
    HttpServer server = serverReturning("/v1/mgmt/flow/async/run", "{\"executionId\":\"ex1\"}", rec);
    try {
      ManagementFlowOptions options = ManagementFlowOptions.builder()
          .input(Collections.singletonMap("k", "v")).preview(false).tenant("T1").build();
      String executionId = flowService(server).runManagementFlowAsync("my-flow", options);
      rec.assertRequest("/v1/mgmt/flow/async/run");
      JsonNode req = rec.body.get();
      assertEquals("my-flow", req.get("flowId").asText());
      assertEquals("v", req.get("options").get("input").get("k").asText());
      assertEquals(false, req.get("options").get("preview").asBoolean());
      assertEquals("T1", req.get("options").get("tenant").asText());
      assertEquals("ex1", executionId);
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testRunManagementFlowAsyncForEmptyFlowID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.runManagementFlowAsync(null, null));
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetManagementFlowAsyncResultForSuccess() throws IOException {
    Recorded rec = new Recorded();
    HttpServer server = serverReturning("/v1/mgmt/flow/async/result", "{\"output\":{\"done\":true}}", rec);
    try {
      Map<String, Object> output = flowService(server).getManagementFlowAsyncResult("ex1");
      rec.assertRequest("/v1/mgmt/flow/async/result");
      assertEquals("{\"executionId\":\"ex1\"}", rec.body.get().toString());
      assertEquals(Collections.singletonMap("done", true), output);
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testGetManagementFlowAsyncResultForEmptyExecutionID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.getManagementFlowAsyncResult(""));
    assertEquals("The ExecutionID argument is invalid", thrown.getMessage());
  }

  private static class Recorded {
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    private final AtomicReference<JsonNode> body = new AtomicReference<>();

    void assertRequest(String expectedPath) {
      assertEquals("POST", method.get());
      assertEquals(expectedPath, path.get());
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":mgmt-key", auth.get());
    }
  }

  private FlowService flowService(HttpServer server) {
    Client client = Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("mgmt-key")
        .build();
    return ManagementServiceBuilder.buildServices(client).getFlowService();
  }

  private HttpServer serverReturning(String path, String body, Recorded rec) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(path, exchange -> {
      rec.method.set(exchange.getRequestMethod());
      rec.path.set(exchange.getRequestURI().getPath());
      rec.auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      String reqBody = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
      rec.body.set(new ObjectMapper().readTree(reqBody));
      byte[] payload = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    return server;
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleTheme() throws Exception {
    FlowsResponse flows = flowService.listFlows();
    assertNotNull(flows.getFlows());
    Assertions.assertThat(flows.getFlows().size()).isGreaterThan(0);
    for (FlowMetadata f : flows.getFlows()) {
      Assertions.assertThat(f.getId()).isNotBlank();
    }
    String flowId = "sign-up-or-in";
    FlowResponse flowResponse = flowService.exportFlow(flowId);
    Assertions.assertThat(flowResponse.getScreens().size()).isGreaterThan(0);
    assertNotNull(flowResponse.getFlow());
    // flowResponse = flowService.importFlow(flowId, flowResponse.getFlow(), flowResponse.getScreens());
    // Assertions.assertThat(flowResponse.getScreens().size()).isGreaterThan(0);
    // assertNotNull(flowResponse.getFlow());
    Theme theme = flowService.exportTheme();
    assertNotNull(theme);
    Assertions.assertThat(theme.getId()).isNotBlank();
    assertNotNull(theme.getCssTemplate());
    theme = flowService.importTheme(theme);
    assertNotNull(theme);
    Assertions.assertThat(theme.getId()).isNotBlank();
    assertNotNull(theme.getCssTemplate());
  }
}
