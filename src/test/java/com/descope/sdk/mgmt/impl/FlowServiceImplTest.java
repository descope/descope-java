package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowMetadata;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.ManagementFlowOptions;
import com.descope.model.flow.ManagementFlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.model.flow.ThemeResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FlowService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
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

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";

  @Test
  void testListFlowsWithIds() {
    Call call = call("{\"flows\":[{\"id\":\"sign-up\",\"name\":\"Sign Up\",\"description\":\"desc\","
        + "\"disabled\":true}],\"total\":7}",
        fs -> fs.listFlows(Arrays.asList("sign-up", "sign-in")));
    assertEquals("https://api.descope.com/v1/mgmt/flow/list", call.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, call.authHeader);
    assertEquals(Collections.singletonMap("ids", Arrays.asList("sign-up", "sign-in")), call.body);
    FlowsResponse response = (FlowsResponse) call.response;
    assertEquals(7, response.getTotal());
    Assertions.assertThat(response.getFlows()).hasSize(1);
    FlowMetadata flow = response.getFlows().get(0);
    assertEquals("sign-up", flow.getId());
    assertEquals("Sign Up", flow.getName());
    assertEquals("desc", flow.getDescription());
    assertEquals(true, flow.isDisabled());
  }

  @Test
  void testListFlowsWithoutIds() {
    Call call = call("{\"flows\":[]}", fs -> fs.listFlows(null));
    assertEquals("https://api.descope.com/v1/mgmt/flow/list", call.uri.toString());
    assertEquals(Collections.emptyMap(), call.body);
    assertEquals(0, ((FlowsResponse) call.response).getTotal());
  }

  @Test
  void testRunManagementFlow() {
    Map<String, Object> input = new HashMap<>();
    input.put("email", "test@example.com");
    ManagementFlowOptions options = ManagementFlowOptions.builder().input(input).preview(true).tenant("t1").build();
    Call call = call("{\"output\":{\"result\":\"ok\",\"count\":2}}",
        fs -> fs.runManagementFlow("management-flow", options));
    assertEquals("https://api.descope.com/v1/mgmt/flow/run", call.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, call.authHeader);
    Map<String, Object> expected = new HashMap<>();
    expected.put("flowId", "management-flow");
    expected.put("options", options);
    assertEquals(expected, call.body);
    Map<String, Object> output = ((ManagementFlowResponse) call.response).getOutput();
    assertEquals("ok", output.get("result"));
    assertEquals(2, output.get("count"));
  }

  @Test
  void testRunManagementFlowWithoutOptions() {
    Call call = call("{\"output\":{}}", fs -> fs.runManagementFlow("management-flow", null));
    assertEquals(Collections.singletonMap("flowId", "management-flow"), call.body);
  }

  @Test
  void testRunManagementFlowForEmptyFlowID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.runManagementFlow("", null));
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRunManagementFlowAsync() {
    ManagementFlowOptions options = ManagementFlowOptions.builder().tenant("t1").build();
    Call call = call("{\"executionId\":\"exec-1\"}", fs -> fs.runManagementFlowAsync("management-flow", options));
    assertEquals("https://api.descope.com/v1/mgmt/flow/async/run", call.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, call.authHeader);
    Map<String, Object> expected = new HashMap<>();
    expected.put("flowId", "management-flow");
    expected.put("options", options);
    assertEquals(expected, call.body);
    assertEquals("exec-1", call.response);
  }

  @Test
  void testRunManagementFlowAsyncForEmptyFlowID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.runManagementFlowAsync(null, null));
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetManagementFlowAsyncResult() {
    Call call = call("{\"output\":{\"result\":\"ok\"}}", fs -> fs.getManagementFlowAsyncResult("exec-1"));
    assertEquals("https://api.descope.com/v1/mgmt/flow/async/result", call.uri.toString());
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, call.authHeader);
    assertEquals(Collections.singletonMap("executionId", "exec-1"), call.body);
    assertEquals(Collections.singletonMap("result", "ok"), ((ManagementFlowResponse) call.response).getOutput());
  }

  @Test
  void testGetManagementFlowAsyncResultForEmptyExecutionID() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> flowService.getManagementFlowAsyncResult(""));
    assertEquals("The ExecutionID argument is invalid", thrown.getMessage());
  }

  @Test
  void testRunManagementFlowResponseWithNullOutput() {
    Call call = call("{\"output\":null}", fs -> fs.runManagementFlow("management-flow", null));
    assertNull(((ManagementFlowResponse) call.response).getOutput());
  }

  private static class Call {
    private String authHeader;
    private URI uri;
    private Object body;
    private Object response;
  }

  /** Runs the call against a proxy that answers with the given JSON, and records the request. */
  @SuppressWarnings("unchecked")
  private static Call call(String json, Function<FlowService, Object> fn) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    FlowService fs = ManagementServiceBuilder.buildServices(client).getFlowService();
    ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doAnswer(inv -> objectMapper.readValue(json, (Class<?>) inv.getArgument(2)))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    Call call = new Call();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.response = fn.apply(fs);
    }
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), any());
    call.authHeader = authHeader.get();
    call.uri = uriCaptor.getValue();
    call.body = bodyCaptor.getValue();
    return call;
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
