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

import org.mockito.ArgumentCaptor;

import com.descope.enums.TenantAuthType;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.CreateTenantRequest;
import com.descope.model.tenant.request.CreateTenantsBatchRequest;
import com.descope.model.tenant.request.DeleteTenantsBatchRequest;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.PatchTenantRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.request.UpdateTenantRequest;
import com.descope.model.tenant.request.UpdateTenantsBatchRequest;
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
import java.util.concurrent.atomic.AtomicReference;
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

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final ObjectMapper WRITER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
  private static final ObjectMapper READER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static TenantService mgmtTenantService() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    return ManagementServiceBuilder.buildServices(client).getTenantService();
  }

  @SuppressWarnings("unchecked")
  private static void mockProxy(MockedStatic<ApiProxyBuilder> mocked, ApiProxy apiProxy,
      AtomicReference<String> authHeader) {
    mocked.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
  }

  @Test
  void testPatchForNullRequest() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.patch(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
  }

  @Test
  void testPatchForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.patch(PatchTenantRequest.builder().name("name").build()));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testPatchForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mocked, apiProxy, authHeader);
      mgmtTenantService().patch(PatchTenantRequest.builder()
          .id("t1")
          .name("name")
          .enforceSSO(true)
          .federatedAppIds(Collections.emptyList())
          .build());
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<PatchTenantRequest> body = ArgumentCaptor.forClass(PatchTenantRequest.class);
      verify(apiProxy, times(1)).patch(uri.capture(), body.capture(), eq(Void.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/tenant/patch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
      assertThat(WRITER.writeValueAsString(body.getValue()))
          .isEqualTo("{\"id\":\"t1\",\"name\":\"name\",\"enforceSSO\":true,\"federatedAppIds\":[]}");
    }
  }

  @Test
  void testCreateBatchForEmptyTenants() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(null));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.createBatch(Collections.emptyList()));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateBatchForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.createBatch(Arrays.asList(CreateTenantRequest.builder().id("t1").build())));
    assertEquals("The tenants[].name argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateBatchForSuccess() throws Exception {
    CreateTenantsBatchResponse response = READER.readValue("{\"createdTenants\":[{\"id\":\"t1\",\"name\":\"n1\"}],"
        + "\"failedTenants\":[{\"failure\":\"exists\",\"tenant\":{\"name\":\"n2\",\"id\":\"t2\","
        + "\"selfProvisioningDomains\":[\"d.com\"],\"customAttributes\":{\"k\":\"v\"},\"authType\":\"saml\","
        + "\"disabled\":false,\"enforceSSO\":true,\"enforceSSOExclusions\":[\"u1\"],\"federatedAppIds\":[\"a1\"],"
        + "\"parent\":\"p1\",\"roleInheritance\":\"none\"}}],\"additionalErrors\":{\"k\":\"v\"},\"unknown\":1}",
        CreateTenantsBatchResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mocked, apiProxy, authHeader);
      CreateTenantsBatchResponse res = mgmtTenantService().createBatch(Arrays.asList(
          CreateTenantRequest.builder().name("n1").parent("p1").build(),
          CreateTenantRequest.builder().name("n2").id("t2").build()));
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<CreateTenantsBatchRequest> body = ArgumentCaptor.forClass(CreateTenantsBatchRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(CreateTenantsBatchResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/tenant/create/batch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
      assertThat(WRITER.writeValueAsString(body.getValue()))
          .isEqualTo("{\"tenants\":[{\"name\":\"n1\",\"parent\":\"p1\"},{\"name\":\"n2\",\"id\":\"t2\"}]}");
      assertThat(res.getCreatedTenants()).hasSize(1);
      assertThat(res.getCreatedTenants().get(0).getId()).isEqualTo("t1");
      assertThat(res.getCreatedTenants().get(0).getName()).isEqualTo("n1");
      assertThat(res.getFailedTenants().get(0).getFailure()).isEqualTo("exists");
      CreateTenantRequest failed = res.getFailedTenants().get(0).getTenant();
      assertThat(failed.getId()).isEqualTo("t2");
      assertThat(failed.getName()).isEqualTo("n2");
      assertThat(failed.getSelfProvisioningDomains()).containsExactly("d.com");
      assertThat(failed.getCustomAttributes()).containsEntry("k", "v");
      assertThat(failed.getAuthType()).isEqualTo("saml");
      assertThat(failed.getDisabled()).isFalse();
      assertThat(failed.getEnforceSSO()).isTrue();
      assertThat(failed.getEnforceSSOExclusions()).containsExactly("u1");
      assertThat(failed.getFederatedAppIds()).containsExactly("a1");
      assertThat(failed.getParent()).isEqualTo("p1");
      assertThat(failed.getRoleInheritance()).isEqualTo("none");
      assertThat(res.getAdditionalErrors()).containsEntry("k", "v");
    }
  }

  @Test
  void testUpdateBatchForEmptyTenants() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(null));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateBatchForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.updateBatch(Arrays.asList(UpdateTenantRequest.builder().name("n1").build())));
    assertEquals("The tenants[].id argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateBatchForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.updateBatch(Arrays.asList(UpdateTenantRequest.builder().id("t1").build())));
    assertEquals("The tenants[].name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateBatchForSuccess() throws Exception {
    UpdateTenantsBatchResponse response = READER.readValue("{\"updatedTenantIds\":[\"t1\"],"
        + "\"failedTenants\":[{\"failure\":\"not found\",\"tenant\":{\"id\":\"t2\",\"name\":\"n2\","
        + "\"selfProvisioningDomains\":[\"d.com\"],\"customAttributes\":null,\"authType\":\"oidc\",\"disabled\":true,"
        + "\"enforceSSO\":false,\"enforceSSOExclusions\":[\"u1\"],\"federatedAppIds\":[\"a1\"],"
        + "\"roleInheritance\":\"userOnly\"}}],\"additionalErrors\":{}}", UpdateTenantsBatchResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mocked, apiProxy, authHeader);
      UpdateTenantsBatchResponse res = mgmtTenantService().updateBatch(Arrays.asList(
          UpdateTenantRequest.builder().id("t1").name("n1").disabled(true).build()));
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<UpdateTenantsBatchRequest> body = ArgumentCaptor.forClass(UpdateTenantsBatchRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(UpdateTenantsBatchResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/tenant/update/batch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
      assertThat(WRITER.writeValueAsString(body.getValue()))
          .isEqualTo("{\"tenants\":[{\"id\":\"t1\",\"name\":\"n1\",\"disabled\":true}]}");
      assertThat(res.getUpdatedTenantIds()).containsExactly("t1");
      assertThat(res.getFailedTenants().get(0).getFailure()).isEqualTo("not found");
      UpdateTenantRequest failed = res.getFailedTenants().get(0).getTenant();
      assertThat(failed.getId()).isEqualTo("t2");
      assertThat(failed.getName()).isEqualTo("n2");
      assertThat(failed.getSelfProvisioningDomains()).containsExactly("d.com");
      assertThat(failed.getCustomAttributes()).isNull();
      assertThat(failed.getAuthType()).isEqualTo("oidc");
      assertThat(failed.getDisabled()).isTrue();
      assertThat(failed.getEnforceSSO()).isFalse();
      assertThat(failed.getEnforceSSOExclusions()).containsExactly("u1");
      assertThat(failed.getFederatedAppIds()).containsExactly("a1");
      assertThat(failed.getRoleInheritance()).isEqualTo("userOnly");
      assertThat(res.getAdditionalErrors()).isEmpty();
    }
  }

  @Test
  void testDeleteBatchForEmptyIds() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.deleteBatch(null, false));
    assertEquals("The ids argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.deleteBatch(Collections.emptyList(), false));
    assertEquals("The ids argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteBatchForSuccess() throws Exception {
    DeleteTenantsBatchResponse response = READER.readValue("{\"deletedTenantIds\":[\"t1\"],"
        + "\"failedTenants\":[{\"failure\":\"not found\",\"id\":\"t2\"}],\"additionalErrors\":{\"k\":\"v\"}}",
        DeleteTenantsBatchResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mocked, apiProxy, authHeader);
      DeleteTenantsBatchResponse res = mgmtTenantService().deleteBatch(Arrays.asList("t1", "t2"), true);
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<DeleteTenantsBatchRequest> body = ArgumentCaptor.forClass(DeleteTenantsBatchRequest.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(DeleteTenantsBatchResponse.class));
      assertThat(uri.getValue().getPath()).isEqualTo("/v1/mgmt/tenant/delete/batch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY);
      assertThat(WRITER.writeValueAsString(body.getValue()))
          .isEqualTo("{\"ids\":[\"t1\",\"t2\"],\"cascade\":true}");
      assertThat(res.getDeletedTenantIds()).containsExactly("t1");
      assertThat(res.getFailedTenants().get(0).getId()).isEqualTo("t2");
      assertThat(res.getFailedTenants().get(0).getFailure()).isEqualTo("not found");
      assertThat(res.getAdditionalErrors()).containsEntry("k", "v");
    }
  }
}
