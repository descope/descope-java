package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.resource.DynamicRegistrationTemplate;
import com.descope.model.resource.DynamicRegistrationTemplateRequest;
import com.descope.model.resource.DynamicRegistrationTemplateResponse;
import com.descope.model.resource.LoadAllDynamicRegistrationTemplatesResponse;
import com.descope.model.resource.ThirdPartyApplicationSessionSettings;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.ResourceService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ResourceServiceImplTest {

  private static final String MGMT_KEY = "someManagementKey";

  private final DynamicRegistrationTemplate mockTemplate = DynamicRegistrationTemplate.builder()
      .id("tid")
      .version(3L)
      .name("tname")
      .description("desc")
      .loginPageURL("https://example.com/login")
      .sessionSettings(ThirdPartyApplicationSessionSettings.builder()
          .enabled(true)
          .refreshTokenExpiration(5)
          .refreshTokenExpirationUnit(ThirdPartyApplicationSessionSettings.TIME_UNIT_DAYS)
          .build())
      .skipConsentScreen(true)
      .forceAddAllAuthorizationInfo(false)
      .tags(Arrays.asList("t1", "t2"))
      .createdTime(1700000000L)
      .modifiedTime(1700000001L)
      .fallbackClientLogo("https://example.com/logo.png")
      .build();

  private ResourceService resourceService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.resourceService = ManagementServiceBuilder.buildServices(client).getResourceService();
    // set after build to skip the license handshake
    client.setManagementKey(MGMT_KEY);
  }

  @SuppressWarnings("unchecked")
  private void assertAuthHeader(MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder) {
    ArgumentCaptor<Supplier<String>> captor = ArgumentCaptor.forClass(Supplier.class);
    mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(captor.capture(), any(Client.class)));
    assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MGMT_KEY, captor.getValue().get());
  }

  @Test
  void testCreateDynamicRegistrationTemplateValidation() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> resourceService.createDynamicRegistrationTemplate(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> resourceService.createDynamicRegistrationTemplate(DynamicRegistrationTemplateRequest.builder().build()));
    assertEquals("The request.name argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateDynamicRegistrationTemplateSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(DynamicRegistrationTemplateResponse.builder().template(mockTemplate).build())
        .when(apiProxy).post(any(), any(), any());
    DynamicRegistrationTemplateRequest request = DynamicRegistrationTemplateRequest.builder()
        .name("tname")
        .description("desc")
        .tags(Collections.singletonList("t1"))
        .build();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      DynamicRegistrationTemplate res = resourceService.createDynamicRegistrationTemplate(request);
      assertEquals(mockTemplate, res);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).post(uriCaptor.capture(), eq(request), eq(DynamicRegistrationTemplateResponse.class));
      assertEquals("/v1/mgmt/resource/dynamic-registration-template/create", uriCaptor.getValue().getPath());
      assertAuthHeader(mockedApiProxyBuilder);
    }
  }

  @Test
  void testUpdateDynamicRegistrationTemplateValidation() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> resourceService.updateDynamicRegistrationTemplate(null));
    assertEquals("The template argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> resourceService.updateDynamicRegistrationTemplate(
            DynamicRegistrationTemplate.builder().name("n").build()));
    assertEquals("The template.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> resourceService.updateDynamicRegistrationTemplate(
            DynamicRegistrationTemplate.builder().id("i").build()));
    assertEquals("The template.name argument is invalid", thrown.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testUpdateDynamicRegistrationTemplateSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(DynamicRegistrationTemplateResponse.builder().template(mockTemplate).build())
        .when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      DynamicRegistrationTemplate res = resourceService.updateDynamicRegistrationTemplate(mockTemplate);
      assertEquals(mockTemplate, res);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(DynamicRegistrationTemplateResponse.class));
      assertEquals("/v1/mgmt/resource/dynamic-registration-template/update", uriCaptor.getValue().getPath());
      assertSame(mockTemplate, ((Map<String, Object>) bodyCaptor.getValue()).get("template"));
      assertAuthHeader(mockedApiProxyBuilder);
    }
  }

  @Test
  void testLoadDynamicRegistrationTemplateValidation() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> resourceService.loadDynamicRegistrationTemplate(null));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadDynamicRegistrationTemplateSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(DynamicRegistrationTemplateResponse.builder().template(mockTemplate).build())
        .when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      DynamicRegistrationTemplate res = resourceService.loadDynamicRegistrationTemplate("tid");
      assertEquals(mockTemplate, res);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(DynamicRegistrationTemplateResponse.class));
      assertEquals("/v1/mgmt/resource/dynamic-registration-template/load", uriCaptor.getValue().getPath());
      assertEquals("id=tid", uriCaptor.getValue().getQuery());
      assertAuthHeader(mockedApiProxyBuilder);
    }
  }

  @Test
  void testLoadAllDynamicRegistrationTemplatesSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    LoadAllDynamicRegistrationTemplatesResponse resp = LoadAllDynamicRegistrationTemplatesResponse.builder()
        .templates(Collections.singletonList(mockTemplate))
        .total(1)
        .build();
    doReturn(resp).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      LoadAllDynamicRegistrationTemplatesResponse res = resourceService.loadAllDynamicRegistrationTemplates();
      assertEquals(resp, res);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(LoadAllDynamicRegistrationTemplatesResponse.class));
      assertEquals("/v1/mgmt/resource/dynamic-registration-templates/load", uriCaptor.getValue().getPath());
      assertAuthHeader(mockedApiProxyBuilder);
    }
  }

  @Test
  void testDynamicRegistrationTemplateJson() throws Exception {
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String json = "{\"template\":{\"id\":\"tid\",\"version\":\"3\",\"name\":\"tname\",\"description\":\"desc\","
        + "\"loginPageURL\":\"https://example.com/login\",\"sessionSettings\":{\"enabled\":true,"
        + "\"refreshTokenExpiration\":5,\"refreshTokenExpirationUnit\":\"days\"},\"skipConsentScreen\":true,"
        + "\"forceAddAllAuthorizationInfo\":false,\"tags\":[\"t1\",\"t2\"],\"createdTime\":1700000000,"
        + "\"modifiedTime\":\"1700000001\",\"fallbackClientLogo\":\"https://example.com/logo.png\",\"extra\":1}}";
    DynamicRegistrationTemplateResponse res = mapper.readValue(json, DynamicRegistrationTemplateResponse.class);
    assertEquals(mockTemplate, res.getTemplate());
    String out = mapper.writeValueAsString(mockTemplate);
    assertTrue(out.contains("\"version\":\"3\""));
    assertTrue(out.contains("\"createdTime\":\"1700000000\""));
    assertTrue(out.contains("\"loginPageURL\":\"https://example.com/login\""));
  }
}
