package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.descope.enums.ProjectTag;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.project.ExportProjectResponse;
import com.descope.model.project.NewProjectResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.ProjectService;


public class ProjectServiceImplTest {
  private ProjectService projectService;
  private final NewProjectResponse mockCloneResponse = NewProjectResponse.builder()
      .projectId("id1").projectName("name1").build();
  private final ExportProjectResponse mockExportResponse = ExportProjectResponse.builder()
      .files(mapOf("foo/bar.json", mapOf("a", "b"))).build();

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.projectService = mgmtServices.getProjectService();
  }

  @Test
  void testUpdateNameForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      projectService.updateName("new-name");
    }
  }

  @Test
  void testCloneForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockCloneResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      NewProjectResponse response = projectService.cloneProject("new-name", ProjectTag.Production);
      Assertions.assertThat(response.getProjectId()).isNotBlank();
    }
  }

  @Test
  void testExportForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockExportResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ExportProjectResponse response = projectService.exportProject();
      Assertions.assertThat(response.getFiles()).isEqualTo(mapOf("foo/bar.json", mapOf("a", "b")));
    }
  }

  @Test
  void testImportForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      projectService.importProject(mockExportResponse.getFiles());
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }
}
