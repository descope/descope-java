package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.enums.ProjectEnvironment;
import com.descope.enums.ProjectTag;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.project.ExportProjectResponse;
import com.descope.model.project.ExportSnapshotRequest;
import com.descope.model.project.ExportSnapshotResponse;
import com.descope.model.project.ImportSnapshotRequest;
import com.descope.model.project.NewProjectResponse;
import com.descope.model.project.Project;
import com.descope.model.project.ProjectsResponse;
import com.descope.model.project.SnapshotSecret;
import com.descope.model.project.SnapshotSecrets;
import com.descope.model.project.ValidateSnapshotResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

public class ProjectServiceImplTest {
  private ProjectService projectService;
  private ArgumentCaptor<ClassicHttpRequest> captor;
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

  @Test
  void testListProjectsForSuccess() {
    ProjectsResponse mockResponse = ProjectsResponse.builder()
        .projects(Arrays.asList(Project.builder().id("p1").name("name1").environment("production").build()))
        .build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Project> projects = projectService.listProjects();
      Assertions.assertThat(projects).hasSize(1);
      Assertions.assertThat(projects.get(0).getId()).isEqualTo("p1");
    }
  }

  @Test
  void testCloneWithEnvironmentAndTags() throws Exception {
    final NewProjectResponse resp = exchange("/v1/mgmt/project/clone",
        "{\"projectId\":\"P2\",\"projectName\":\"name2\",\"tag\":\"production\","
            + "\"environment\":\"production\",\"tags\":[\"t1\",\"t2\"]}",
        () -> directProjectService().cloneProject("name2", ProjectEnvironment.Production, Arrays.asList("t1", "t2")));
    Map<?, ?> body = lastBody();
    assertEquals("name2", body.get("name"));
    assertEquals("production", body.get("environment"));
    assertEquals(Arrays.asList("t1", "t2"), body.get("tags"));
    assertFalse(body.containsKey("tag"));
    assertEquals("P2", resp.getProjectId());
    assertEquals("name2", resp.getProjectName());
    assertEquals("production", resp.getTag());
    assertEquals("production", resp.getEnvironment());
    assertEquals(Arrays.asList("t1", "t2"), resp.getTags());
  }

  @Test
  void testCloneWithoutEnvironmentAndTags() throws Exception {
    exchange("/v1/mgmt/project/clone", "{\"projectId\":\"P2\"}",
        () -> directProjectService().cloneProject("name2", null, null));
    assertEquals(Collections.singletonMap("name", "name2"), lastBody());
  }

  @Test
  void testExportSnapshot() throws Exception {
    ExportSnapshotResponse resp = exchange("/v1/mgmt/project/snapshot/export",
        "{\"files\":{\"foo/bar.json\":{\"a\":\"b\"}}}",
        () -> directProjectService().exportSnapshot(ExportSnapshotRequest.builder().format("split").build()));
    assertEquals(Collections.singletonMap("format", "split"), lastBody());
    assertEquals(mapOf("foo/bar.json", mapOf("a", "b")), resp.getFiles());

    exchange("/v1/mgmt/project/snapshot/export", "{}", () -> directProjectService().exportSnapshot(null));
    assertTrue(lastBody().isEmpty());
  }

  @Test
  void testImportSnapshot() throws Exception {
    SnapshotSecret secret = SnapshotSecret.builder().id("c1").name("conn").type("http").value("secret").build();
    exchange("/v1/mgmt/project/snapshot/import", "{}", () -> {
      directProjectService().importSnapshot(ImportSnapshotRequest.builder()
          .files(mapOf("foo/bar.json", mapOf("a", "b")))
          .excludes(Collections.singletonList("lists"))
          .inputSecrets(SnapshotSecrets.builder().connectors(Collections.singletonList(secret)).build())
          .build());
      return null;
    });
    Map<?, ?> body = lastBody();
    assertEquals(mapOf("foo/bar.json", mapOf("a", "b")), body.get("files"));
    assertEquals(Collections.singletonList("lists"), body.get("excludes"));
    assertEquals(Collections.singletonMap("connectors", Collections.singletonList(
        mapOf("id", "c1", "name", "conn", "type", "http", "value", "secret"))), body.get("inputSecrets"));
  }

  @Test
  void testImportSnapshotOnlyFiles() throws Exception {
    exchange("/v1/mgmt/project/snapshot/import", "{}", () -> {
      directProjectService().importSnapshot(ImportSnapshotRequest.builder().files(mapOf("a", "b")).build());
      return null;
    });
    assertEquals(Collections.singletonMap("files", mapOf("a", "b")), lastBody());
  }

  @Test
  void testValidateSnapshot() throws Exception {
    final ValidateSnapshotResponse resp = exchange("/v1/mgmt/project/snapshot/validate",
        "{\"ok\":false,\"failures\":[\"foo\"],\"missingSecrets\":{"
            + "\"connectors\":[{\"id\":\"c1\",\"name\":\"conn\",\"type\":\"http\"}],"
            + "\"oauthProviders\":[{\"id\":\"o1\",\"name\":\"oauth\",\"type\":\"google\"}],"
            + "\"outboundApps\":[{\"id\":\"a1\",\"name\":\"app\",\"type\":\"slack\"}],"
            + "\"externalAuth\":[{\"id\":\"e1\",\"name\":\"ext\",\"type\":\"jwt\"}]}}",
        () -> directProjectService().validateSnapshot(ImportSnapshotRequest.builder()
            .files(mapOf("a", "b"))
            .excludes(Collections.singletonList("lists"))
            .build()));
    Map<?, ?> body = lastBody();
    assertEquals(mapOf("a", "b"), body.get("files"));
    assertEquals(Collections.singletonList("lists"), body.get("excludes"));
    assertFalse(body.containsKey("inputSecrets"));
    assertFalse(resp.isOk());
    assertEquals(Collections.singletonList("foo"), resp.getFailures());
    SnapshotSecret connector = resp.getMissingSecrets().getConnectors().get(0);
    assertEquals("c1", connector.getId());
    assertEquals("conn", connector.getName());
    assertEquals("http", connector.getType());
    assertNull(connector.getValue());
    assertEquals("o1", resp.getMissingSecrets().getOauthProviders().get(0).getId());
    assertEquals("a1", resp.getMissingSecrets().getOutboundApps().get(0).getId());
    assertEquals("e1", resp.getMissingSecrets().getExternalAuth().get(0).getId());
  }

  private ProjectService directProjectService() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("test-key")
        .build();
    return new ProjectServiceImpl(client);
  }

  // Checks the method, path and management key bearer of the call's request.
  private <T> T exchange(String path, String responseBody, Supplier<T> call) throws IOException {
    captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, responseBody);
    T result;
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      result = call.get();
    }
    ClassicHttpRequest request = captor.getValue();
    assertEquals("POST", request.getMethod());
    assertTrue(request.getRequestUri().endsWith(path));
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", request.getFirstHeader("Authorization").getValue());
    return result;
  }

  private Map<?, ?> lastBody() throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    captor.getValue().getEntity().writeTo(bs);
    return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CloseableHttpClient mockHttpClient(ArgumentCaptor<ClassicHttpRequest> captor, String responseBody)
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
}
