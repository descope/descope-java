package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import com.descope.enums.TenantAuthType;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.CreateTenantRequest;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.PatchTenantRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.request.UpdateTenantRequest;
import com.descope.model.tenant.response.CreateTenantsBatchResponse;
import com.descope.model.tenant.response.DeleteTenantsBatchResponse;
import com.descope.model.tenant.response.GenerateTenantLinkResponse;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.model.tenant.response.UpdateTenantsBatchResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.TenantService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class TenantServiceImplTest {

  private final List<String> selfProvisioningDomains = Arrays.asList("domain1", "domain2");
  Tenant mockTenant = Tenant.builder()
      .id("id")
      .name("name")
      .selfProvisioningDomains(selfProvisioningDomains)
      .build();

  TenantSettings mockSettings = TenantSettings.builder()
      .sessionSettingsEnabled(true)
      .domains(Arrays.asList("d1", "d2"))
      .enableInactivity(true)
      .inactivityTime(3)
      .inactivityTimeUnit("days")
      .refreshTokenExpiration(30)
      .refreshTokenExpirationUnit("days")
      .selfProvisioningDomains(Arrays.asList("dd1", "dd2"))
      .sessionTokenExpiration(5)
      .sessionTokenExpirationUnit("minutes")
      .build();
  private TenantService tenantService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.tenantService = ManagementServiceBuilder.buildServices(client).getTenantService();
  }

  @Test
  void testCreateForEmptyName() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class, () -> tenantService.create("", selfProvisioningDomains));
    assertNotNull(thrown);
    assertEquals("The name argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String response = tenantService.create("someName", selfProvisioningDomains);
      assertThat(response).isEqualTo("id");
    }
  }

  @Test
  void testCreateWithIdForEmptyId() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> tenantService.createWithId("", "", selfProvisioningDomains));
    assertNotNull(thrown);
    assertEquals("The id or name argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateWithIdForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.createWithId("someLoginId", "someName", selfProvisioningDomains);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testUpdateForEmptyId() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> tenantService.update("", "", selfProvisioningDomains, null));
    assertNotNull(thrown);
    assertEquals("The id or name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.update("someLoginId", "someName", selfProvisioningDomains, null);
      ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
      verify(apiProxy, times(1)).post(any(), captor.capture(), any());
      Tenant sent = captor.getValue();
      assertThat(sent.getId()).isEqualTo("someLoginId");
      assertThat(sent.getName()).isEqualTo("someName");
      assertThat(sent.getSelfProvisioningDomains()).isEqualTo(selfProvisioningDomains);
      assertThat(sent.getCustomAttributes()).isNull();
      assertThat(sent.getAuthType()).isNull();
      assertThat(sent.getDisabled()).isNull();
      assertThat(sent.getEnforceSSO()).isNull();
      assertThat(sent.getEnforceSSOExclusions()).isNull();
      assertThat(sent.getFederatedAppIds()).isNull();
      assertThat(sent.getRoleInheritance()).isNull();
    }
  }

  @Test
  void testUpdateFullForEmptyId() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class,
        () -> tenantService.update("", "", selfProvisioningDomains, null, null, null, null, null, null, null));
    assertNotNull(thrown);
    assertEquals("The id or name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateFullForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.update("someId", "someName", selfProvisioningDomains, null,
          "saml", true, true, Arrays.asList("user1"), Arrays.asList("app1"), "inherit");
      ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
      verify(apiProxy, times(1)).post(any(), captor.capture(), any());
      Tenant sent = captor.getValue();
      assertThat(sent.getId()).isEqualTo("someId");
      assertThat(sent.getName()).isEqualTo("someName");
      assertThat(sent.getSelfProvisioningDomains()).isEqualTo(selfProvisioningDomains);
      assertThat(sent.getAuthType()).isEqualTo("saml");
      assertThat(sent.getDisabled()).isTrue();
      assertThat(sent.getEnforceSSO()).isTrue();
      assertThat(sent.getEnforceSSOExclusions()).containsExactly("user1");
      assertThat(sent.getFederatedAppIds()).containsExactly("app1");
      assertThat(sent.getRoleInheritance()).isEqualTo("inherit");
    }
  }

  @Test
  void testUpdateFullWithNullOptionalFieldsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.update("someId", "someName", null, null, null, null, null, null, null, null);
      ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
      verify(apiProxy, times(1)).post(any(), captor.capture(), any());
      Tenant sent = captor.getValue();
      assertThat(sent.getId()).isEqualTo("someId");
      assertThat(sent.getName()).isEqualTo("someName");
      assertThat(sent.getAuthType()).isNull();
      assertThat(sent.getDisabled()).isNull();
      assertThat(sent.getEnforceSSO()).isNull();
      assertThat(sent.getEnforceSSOExclusions()).isNull();
      assertThat(sent.getFederatedAppIds()).isNull();
      assertThat(sent.getRoleInheritance()).isNull();
    }
  }

  @Test
  void testDeleteForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.delete(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.delete("someId");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.load(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Tenant response = tenantService.load(mockTenant.getId());
      assertThat(response).isEqualTo(mockTenant);
    }
  }

  @Test
  void testGetSettingsForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.getSettings(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetSettingsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSettings).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      TenantSettings response = tenantService.getSettings(mockTenant.getId());
      assertThat(response).isEqualTo(mockSettings);
    }
  }

  @Test
  void testConfigureSettingsForEmptyId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> tenantService.configureSettings("", null));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForNoSettings() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> tenantService.configureSettings("a", null));
    assertNotNull(thrown);
    assertEquals("The settings argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllForSuccess() {
    GetAllTenantsResponse mockTenantsResponse =
        GetAllTenantsResponse.builder().tenants(Arrays.asList(mockTenant)).build();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockTenantsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      List<Tenant> response = tenantService.loadAll();
      assertThat(response.size()).isEqualTo(1);
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, Arrays.asList(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    List<Tenant> tenants = tenantService.loadAll();
    assertThat(tenants).isNotEmpty();
    boolean found = false;
    for (Tenant t : tenants) {
      if (t.getId().equals(tenantId)) {
        found = true;
        assertEquals(name, t.getName());
        assertThat(t.getSelfProvisioningDomains()).containsOnly(name + ".com", name + "1.com");
      }
    }
    assertTrue(found);
    Tenant tenant = tenantService.load(tenantId);
    assertThat(tenant).isNotNull();
    assertThat(tenant.getId()).isEqualTo(tenantId);
    TenantSettings tenantSettings = tenantService.getSettings(tenantId);
    assertThat(tenantSettings).isNotNull();
    assertThat(tenantSettings.getSelfProvisioningDomains()).containsOnly(name + ".com", name + "1.com");
    assertThat(tenantSettings.getJitDisabled()).isFalse();
    tenantService.update(tenantId, name + "1", Arrays.asList(name + ".com"), null);
    tenants = tenantService.loadAll();
    assertThat(tenants).isNotEmpty();
    found = false;
    for (Tenant t : tenants) {
      if (t.getId().equals(tenantId)) {
        found = true;
        assertEquals(name + "1", t.getName());
        assertThat(t.getSelfProvisioningDomains()).containsOnly(name + ".com");
      }
    }

    TenantSearchRequest tenantSearchRequest = TenantSearchRequest.builder().names(Arrays.asList(name + "1")).build();
    tenants = tenantService.searchAll(tenantSearchRequest);
    assertThat(tenants).isNotEmpty();
    found = false;
    for (Tenant t : tenants) {
      if (t.getId().equals(tenantId)) {
        found = true;
        assertEquals(name + "1", t.getName());
      }
    }

    tenantSearchRequest = TenantSearchRequest.builder().names(Arrays.asList("doesnotexists")).build();
    tenants = tenantService.searchAll(tenantSearchRequest);
    assertThat(tenants).isEmpty();

    GenerateTenantLinkRequest generateTenantLinkRequest = GenerateTenantLinkRequest.builder()
        .tenantId(tenantId)
        .expireDuration(60 * 60 * 24)
        .build();
      
    String link = tenantService.generateSSOConfigurationLink(generateTenantLinkRequest);
    assertThat(link).isNotBlank();

    tenantService.revokeSSOConfigurationLink(tenantId, "");

    tenantSettings.setJitDisabled(true);
    tenantSettings.setAuthType(TenantAuthType.OIDC);
    tenantService.configureSettings(tenantId, tenantSettings);
    tenantSettings = tenantService.getSettings(tenantId);
    assertThat(tenantSettings).isNotNull();
    assertThat(tenantSettings.getJitDisabled()).isTrue();
    assertThat(tenantSettings.getAuthType()).isEqualTo(TenantAuthType.OIDC);
    tenantService.delete(tenantId);
  }

  @Test
  void testGenerateSSOConfigurationLinkForEmptyParams() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () 
        -> tenantService.generateSSOConfigurationLink(GenerateTenantLinkRequest.builder()
          .tenantId("")
          .expireDuration(0)
          .ssoId("")
          .email("")
          .templateId("")
          .build()));
    assertNotNull(thrown);
    assertEquals("The tenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testGenerateSSOConfigurationLinkForSuccess() {
    GenerateTenantLinkResponse mockResponse = GenerateTenantLinkResponse.builder()
        .adminSSOConfigurationLink("some link")
        .build();

    ApiProxy apiProxy = mock(ApiProxy.class);

    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String response = tenantService.generateSSOConfigurationLink(
          GenerateTenantLinkRequest.builder()
            .tenantId("tenant")
            .expireDuration(60 * 60 * 24)
            .ssoId("")
            .email("")
            .templateId("")
            .build());

      assertThat(response).isEqualTo("some link");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testGenerateSSOConfigurationLinkWithSSOIdForSuccess() {
    GenerateTenantLinkResponse mockResponse = GenerateTenantLinkResponse.builder()
        .adminSSOConfigurationLink("some link")
        .build();

    ApiProxy apiProxy = mock(ApiProxy.class);

    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String response = tenantService.generateSSOConfigurationLink(
          GenerateTenantLinkRequest.builder()
            .tenantId("tenant")
            .expireDuration(60 * 60 * 24)
            .ssoId("bla")
            .email("a@a.com")
            .templateId("template-id")
            .build());

      assertThat(response).isEqualTo("some link");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testRevokeSSOConfigurationLinkForEmptyParams() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () ->
        tenantService.revokeSSOConfigurationLink("", ""));
    assertNotNull(thrown);
    assertEquals("The tenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testRevokeSSOConfigurationLinkForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.revokeSSOConfigurationLink("tenant", ""); 
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testRevokeSSOConfigurationLinkWithSSOIdForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.revokeSSOConfigurationLink("tenant", "bla"); 
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private TenantService mockedClientTenantService() {
    Client client = mock(Client.class);
    when(client.getProjectId()).thenReturn("proj");
    when(client.getManagementKey()).thenReturn("key");
    when(client.getUri()).thenReturn("https://api.descope.com");
    return new TenantServiceImpl(client);
  }

  @SuppressWarnings("unchecked")
  private static void assertManagementKeyHeader(MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder) {
    ArgumentCaptor<Supplier<String>> authCaptor = ArgumentCaptor.forClass(Supplier.class);
    mockedApiProxyBuilder.verify(() -> ApiProxyBuilder.buildProxy(authCaptor.capture(), any()));
    assertEquals("Bearer proj:key", authCaptor.getValue().get());
  }

  private static <R> void assertPost(ApiProxy apiProxy, Class<R> returnClz, String path, String body)
      throws Exception {
    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
    verify(apiProxy, times(1)).post(uriCaptor.capture(), bodyCaptor.capture(), eq(returnClz));
    assertEquals(path, uriCaptor.getValue().getPath());
    assertEquals(MAPPER.readTree(body), MAPPER.valueToTree(bodyCaptor.getValue()));
  }

  @Test
  void testPatchForSuccess() throws Exception {
    TenantService service = mockedClientTenantService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      service.patch(PatchTenantRequest.builder()
          .id("t1")
          .name("newName")
          .disabled(false)
          .federatedAppIds(Collections.emptyList())
          .build());
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy, times(1)).patch(uriCaptor.capture(), bodyCaptor.capture(), eq(Void.class));
      assertEquals("/v1/mgmt/tenant/patch", uriCaptor.getValue().getPath());
      assertEquals(MAPPER.readTree("{\"id\":\"t1\",\"name\":\"newName\",\"disabled\":false,\"federatedAppIds\":[]}"),
          MAPPER.valueToTree(bodyCaptor.getValue()));
      assertManagementKeyHeader(mockedApiProxyBuilder);
    }
  }

  @Test
  void testCreateBatchForEmptyTenants() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class, () -> tenantService.createBatch(Collections.emptyList()));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    assertThrows(ServerCommonException.class, () -> tenantService.createBatch(null));
  }

  @Test
  void testCreateBatchForSuccess() throws Exception {
    TenantService service = mockedClientTenantService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    CreateTenantsBatchResponse mockResponse = MAPPER.readValue("{"
        + "\"createdTenants\":[{\"id\":\"t1\",\"name\":\"n1\"}],"
        + "\"failedTenants\":[{\"failure\":\"exists\",\"tenant\":{\"id\":\"t2\",\"name\":\"n2\","
        + "\"selfProvisioningDomains\":[\"d.com\"],\"customAttributes\":{\"k\":\"v\"},\"authType\":\"saml\","
        + "\"disabled\":true,\"enforceSSO\":true,\"enforceSSOExclusions\":[\"u1\"],\"federatedAppIds\":[\"a1\"],"
        + "\"parent\":\"p1\",\"roleInheritance\":\"userOnly\"}}],"
        + "\"additionalErrors\":{\"k\":\"err\"},\"unknownField\":1}", CreateTenantsBatchResponse.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), eq(CreateTenantsBatchResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      CreateTenantsBatchResponse response = service.createBatch(Arrays.asList(
          CreateTenantRequest.builder().name("n1").parent("p1").build(),
          CreateTenantRequest.builder().id("t2").name("n2").enforceSSO(true).build()));
      assertPost(apiProxy, CreateTenantsBatchResponse.class, "/v1/mgmt/tenant/create/batch",
          "{\"tenants\":[{\"name\":\"n1\",\"parent\":\"p1\"},{\"id\":\"t2\",\"name\":\"n2\",\"enforceSSO\":true}]}");
      assertManagementKeyHeader(mockedApiProxyBuilder);

      assertEquals("t1", response.getCreatedTenants().get(0).getId());
      assertEquals("n1", response.getCreatedTenants().get(0).getName());
      assertEquals("exists", response.getFailedTenants().get(0).getFailure());
      CreateTenantRequest failed = response.getFailedTenants().get(0).getTenant();
      assertEquals("t2", failed.getId());
      assertEquals("n2", failed.getName());
      assertEquals(Arrays.asList("d.com"), failed.getSelfProvisioningDomains());
      assertEquals("v", failed.getCustomAttributes().get("k"));
      assertEquals("saml", failed.getAuthType());
      assertTrue(failed.getDisabled());
      assertTrue(failed.getEnforceSSO());
      assertEquals(Arrays.asList("u1"), failed.getEnforceSSOExclusions());
      assertEquals(Arrays.asList("a1"), failed.getFederatedAppIds());
      assertEquals("p1", failed.getParent());
      assertEquals("userOnly", failed.getRoleInheritance());
      assertEquals("err", response.getAdditionalErrors().get("k"));
    }
  }

  @Test
  void testUpdateBatchForEmptyTenants() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class, () -> tenantService.updateBatch(Collections.emptyList()));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(null));
  }

  @Test
  void testUpdateBatchForSuccess() throws Exception {
    TenantService service = mockedClientTenantService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    UpdateTenantsBatchResponse mockResponse = MAPPER.readValue("{"
        + "\"updatedTenantIds\":[\"t1\"],"
        + "\"failedTenants\":[{\"failure\":\"not found\",\"tenant\":{\"id\":\"t2\",\"name\":\"n2\","
        + "\"selfProvisioningDomains\":[],\"customAttributes\":null,\"authType\":\"\",\"disabled\":false,"
        + "\"enforceSSO\":false,\"enforceSSOExclusions\":[],\"federatedAppIds\":[],\"roleInheritance\":\"none\"}}],"
        + "\"additionalErrors\":{}}", UpdateTenantsBatchResponse.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), eq(UpdateTenantsBatchResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      UpdateTenantsBatchResponse response = service.updateBatch(Arrays.asList(
          UpdateTenantRequest.builder().id("t1").name("n1").roleInheritance("userOnly").build()));
      assertPost(apiProxy, UpdateTenantsBatchResponse.class, "/v1/mgmt/tenant/update/batch",
          "{\"tenants\":[{\"id\":\"t1\",\"name\":\"n1\",\"roleInheritance\":\"userOnly\"}]}");
      assertManagementKeyHeader(mockedApiProxyBuilder);

      assertEquals(Arrays.asList("t1"), response.getUpdatedTenantIds());
      assertEquals("not found", response.getFailedTenants().get(0).getFailure());
      UpdateTenantRequest failed = response.getFailedTenants().get(0).getTenant();
      assertEquals("t2", failed.getId());
      assertEquals("n2", failed.getName());
      assertTrue(failed.getSelfProvisioningDomains().isEmpty());
      assertEquals(null, failed.getCustomAttributes());
      assertEquals(false, failed.getDisabled());
      assertEquals("none", failed.getRoleInheritance());
      assertTrue(response.getAdditionalErrors().isEmpty());
    }
  }

  @Test
  void testDeleteBatchForEmptyIds() {
    ServerCommonException thrown = assertThrows(
        ServerCommonException.class, () -> tenantService.deleteBatch(Collections.emptyList(), false));
    assertEquals("The ids argument is invalid", thrown.getMessage());
    assertThrows(ServerCommonException.class, () -> tenantService.deleteBatch(null, false));
  }

  @Test
  void testDeleteBatchForSuccess() throws Exception {
    TenantService service = mockedClientTenantService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    DeleteTenantsBatchResponse mockResponse = MAPPER.readValue("{"
        + "\"deletedTenantIds\":[\"t1\"],"
        + "\"failedTenants\":[{\"failure\":\"not found\",\"id\":\"t2\"}],"
        + "\"additionalErrors\":{\"k\":\"err\"}}", DeleteTenantsBatchResponse.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), eq(DeleteTenantsBatchResponse.class));
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      DeleteTenantsBatchResponse response = service.deleteBatch(Arrays.asList("t1", "t2"), true);
      assertPost(apiProxy, DeleteTenantsBatchResponse.class, "/v1/mgmt/tenant/delete/batch",
          "{\"ids\":[\"t1\",\"t2\"],\"cascade\":true}");
      assertManagementKeyHeader(mockedApiProxyBuilder);

      assertEquals(Arrays.asList("t1"), response.getDeletedTenantIds());
      assertEquals("not found", response.getFailedTenants().get(0).getFailure());
      assertEquals("t2", response.getFailedTenants().get(0).getId());
      assertEquals("err", response.getAdditionalErrors().get("k"));
    }
  }
}
