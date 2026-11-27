package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowMetadata;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.TenantTheme;
import com.descope.model.flow.Theme;
import com.descope.model.flow.ThemeResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FlowService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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

  private static final String TENANT_THEME_URI = "https://api.descope.com/v1/mgmt/theme/tenant/";
  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";

  private static TenantTheme mockTenantTheme() {
    Map<String, Object> light = new LinkedHashMap<>();
    light.put("logo", "data:image/png;base64,AA==");
    Map<String, Map<String, Object>> styles = new LinkedHashMap<>();
    styles.put("light", light);
    styles.put("dark", Collections.emptyMap());
    return TenantTheme.builder().styles(styles).componentsVersion("1.2.3").build();
  }

  /** Runs the call against a mocked proxy and returns the Authorization header it was sent with. */
  @SuppressWarnings("unchecked")
  private static String runTenantThemeCall(ApiProxy apiProxy, Consumer<FlowService> call) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    FlowService service = ManagementServiceBuilder.buildServices(client).getFlowService();
    AtomicReference<String> captured = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        captured.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.accept(service);
    }
    return captured.get();
  }

  private static void assertInvalid(String argument, Executable call) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, call);
    assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetTenantThemeForEmptyTenantID() {
    assertInvalid("TenantID", () -> flowService.getTenantTheme(""));
    assertInvalid("TenantID", () -> flowService.getTenantTheme(null));
  }

  @Test
  void testGetTenantThemeForSuccess() {
    TenantTheme theme = mockTenantTheme();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(theme).when(apiProxy).get(any(), eq(TenantTheme.class));
    AtomicReference<TenantTheme> result = new AtomicReference<>();
    String auth = runTenantThemeCall(apiProxy, s -> result.set(s.getTenantTheme("someTenantID")));
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth);
    verify(apiProxy).get(eq(URI.create(TENANT_THEME_URI + "someTenantID")), eq(TenantTheme.class));
    assertEquals(theme.getStyles(), result.get().getStyles());
    assertEquals("1.2.3", result.get().getComponentsVersion());
  }

  @Test
  void testGetTenantThemeEncodesTenantID() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    runTenantThemeCall(apiProxy, s -> s.getTenantTheme("a b/c?d"));
    verify(apiProxy).get(eq(URI.create(TENANT_THEME_URI + "a%20b%2Fc%3Fd")), eq(TenantTheme.class));
  }

  @Test
  void testSetTenantThemeForInvalidArguments() {
    TenantTheme theme = mockTenantTheme();
    assertInvalid("TenantID", () -> flowService.setTenantTheme("", theme));
    assertInvalid("Styles", () -> flowService.setTenantTheme("someTenantID", null));
    assertInvalid("Styles", () -> flowService.setTenantTheme("someTenantID", new TenantTheme()));
    assertInvalid("Styles",
        () -> flowService.setTenantTheme("someTenantID", TenantTheme.builder().styles(Collections.emptyMap()).build()));
  }

  @Test
  void testSetTenantThemeForSuccess() throws Exception {
    TenantTheme theme = mockTenantTheme();
    TenantTheme request = TenantTheme.builder().styles(theme.getStyles()).build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(theme).when(apiProxy).post(any(), any(), eq(TenantTheme.class));
    AtomicReference<TenantTheme> result = new AtomicReference<>();
    String auth = runTenantThemeCall(apiProxy, s -> result.set(s.setTenantTheme("someTenantID", request)));
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth);
    ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy).post(eq(URI.create(TENANT_THEME_URI + "someTenantID")), body.capture(), eq(TenantTheme.class));
    assertEquals("{\"styles\":{\"light\":{\"logo\":\"data:image/png;base64,AA==\"},\"dark\":{}}}",
        new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(body.getValue()));
    assertEquals(theme.getStyles(), result.get().getStyles());
    assertEquals("1.2.3", result.get().getComponentsVersion());
  }

  @Test
  void testDeleteTenantThemeForEmptyTenantID() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    runTenantThemeCall(apiProxy, s -> assertInvalid("TenantID", () -> s.deleteTenantTheme(" ")));
    verify(apiProxy, never()).delete(any(), any(), any());
  }

  @Test
  void testDeleteTenantThemeForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    String auth = runTenantThemeCall(apiProxy, s -> s.deleteTenantTheme("someTenantID"));
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, auth);
    verify(apiProxy).delete(eq(URI.create(TENANT_THEME_URI + "someTenantID")), isNull(), eq(Void.class));
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
