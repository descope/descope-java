package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.flow.ExportedFlow;
import com.descope.model.flow.ExportedFlowMetadata;
import com.descope.model.flow.ExportedScreen;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowMetadata;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.ManagementConfigTriggers;
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

  private static final String PROJECT_ID = "P123";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final ObjectMapper MAPPER = new ObjectMapper();

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

  @Test
  void testExportFlowV2ForEmptyFlowID() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown =
          assertThrows(ServerCommonException.class, () -> flowService.exportFlowV2(""));
      assertEquals("The FlowID argument is invalid", thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  void testExportFlowV2ForSuccess() throws Exception {
    String response = "{\"flow\":{\"flowId\":\"sign-up\",\"metadata\":{\"name\":\"Sign Up\","
        + "\"description\":\"desc\",\"componentsVersion\":\"1.2.3\",\"disabled\":true,\"fingerprint\":true,"
        + "\"widget\":false,\"translation\":{\"enabled\":true,\"connectorId\":\"c1\",\"sourceLanguage\":\"en\","
        + "\"targetLanguages\":[\"fr\"]},\"sharedInteractions\":[{\"id\":\"i1\",\"type\":\"button\","
        + "\"label\":\"Go\",\"icon\":\"ic\",\"subType\":\"st\"}],\"management\":true,"
        + "\"managementConfig\":{\"triggers\":{\"enabled\":true,\"triggerType\":\"event\","
        + "\"eventTriggers\":{\"triggeringEventTypes\":[\"user.created\"]}}},\"clientValidationTracking\":true},"
        + "\"contents\":{\"a\":1},\"screens\":[{\"screenId\":\"s1\",\"interactions\":[{\"id\":\"i2\"}],"
        + "\"contents\":{\"b\":\"c\"}}],\"references\":{\"connectors\":{\"k\":\"v\"},\"oauth\":[\"google\"],"
        + "\"omitted\":[\"o\"],\"roles\":{\"r\":\"R\"},\"userAttributes\":[\"ua\"],"
        + "\"tenantAttributes\":[\"ta\"],\"newField\":1}},\"unknown\":\"x\"}";
    Captured captured = new Captured();
    HttpServer server = server("/v2/mgmt/flow/export", response, captured);
    try {
      final ExportedFlow flow = flowService(server).exportFlowV2("sign-up");
      assertEquals("POST", captured.method.get());
      assertEquals("/v2/mgmt/flow/export", captured.path.get());
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, captured.auth.get());
      assertEquals(MAPPER.readTree("{\"flowId\":\"sign-up\"}"), captured.body.get());
      assertEquals("sign-up", flow.getFlowId());
      ExportedFlowMetadata metadata = flow.getMetadata();
      assertEquals("Sign Up", metadata.getName());
      assertEquals("desc", metadata.getDescription());
      assertEquals("1.2.3", metadata.getComponentsVersion());
      assertEquals(true, metadata.getDisabled());
      assertEquals(true, metadata.getFingerprint());
      assertEquals(false, metadata.getWidget());
      assertEquals(true, metadata.getTranslation().getEnabled());
      assertEquals("c1", metadata.getTranslation().getConnectorId());
      assertEquals("en", metadata.getTranslation().getSourceLanguage());
      assertEquals(Arrays.asList("fr"), metadata.getTranslation().getTargetLanguages());
      assertEquals("i1", metadata.getSharedInteractions().get(0).getId());
      assertEquals("button", metadata.getSharedInteractions().get(0).getType());
      assertEquals("Go", metadata.getSharedInteractions().get(0).getLabel());
      assertEquals("ic", metadata.getSharedInteractions().get(0).getIcon());
      assertEquals("st", metadata.getSharedInteractions().get(0).getSubType());
      assertEquals(true, metadata.getManagement());
      ManagementConfigTriggers triggers = metadata.getManagementConfig().getTriggers();
      assertEquals(true, triggers.getEnabled());
      assertEquals(ManagementConfigTriggers.TRIGGER_TYPE_EVENT, triggers.getTriggerType());
      assertEquals(Arrays.asList("user.created"), triggers.getEventTriggers().getTriggeringEventTypes());
      assertEquals(true, metadata.getClientValidationTracking());
      assertEquals(1, flow.getContents().get("a"));
      ExportedScreen screen = flow.getScreens().get(0);
      assertEquals("s1", screen.getScreenId());
      assertEquals("i2", screen.getInteractions().get(0).getId());
      assertEquals("c", screen.getContents().get("b"));
      assertEquals(Collections.singletonMap("k", "v"), flow.getReferences().getConnectors());
      assertEquals(Arrays.asList("google"), flow.getReferences().getOauth());
      assertEquals(Arrays.asList("o"), flow.getReferences().getOmitted());
      assertEquals(Collections.singletonMap("r", "R"), flow.getReferences().getRoles());
      assertEquals(Arrays.asList("ua"), flow.getReferences().getUserAttributes());
      assertEquals(Arrays.asList("ta"), flow.getReferences().getTenantAttributes());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testExportFlowV2ForNullFlow() throws Exception {
    HttpServer server = server("/v2/mgmt/flow/export", "{\"flow\":null}", new Captured());
    try {
      Assertions.assertThat(flowService(server).exportFlowV2("sign-up")).isNull();
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testImportFlowV2ForEmptyFlowID() {
    ExportedFlow flow = ExportedFlow.builder().flowId("x").build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown =
          assertThrows(ServerCommonException.class, () -> flowService.importFlowV2("", flow));
      assertEquals("The FlowID argument is invalid", thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  void testImportFlowV2ForNullFlow() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown =
          assertThrows(ServerCommonException.class, () -> flowService.importFlowV2("sign-up", null));
      assertEquals("The Flow argument is invalid", thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  void testImportFlowV2ForSuccess() throws Exception {
    Map<String, Object> contents = new HashMap<>();
    contents.put("a", 1);
    ExportedFlow flow = ExportedFlow.builder()
        .flowId("old-id")
        .metadata(ExportedFlowMetadata.builder().name("Sign Up").build())
        .contents(contents)
        .build();
    Captured captured = new Captured();
    HttpServer server = server("/v2/mgmt/flow/import", "{}", captured);
    try {
      flowService(server).importFlowV2("sign-up", flow, true, "some change");
      assertEquals("POST", captured.method.get());
      assertEquals("/v2/mgmt/flow/import", captured.path.get());
      assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, captured.auth.get());
      assertEquals(MAPPER.readTree("{\"flow\":{\"flowId\":\"sign-up\",\"metadata\":{\"name\":\"Sign Up\"},"
          + "\"contents\":{\"a\":1}},\"skipRender\":true,\"changelog\":\"some change\"}"), captured.body.get());
      assertEquals("old-id", flow.getFlowId());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void testImportFlowV2OmitsUnsetOptions() throws Exception {
    ExportedFlow flow = ExportedFlow.builder().flowId("sign-up").build();
    Captured captured = new Captured();
    HttpServer server = server("/v2/mgmt/flow/import", "{}", captured);
    try {
      flowService(server).importFlowV2("sign-up", flow);
      JsonNode body = captured.body.get();
      assertEquals(MAPPER.readTree("{\"flowId\":\"sign-up\"}"), body.get("flow"));
      assertFalse(body.has("skipRender"));
      assertFalse(body.has("changelog"));
    } finally {
      server.stop(0);
    }
  }

  private static class Captured {
    final AtomicReference<String> method = new AtomicReference<>();
    final AtomicReference<String> path = new AtomicReference<>();
    final AtomicReference<String> auth = new AtomicReference<>();
    final AtomicReference<JsonNode> body = new AtomicReference<>();
  }

  private static FlowService flowService(HttpServer server) {
    Client client = Client.builder()
        .uri("http://localhost:" + server.getAddress().getPort())
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .keys(new AtomicReference<>(new HashMap<>()))
        .build();
    return ManagementServiceBuilder.buildServices(client).getFlowService();
  }

  private static HttpServer server(String path, String response, Captured captured) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(path, exchange -> {
      captured.method.set(exchange.getRequestMethod());
      captured.path.set(exchange.getRequestURI().getPath());
      captured.auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      captured.body.set(MAPPER.readTree(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8)));
      byte[] payload = response.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    });
    server.start();
    return server;
  }
}
