package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.ServerCommonException;
import com.descope.model.flow.Flow;
import com.descope.model.flow.FlowResponse;
import com.descope.model.flow.Screen;
import com.descope.model.flow.Theme;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FlowService;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FlowServiceImplTest {

  private FlowService flowService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getManagementParams();
    var client = TestUtils.getClient();
    this.flowService = ManagementServiceBuilder.buildServices(client, authParams).getFlowService();
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
    var flowResponse = mock(FlowResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(flowResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = flowService.exportFlow("someFlowID");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testImportFlowForEmptyFlowID() {
    var flow = mock(Flow.class);
    var screen = mock(Screen.class);
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> flowService.importFlow("", flow, List.of(screen)));
    assertNotNull(thrown);
    assertEquals("The FlowID argument is invalid", thrown.getMessage());
  }

  @Test
  void testImportFlowForSuccess() {
    var flowResponse = mock(FlowResponse.class);
    var flow = mock(Flow.class);
    var screen = mock(Screen.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(flowResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = flowService.importFlow("someFlowID", flow, List.of(screen));
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testExportThemeForSuccess() {
    var theme = mock(Theme.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(theme).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = flowService.exportTheme();
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
    var theme = mock(Theme.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(theme).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = flowService.importTheme(theme);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testFunctionalFullCycleTheme() throws Exception {
    var flows = flowService.listFlows();
    assertNotNull(flows.getFlows());
    Assertions.assertThat(flows.getFlows().size()).isGreaterThan(0);
    for (var f : flows.getFlows()) {
      Assertions.assertThat(f.getId()).isNotBlank();
    }
    String flowId = "sign-up-or-in";
    var flowResponse = flowService.exportFlow(flowId);
    Assertions.assertThat(flowResponse.getScreens().size()).isGreaterThan(0);
    assertNotNull(flowResponse.getFlow());
    // flowResponse = flowService.importFlow(flowId, flowResponse.getFlow(), flowResponse.getScreens());
    // Assertions.assertThat(flowResponse.getScreens().size()).isGreaterThan(0);
    // assertNotNull(flowResponse.getFlow());
    var theme = flowService.exportTheme();
    assertNotNull(theme);
    Assertions.assertThat(theme.getId()).isNotBlank();
    assertNotNull(theme.getCssTemplate());
    theme = flowService.importTheme(theme);
    assertNotNull(theme);
    Assertions.assertThat(theme.getId()).isNotBlank();
    assertNotNull(theme.getCssTemplate());
  }
}
