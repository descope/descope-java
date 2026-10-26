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
import com.fasterxml.jackson.databind.JsonNode;
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

  private static final String MOCK_PROJECT_ID = "P123456789012345678901234567";
  private static final String MOCK_MANAGEMENT_KEY = "some-management-key";
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static TenantService keyedTenantService() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(MOCK_PROJECT_ID)
        .managementKey(MOCK_MANAGEMENT_KEY)
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
  void testPatchForMissingArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.patch(null));
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.patch(PatchTenantRequest.builder().name("name").build()));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.patch(
        PatchTenantRequest.builder().id("id").selfProvisioningDomains(Arrays.asList("")).build()));
    assertEquals("The selfProvisioningDomains argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.patch(
        PatchTenantRequest.builder().id("id").enforceSSOExclusions(Arrays.asList(" ")).build()));
    assertEquals("The enforceSSOExclusions argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.patch(
        PatchTenantRequest.builder().id("id").federatedAppIds(Arrays.asList("")).build()));
    assertEquals("The federatedAppIds argument is invalid", thrown.getMessage());
  }

  @Test
  void testPatchForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedApiProxyBuilder, apiProxy, authHeader);
      keyedTenantService().patch(PatchTenantRequest.builder()
          .id("someId")
          .name("someName")
          .enforceSSO(false)
          .federatedAppIds(Collections.emptyList())
          .build());
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy, times(1)).patch(uri.capture(), body.capture(), eq(Void.class));
      assertThat(uri.getValue().toString()).isEqualTo("https://api.descope.com/v1/mgmt/tenant/patch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
      assertThat(MAPPER.readTree(MAPPER.writeValueAsString(body.getValue()))).isEqualTo(MAPPER.readTree(
          "{\"id\":\"someId\",\"name\":\"someName\",\"enforceSSO\":false,\"federatedAppIds\":[]}"));
    }
  }

  @Test
  void testCreateBatchForMissingArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(null));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.createBatch(Collections.emptyList()));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.createBatch(Collections.singletonList(null)));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(
        Arrays.asList(CreateTenantRequest.builder().id("id").build())));
    assertEquals("The tenants[].name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(Arrays.asList(
        CreateTenantRequest.builder().name("name").selfProvisioningDomains(Arrays.asList("")).build())));
    assertEquals("The tenants[].selfProvisioningDomains argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(Arrays.asList(
        CreateTenantRequest.builder().name("name").enforceSSOExclusions(Arrays.asList("")).build())));
    assertEquals("The tenants[].enforceSSOExclusions argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(Arrays.asList(
        CreateTenantRequest.builder().name("name").federatedAppIds(Arrays.asList("")).build())));
    assertEquals("The tenants[].federatedAppIds argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateBatchForSuccess() throws Exception {
    CreateTenantsBatchResponse mockResponse = MAPPER.readValue("{\"createdTenants\":[{\"id\":\"t1\",\"name\":\"n1\"}],"
        + "\"failedTenants\":[{\"failure\":\"duplicate\",\"tenant\":{\"name\":\"n2\",\"id\":\"t2\","
        + "\"selfProvisioningDomains\":[\"d.com\"],\"customAttributes\":{\"a\":\"b\"},\"authType\":\"saml\","
        + "\"disabled\":true,\"enforceSSO\":true,\"enforceSSOExclusions\":[\"u1\"],\"federatedAppIds\":[\"app1\"],"
        + "\"parent\":\"p1\",\"roleInheritance\":\"none\",\"unknownField\":1}}],"
        + "\"additionalErrors\":{\"k\":\"v\"}}", CreateTenantsBatchResponse.class);
    assertThat(mockResponse.getCreatedTenants()).hasSize(1);
    assertThat(mockResponse.getCreatedTenants().get(0).getId()).isEqualTo("t1");
    assertThat(mockResponse.getCreatedTenants().get(0).getName()).isEqualTo("n1");
    assertThat(mockResponse.getFailedTenants().get(0).getFailure()).isEqualTo("duplicate");
    CreateTenantRequest failed = mockResponse.getFailedTenants().get(0).getTenant();
    assertThat(failed.getId()).isEqualTo("t2");
    assertThat(failed.getName()).isEqualTo("n2");
    assertThat(failed.getSelfProvisioningDomains()).containsExactly("d.com");
    assertThat(failed.getCustomAttributes()).containsEntry("a", "b");
    assertThat(failed.getAuthType()).isEqualTo("saml");
    assertThat(failed.getDisabled()).isTrue();
    assertThat(failed.getEnforceSSO()).isTrue();
    assertThat(failed.getEnforceSSOExclusions()).containsExactly("u1");
    assertThat(failed.getFederatedAppIds()).containsExactly("app1");
    assertThat(failed.getParent()).isEqualTo("p1");
    assertThat(failed.getRoleInheritance()).isEqualTo("none");
    assertThat(mockResponse.getAdditionalErrors()).containsEntry("k", "v");

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedApiProxyBuilder, apiProxy, authHeader);
      CreateTenantsBatchResponse response = keyedTenantService().createBatch(Arrays.asList(
          CreateTenantRequest.builder().name("n1").build(),
          CreateTenantRequest.builder().id("t2").name("n2").parent("p1").enforceSSO(true).build()));
      assertThat(response).isSameAs(mockResponse);
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(CreateTenantsBatchResponse.class));
      assertThat(uri.getValue().toString()).isEqualTo("https://api.descope.com/v1/mgmt/tenant/create/batch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
      assertThat(MAPPER.readTree(MAPPER.writeValueAsString(body.getValue()))).isEqualTo(MAPPER.readTree(
          "{\"tenants\":[{\"name\":\"n1\"},{\"id\":\"t2\",\"name\":\"n2\",\"parent\":\"p1\",\"enforceSSO\":true}]}"));
    }
  }

  @Test
  void testUpdateBatchForMissingArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(null));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.updateBatch(Collections.emptyList()));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.updateBatch(Collections.singletonList(null)));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(
        Arrays.asList(UpdateTenantRequest.builder().name("name").build())));
    assertEquals("The tenants[].id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(
        Arrays.asList(UpdateTenantRequest.builder().id("id").build())));
    assertEquals("The tenants[].name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(Arrays.asList(
        UpdateTenantRequest.builder().id("id").name("name").selfProvisioningDomains(Arrays.asList("")).build())));
    assertEquals("The tenants[].selfProvisioningDomains argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(Arrays.asList(
        UpdateTenantRequest.builder().id("id").name("name").enforceSSOExclusions(Arrays.asList("")).build())));
    assertEquals("The tenants[].enforceSSOExclusions argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.updateBatch(Arrays.asList(
        UpdateTenantRequest.builder().id("id").name("name").federatedAppIds(Arrays.asList("")).build())));
    assertEquals("The tenants[].federatedAppIds argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateBatchForSuccess() throws Exception {
    UpdateTenantsBatchResponse mockResponse = MAPPER.readValue("{\"updatedTenantIds\":[\"t1\"],"
        + "\"failedTenants\":[{\"failure\":\"not found\",\"tenant\":{\"id\":\"t2\",\"name\":\"n2\","
        + "\"selfProvisioningDomains\":[],\"customAttributes\":null,\"authType\":\"\",\"disabled\":false,"
        + "\"enforceSSO\":false,\"enforceSSOExclusions\":[],\"federatedAppIds\":[],\"roleInheritance\":\"\"}}],"
        + "\"additionalErrors\":{}}", UpdateTenantsBatchResponse.class);
    assertThat(mockResponse.getUpdatedTenantIds()).containsExactly("t1");
    assertThat(mockResponse.getFailedTenants().get(0).getFailure()).isEqualTo("not found");
    UpdateTenantRequest failed = mockResponse.getFailedTenants().get(0).getTenant();
    assertThat(failed.getId()).isEqualTo("t2");
    assertThat(failed.getName()).isEqualTo("n2");
    assertThat(failed.getSelfProvisioningDomains()).isEmpty();
    assertThat(failed.getCustomAttributes()).isNull();
    assertThat(failed.getDisabled()).isFalse();
    assertThat(failed.getEnforceSSO()).isFalse();
    assertThat(mockResponse.getAdditionalErrors()).isEmpty();

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedApiProxyBuilder, apiProxy, authHeader);
      UpdateTenantsBatchResponse response = keyedTenantService().updateBatch(Arrays.asList(
          UpdateTenantRequest.builder().id("t1").name("n1").disabled(true)
              .selfProvisioningDomains(Arrays.asList("d.com")).build()));
      assertThat(response).isSameAs(mockResponse);
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(UpdateTenantsBatchResponse.class));
      assertThat(uri.getValue().toString()).isEqualTo("https://api.descope.com/v1/mgmt/tenant/update/batch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
      assertThat(MAPPER.readTree(MAPPER.writeValueAsString(body.getValue()))).isEqualTo(MAPPER.readTree(
          "{\"tenants\":[{\"id\":\"t1\",\"name\":\"n1\",\"disabled\":true,\"selfProvisioningDomains\":[\"d.com\"]}]}"));
    }
  }

  @Test
  void testDeleteBatchForMissingArguments() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.deleteBatch(null, null));
    assertEquals("The ids argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.deleteBatch(Collections.emptyList(), null));
    assertEquals("The ids argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.deleteBatch(Arrays.asList("t1", ""), null));
    assertEquals("The ids argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteBatchForSuccess() throws Exception {
    DeleteTenantsBatchResponse mockResponse = MAPPER.readValue("{\"deletedTenantIds\":[\"t1\"],"
        + "\"failedTenants\":[{\"failure\":\"not found\",\"id\":\"t2\"}],"
        + "\"additionalErrors\":{\"k\":\"v\"}}", DeleteTenantsBatchResponse.class);
    assertThat(mockResponse.getDeletedTenantIds()).containsExactly("t1");
    assertThat(mockResponse.getFailedTenants().get(0).getFailure()).isEqualTo("not found");
    assertThat(mockResponse.getFailedTenants().get(0).getId()).isEqualTo("t2");
    assertThat(mockResponse.getAdditionalErrors()).containsEntry("k", "v");

    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockResponse).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedApiProxyBuilder, apiProxy, authHeader);
      DeleteTenantsBatchResponse response = keyedTenantService().deleteBatch(Arrays.asList("t1", "t2"), true);
      assertThat(response).isSameAs(mockResponse);
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy, times(1)).post(uri.capture(), body.capture(), eq(DeleteTenantsBatchResponse.class));
      assertThat(uri.getValue().toString()).isEqualTo("https://api.descope.com/v1/mgmt/tenant/delete/batch");
      assertThat(authHeader.get()).isEqualTo("Bearer " + MOCK_PROJECT_ID + ":" + MOCK_MANAGEMENT_KEY);
      JsonNode sent = MAPPER.readTree(MAPPER.writeValueAsString(body.getValue()));
      assertThat(sent).isEqualTo(MAPPER.readTree("{\"ids\":[\"t1\",\"t2\"],\"cascade\":true}"));
    }
  }
}
