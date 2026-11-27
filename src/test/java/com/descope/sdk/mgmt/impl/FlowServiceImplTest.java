package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.flow.ExportedTheme;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowMetadata;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.FlowsResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.model.flow.ThemeReferences;
import com.descope.model.flow.ThemeResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FlowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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

  @Test
  void testExportProjectThemeForSuccess() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor,
        "{\"theme\":{\"styles\":{\"light\":{\"color\":\"red\"}},\"componentsVersion\":\"1.2.3\","
            + "\"references\":{\"connectors\":{\"c1\":\"Connector 1\"},\"oauth\":[\"google\"],"
            + "\"omitted\":[\"o1\"],\"roles\":{\"r1\":\"Role 1\"},\"userAttributes\":[\"ua1\"],"
            + "\"tenantAttributes\":[\"ta1\"]},\"unknown\":true}}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      ExportedTheme theme = keyedFlowService().exportProjectTheme();
      ClassicHttpRequest request = captor.getValue();
      assertEquals("POST", request.getMethod());
      assertTrue(request.getRequestUri().endsWith("/v2/mgmt/theme/export"));
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", request.getHeader("Authorization").getValue());
      assertEquals(Collections.singletonMap("light", Collections.singletonMap("color", "red")), theme.getStyles());
      assertEquals("1.2.3", theme.getComponentsVersion());
      ThemeReferences references = theme.getReferences();
      assertEquals(Collections.singletonMap("c1", "Connector 1"), references.getConnectors());
      assertEquals(Arrays.asList("google"), references.getOauth());
      assertEquals(Arrays.asList("o1"), references.getOmitted());
      assertEquals(Collections.singletonMap("r1", "Role 1"), references.getRoles());
      assertEquals(Arrays.asList("ua1"), references.getUserAttributes());
      assertEquals(Arrays.asList("ta1"), references.getTenantAttributes());
    }
  }

  @Test
  void testExportProjectThemeForNullTheme() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, "{\"theme\":null}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      assertNull(keyedFlowService().exportProjectTheme());
    }
  }

  @Test
  void testImportProjectThemeForNullTheme() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown =
          assertThrows(ServerCommonException.class, () -> flowService.importProjectTheme(null));
      assertEquals("The Theme argument is invalid", thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  void testImportProjectThemeForNullStyles() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ServerCommonException thrown = assertThrows(ServerCommonException.class,
          () -> flowService.importProjectTheme(ExportedTheme.builder().componentsVersion("1.2.3").build()));
      assertEquals("The Styles argument is invalid", thrown.getMessage());
      verify(apiProxy, never()).post(any(), any(), any());
    }
  }

  @Test
  void testImportProjectThemeForSuccess() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, "{}");
    ExportedTheme theme = ExportedTheme.builder()
        .styles(Collections.singletonMap("light", Collections.singletonMap("color", "red")))
        .componentsVersion("1.2.3")
        .references(ThemeReferences.builder()
            .connectors(Collections.singletonMap("c1", "Connector 1"))
            .oauth(Arrays.asList("google"))
            .omitted(Arrays.asList("o1"))
            .roles(Collections.singletonMap("r1", "Role 1"))
            .userAttributes(Arrays.asList("ua1"))
            .tenantAttributes(Arrays.asList("ta1"))
            .build())
        .build();
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      keyedFlowService().importProjectTheme(theme);
    }
    ClassicHttpRequest request = captor.getValue();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith("/v2/mgmt/theme/import"));
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", request.getHeader("Authorization").getValue());
    ObjectMapper objectMapper = new ObjectMapper();
    Map<?, ?> expected = objectMapper.readValue("{\"theme\":{\"styles\":{\"light\":{\"color\":\"red\"}},"
        + "\"componentsVersion\":\"1.2.3\",\"references\":{\"connectors\":{\"c1\":\"Connector 1\"},"
        + "\"oauth\":[\"google\"],\"omitted\":[\"o1\"],\"roles\":{\"r1\":\"Role 1\"},"
        + "\"userAttributes\":[\"ua1\"],\"tenantAttributes\":[\"ta1\"]}}}", Map.class);
    assertEquals(expected, objectMapper.readValue(requestBody(request), Map.class));
  }

  @Test
  void testImportProjectThemeOmitsUnsetFields() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, "{}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      keyedFlowService().importProjectTheme(ExportedTheme.builder().styles(Collections.emptyMap()).build());
    }
    assertEquals("{\"theme\":{\"styles\":{}}}", requestBody(captor.getValue()));
  }

  private static FlowService keyedFlowService() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("test-key")
        .build();
    return ManagementServiceBuilder.buildServices(client).getFlowService();
  }

  private static String requestBody(ClassicHttpRequest request) throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    return new String(bs.toByteArray(), StandardCharsets.UTF_8);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static CloseableHttpClient mockHttpClient(ArgumentCaptor<ClassicHttpRequest> captor, String responseBody)
      throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent())
          .thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    return httpClient;
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
