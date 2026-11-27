package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
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

import com.descope.enums.TenantAuthType;
import com.descope.enums.TenantRoleInheritance;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.tenant.AdditionalSSOConfiguration;
import com.descope.model.tenant.SSOSetupSuiteSettings;
import com.descope.model.tenant.SSOSetupSuiteSettingsDisabledFeatures;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.CreateTenantRequest;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.PatchTenantRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.request.UpdateTenantRequest;
import com.descope.model.tenant.response.CreateTenantResponse;
import com.descope.model.tenant.response.CreateTenantsBatchResponse;
import com.descope.model.tenant.response.DeleteTenantsBatchResponse;
import com.descope.model.tenant.response.GenerateTenantLinkResponse;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.model.tenant.response.UpdateTenantsBatchResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.TenantService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
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

  private static final String MGMT_KEY = "some-management-key";
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  /** Captures what one management call sends through the proxy. */
  private static class Sent {
    final AtomicReference<String> auth = new AtomicReference<>();
    final ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    final ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);

    String path() {
      return uri.getValue().getPath();
    }

    JsonNode json() {
      return MAPPER.valueToTree(body.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private static Sent call(ApiProxy apiProxy, Consumer<TenantService> call) {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MGMT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    TenantService service = ManagementServiceBuilder.buildServices(client).getTenantService();
    Sent sent = new Sent();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        sent.auth.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.accept(service);
    }
    return sent;
  }

  private static <T> T parse(String json, Class<T> clz) throws Exception {
    return MAPPER.readValue(json, clz);
  }

  private static JsonNode tree(String json) throws Exception {
    return MAPPER.readTree(json);
  }

  private static void assertMgmtAuth(Sent sent) {
    assertThat(sent.auth.get()).isEqualTo("Bearer " + TestUtils.PROJECT_ID + ":" + MGMT_KEY);
  }

  private static final String TENANT_REQUEST_FIELDS = "\"selfProvisioningDomains\":[\"d.com\"],"
      + "\"customAttributes\":{\"k\":\"v\"},\"authType\":\"saml\",\"disabled\":false,\"enforceSSO\":true,"
      + "\"enforceSSOExclusions\":[\"u1\"],\"federatedAppIds\":[\"app1\"],\"roleInheritance\":\"userOnly\","
      + "\"approvedDomains\":[\"vanity.example.com\"],\"enforceSSOExclusionPermissions\":[\"perm1\"],"
      + "\"strict\":true,\"permittedIps\":[\"10.0.0.0/8\"]";

  private static CreateTenantRequest fullCreateRequest() {
    return CreateTenantRequest.builder()
        .name("t1")
        .id("tid")
        .parent("pid")
        .selfProvisioningDomains(Arrays.asList("d.com"))
        .customAttributes(mapOf("k", "v"))
        .authType(TenantAuthType.SAML.getValue())
        .disabled(false)
        .enforceSSO(true)
        .enforceSSOExclusions(Arrays.asList("u1"))
        .federatedAppIds(Arrays.asList("app1"))
        .roleInheritance(TenantRoleInheritance.USER_ONLY.getValue())
        .approvedDomains(Arrays.asList("vanity.example.com"))
        .enforceSSOExclusionPermissions(Arrays.asList("perm1"))
        .strict(true)
        .permittedIps(Arrays.asList("10.0.0.0/8"))
        .build();
  }

  private static UpdateTenantRequest fullUpdateRequest() {
    return UpdateTenantRequest.builder()
        .id("tid")
        .name("t1")
        .selfProvisioningDomains(Arrays.asList("d.com"))
        .customAttributes(mapOf("k", "v"))
        .authType(TenantAuthType.SAML.getValue())
        .disabled(false)
        .enforceSSO(true)
        .enforceSSOExclusions(Arrays.asList("u1"))
        .federatedAppIds(Arrays.asList("app1"))
        .roleInheritance(TenantRoleInheritance.USER_ONLY.getValue())
        .approvedDomains(Arrays.asList("vanity.example.com"))
        .enforceSSOExclusionPermissions(Arrays.asList("perm1"))
        .strict(true)
        .permittedIps(Arrays.asList("10.0.0.0/8"))
        .build();
  }

  @Test
  void testCreateWithRequestForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parse("{\"id\":\"tid\",\"unknown\":1}", CreateTenantResponse.class))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<String> id = new AtomicReference<>();
    Sent sent = call(apiProxy, s -> id.set(s.create(fullCreateRequest())));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(CreateTenantResponse.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/create");
    assertThat(sent.json()).isEqualTo(tree("{\"name\":\"t1\",\"id\":\"tid\",\"parent\":\"pid\","
        + TENANT_REQUEST_FIELDS + "}"));
    assertThat(id.get()).isEqualTo("tid");
  }

  @Test
  void testCreateWithRequestOmitsUnsetFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new CreateTenantResponse("tid")).when(apiProxy).post(any(), any(), any());
    Sent sent = call(apiProxy, s -> s.create(CreateTenantRequest.builder().name("t1").build()));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), any());
    assertThat(sent.json().toString()).isEqualTo("{\"name\":\"t1\"}");
  }

  @Test
  void testCreateWithRequestForEmptyName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.create(CreateTenantRequest.builder().id("tid").build()));
    assertEquals("The name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.create((CreateTenantRequest) null));
    assertEquals("The name argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateBatchForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parse("{\"createdTenants\":[{\"id\":\"tid\",\"name\":\"t1\"}],"
        + "\"failedTenants\":[{\"failure\":\"bad\",\"tenant\":{\"name\":\"t2\",\"parent\":\"pid\","
        + "\"permittedIps\":[\"1.2.3.4\"]}}],\"additionalErrors\":{\"k\":\"e\"}}", CreateTenantsBatchResponse.class))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<CreateTenantsBatchResponse> res = new AtomicReference<>();
    Sent sent = call(apiProxy, s -> res.set(s.createBatch(Arrays.asList(fullCreateRequest()), true)));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(CreateTenantsBatchResponse.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/create/batch");
    assertThat(sent.json()).isEqualTo(tree("{\"tenants\":[{\"name\":\"t1\",\"id\":\"tid\",\"parent\":\"pid\","
        + TENANT_REQUEST_FIELDS + "}],\"strict\":true}"));
    CreateTenantsBatchResponse r = res.get();
    assertThat(r.getCreatedTenants()).hasSize(1);
    assertThat(r.getCreatedTenants().get(0).getId()).isEqualTo("tid");
    assertThat(r.getCreatedTenants().get(0).getName()).isEqualTo("t1");
    assertThat(r.getFailedTenants().get(0).getFailure()).isEqualTo("bad");
    assertThat(r.getFailedTenants().get(0).getTenant().getName()).isEqualTo("t2");
    assertThat(r.getFailedTenants().get(0).getTenant().getParent()).isEqualTo("pid");
    assertThat(r.getFailedTenants().get(0).getTenant().getPermittedIps()).containsExactly("1.2.3.4");
    assertThat(r.getAdditionalErrors()).containsEntry("k", "e");
  }

  @Test
  void testCreateBatchWithoutStrict() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    Sent sent = call(apiProxy,
        s -> s.createBatch(Arrays.asList(CreateTenantRequest.builder().name("t1").build()), null));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), any());
    assertThat(sent.json().toString()).isEqualTo("{\"tenants\":[{\"name\":\"t1\"}]}");
  }

  @Test
  void testCreateBatchForEmptyTenants() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.createBatch(Collections.emptyList(), true));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.createBatch(null, null));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateWithRequestForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    Sent sent = call(apiProxy, s -> s.update(fullUpdateRequest()));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(Void.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/update");
    assertThat(sent.json()).isEqualTo(tree("{\"id\":\"tid\",\"name\":\"t1\"," + TENANT_REQUEST_FIELDS + "}"));
  }

  @Test
  void testUpdateWithRequestForEmptyIdOrName() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.update(UpdateTenantRequest.builder().name("t1").build()));
    assertEquals("The id or name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.update(UpdateTenantRequest.builder().id("tid").build()));
    assertEquals("The id or name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> tenantService.update((UpdateTenantRequest) null));
    assertEquals("The id or name argument is invalid", thrown.getMessage());
  }

  @Test
  void testUpdateBatchForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parse("{\"updatedTenantIds\":[\"tid\"],\"failedTenants\":[{\"failure\":\"bad\",\"tenant\":"
        + "{\"id\":\"t2\",\"name\":\"n2\",\"approvedDomains\":[\"a.com\"]}}],\"additionalErrors\":{\"k\":\"e\"}}",
        UpdateTenantsBatchResponse.class)).when(apiProxy).post(any(), any(), any());
    AtomicReference<UpdateTenantsBatchResponse> res = new AtomicReference<>();
    Sent sent = call(apiProxy, s -> res.set(s.updateBatch(Arrays.asList(fullUpdateRequest()), false)));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(UpdateTenantsBatchResponse.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/update/batch");
    assertThat(sent.json()).isEqualTo(tree("{\"tenants\":[{\"id\":\"tid\",\"name\":\"t1\","
        + TENANT_REQUEST_FIELDS + "}],\"strict\":false}"));
    UpdateTenantsBatchResponse r = res.get();
    assertThat(r.getUpdatedTenantIds()).containsExactly("tid");
    assertThat(r.getFailedTenants().get(0).getFailure()).isEqualTo("bad");
    assertThat(r.getFailedTenants().get(0).getTenant().getId()).isEqualTo("t2");
    assertThat(r.getFailedTenants().get(0).getTenant().getApprovedDomains()).containsExactly("a.com");
    assertThat(r.getAdditionalErrors()).containsEntry("k", "e");
  }

  @Test
  void testUpdateBatchForEmptyTenants() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.updateBatch(Collections.emptyList(), null));
    assertEquals("The tenants argument is invalid", thrown.getMessage());
  }

  @Test
  void testPatchForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    PatchTenantRequest request = PatchTenantRequest.builder()
        .name("t1")
        .selfProvisioningDomains(Collections.emptyList())
        .customAttributes(mapOf("k", "v"))
        .authType(TenantAuthType.OIDC.getValue())
        .disabled(false)
        .enforceSSO(true)
        .enforceSSOExclusions(Arrays.asList("u1"))
        .federatedAppIds(Arrays.asList("app1"))
        .roleInheritance(TenantRoleInheritance.NONE.getValue())
        .approvedDomains(Arrays.asList("vanity.example.com"))
        .enforceSSOExclusionPermissions(Arrays.asList("perm1"))
        .strict(true)
        .permittedIps(Arrays.asList("10.0.0.1"))
        .build();
    Sent sent = call(apiProxy, s -> s.patch("tid", request));
    verify(apiProxy, times(1)).patch(sent.uri.capture(), sent.body.capture(), eq(Void.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/patch");
    assertThat(sent.json()).isEqualTo(tree("{\"id\":\"tid\",\"name\":\"t1\",\"selfProvisioningDomains\":[],"
        + "\"customAttributes\":{\"k\":\"v\"},\"authType\":\"oidc\",\"disabled\":false,\"enforceSSO\":true,"
        + "\"enforceSSOExclusions\":[\"u1\"],\"federatedAppIds\":[\"app1\"],\"roleInheritance\":\"none\","
        + "\"approvedDomains\":[\"vanity.example.com\"],\"enforceSSOExclusionPermissions\":[\"perm1\"],"
        + "\"strict\":true,\"permittedIps\":[\"10.0.0.1\"]}"));
  }

  @Test
  void testPatchSendsOnlySetFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    Sent sent = call(apiProxy, s -> s.patch("tid", PatchTenantRequest.builder().disabled(true).build()));
    verify(apiProxy, times(1)).patch(sent.uri.capture(), sent.body.capture(), any());
    assertThat(sent.json()).isEqualTo(MAPPER.createObjectNode().put("id", "tid").put("disabled", true));
    sent = call(apiProxy, s -> s.patch("tid", null));
    verify(apiProxy, times(2)).patch(sent.uri.capture(), sent.body.capture(), any());
    assertThat(sent.json().toString()).isEqualTo("{\"id\":\"tid\"}");
  }

  @Test
  void testPatchForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.patch("", PatchTenantRequest.builder().name("t1").build()));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteWithCascadeForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    Sent sent = call(apiProxy, s -> s.delete("tid", true));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(Void.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/delete");
    assertThat(sent.json()).isEqualTo(tree("{\"id\":\"tid\",\"cascade\":true}"));
  }

  @Test
  void testDeleteWithoutCascadeSendsIdOnly() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    Sent sent = call(apiProxy, s -> s.delete("tid"));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(Void.class));
    assertThat(sent.json().toString()).isEqualTo("{\"id\":\"tid\"}");
  }

  @Test
  void testDeleteWithCascadeForEmptyId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> tenantService.delete("", true));
    assertEquals("The id argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteBatchForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parse("{\"deletedTenantIds\":[\"t1\"],\"failedTenants\":[{\"failure\":\"bad\",\"id\":\"t2\"}],"
        + "\"additionalErrors\":{\"k\":\"e\"}}", DeleteTenantsBatchResponse.class))
        .when(apiProxy).post(any(), any(), any());
    AtomicReference<DeleteTenantsBatchResponse> res = new AtomicReference<>();
    Sent sent = call(apiProxy, s -> res.set(s.deleteBatch(Arrays.asList("t1", "t2"), true)));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(DeleteTenantsBatchResponse.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/delete/batch");
    assertThat(sent.json()).isEqualTo(tree("{\"ids\":[\"t1\",\"t2\"],\"cascade\":true}"));
    DeleteTenantsBatchResponse r = res.get();
    assertThat(r.getDeletedTenantIds()).containsExactly("t1");
    assertThat(r.getFailedTenants().get(0).getFailure()).isEqualTo("bad");
    assertThat(r.getFailedTenants().get(0).getId()).isEqualTo("t2");
    assertThat(r.getAdditionalErrors()).containsEntry("k", "e");
  }

  @Test
  void testDeleteBatchForEmptyIds() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> tenantService.deleteBatch(Collections.emptyList(), null));
    assertEquals("The ids argument is invalid", thrown.getMessage());
  }

  @Test
  void testSearchAllWithNewFiltersForSuccess() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new GetAllTenantsResponse(Arrays.asList(mockTenant))).when(apiProxy).post(any(), any(), any());
    Sent sent = call(apiProxy, s -> s.searchAll(TenantSearchRequest.builder()
        .ssoDomains(Arrays.asList("sso.com"))
        .parentTenantId("pid")
        .topLevelOnly(true)
        .build()));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(GetAllTenantsResponse.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/search");
    assertThat(sent.json()).isEqualTo(
        tree("{\"tenantSSODomains\":[\"sso.com\"],\"parentTenantId\":\"pid\",\"topLevelOnly\":true}"));
  }

  @Test
  void testLoadParsesNewFields() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parse("{\"id\":\"tid\",\"name\":\"t1\",\"createdTime\":1726067547,\"parent\":\"pid\","
        + "\"successors\":[\"s1\"],\"defaultRoles\":[\"r1\"],\"additionalSSOConfigs\":[{\"ssoId\":\"sso1\","
        + "\"name\":\"n\",\"authType\":\"oidc\"}],\"approvedDomains\":[\"vanity.example.com\"],"
        + "\"enforceSSOExclusionPermissions\":[\"perm1\"],\"permittedIps\":[\"10.0.0.1\"],\"unknown\":null}",
        Tenant.class)).when(apiProxy).get(any(), any());
    AtomicReference<Tenant> res = new AtomicReference<>();
    Sent sent = call(apiProxy, s -> res.set(s.load("tid")));
    verify(apiProxy, times(1)).get(sent.uri.capture(), eq(Tenant.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant");
    assertThat(sent.uri.getValue().getQuery()).isEqualTo("id=tid");
    Tenant t = res.get();
    assertThat(t.getCreatedTime()).isEqualTo(1726067547);
    assertThat(t.getParent()).isEqualTo("pid");
    assertThat(t.getSuccessors()).containsExactly("s1");
    assertThat(t.getDefaultRoles()).containsExactly("r1");
    assertThat(t.getAdditionalSSOConfigs()).containsExactly(new AdditionalSSOConfiguration("sso1", "n", "oidc"));
    assertThat(t.getApprovedDomains()).containsExactly("vanity.example.com");
    assertThat(t.getEnforceSSOExclusionPermissions()).containsExactly("perm1");
    assertThat(t.getPermittedIps()).containsExactly("10.0.0.1");
  }

  @Test
  void testGetSettingsParsesNewFields() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(parse("{\"enabled\":true,\"stepupTokenExpiration\":10,\"stepupTokenExpirationUnit\":\"minutes\","
        + "\"ssoSetupSuiteSettings\":{\"enabled\":true,\"styleId\":\"style\",\"disabledFeatures\":{\"saml\":true,"
        + "\"oidc\":true,\"scim\":false,\"ssoDomains\":true,\"groupMapping\":true,\"roleMapping\":true,"
        + "\"fgaMapping\":true,\"sso\":true,\"audit\":true}}}", TenantSettings.class))
        .when(apiProxy).get(any(), any());
    AtomicReference<TenantSettings> res = new AtomicReference<>();
    Sent sent = call(apiProxy, s -> res.set(s.getSettings("tid")));
    verify(apiProxy, times(1)).get(sent.uri.capture(), eq(TenantSettings.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/settings");
    assertThat(sent.uri.getValue().getQuery()).isEqualTo("id=tid");
    TenantSettings t = res.get();
    assertThat(t.getSessionSettingsEnabled()).isTrue();
    assertThat(t.getStepupTokenExpiration()).isEqualTo(10);
    assertThat(t.getStepupTokenExpirationUnit()).isEqualTo("minutes");
    assertThat(t.getSsoSetupSuiteSettings().getEnabled()).isTrue();
    assertThat(t.getSsoSetupSuiteSettings().getStyleId()).isEqualTo("style");
    assertThat(t.getSsoSetupSuiteSettings().getDisabledFeatures()).isEqualTo(
        new SSOSetupSuiteSettingsDisabledFeatures(true, true, false, true, true, true, true, true, true));
  }

  @Test
  void testGetSettingsWithNullSsoSetupSuiteSettings() throws Exception {
    TenantSettings t = parse("{\"ssoSetupSuiteSettings\":null}", TenantSettings.class);
    assertThat(t.getSsoSetupSuiteSettings()).isNull();
  }

  @Test
  void testConfigureSettingsSendsNewFields() throws Exception {
    ApiProxy apiProxy = mock(ApiProxy.class);
    TenantSettings settings = TenantSettings.builder()
        .stepupTokenExpiration(10)
        .stepupTokenExpirationUnit("minutes")
        .ssoSetupSuiteSettings(SSOSetupSuiteSettings.builder()
            .enabled(false)
            .styleId("style")
            .disabledFeatures(SSOSetupSuiteSettingsDisabledFeatures.builder().roleMapping(true).scim(false).build())
            .build())
        .build();
    Sent sent = call(apiProxy, s -> s.configureSettings("tid", settings));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), eq(Void.class));
    assertMgmtAuth(sent);
    assertThat(sent.path()).isEqualTo("/v1/mgmt/tenant/settings");
    assertThat(sent.json()).isEqualTo(tree("{\"tenantId\":\"tid\",\"stepupTokenExpiration\":10,"
        + "\"stepupTokenExpirationUnit\":\"minutes\",\"ssoSetupSuiteSettings\":{\"enabled\":false,"
        + "\"styleId\":\"style\",\"disabledFeatures\":{\"scim\":false,\"roleMapping\":true}}}"));
  }

  @Test
  void testConfigureSettingsOmitsUnsetNewFields() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    Sent sent = call(apiProxy, s -> s.configureSettings("tid", TenantSettings.builder().inactivityTime(3).build()));
    verify(apiProxy, times(1)).post(sent.uri.capture(), sent.body.capture(), any());
    assertThat(sent.json()).isEqualTo(MAPPER.createObjectNode().put("tenantId", "tid").put("inactivityTime", 3));
  }

  @Test
  void testLegacyConstructorsKeepNewFieldsUnset() {
    Tenant t = new Tenant("id", "n", null, null, null, null, null, null, null, null, null);
    assertThat(t.getId()).isEqualTo("id");
    assertThat(t.getPermittedIps()).isNull();
    TenantSettings s = new TenantSettings(null, null, true, null, null, null, null, null, null, null, null, null);
    assertThat(s.getSessionSettingsEnabled()).isTrue();
    assertThat(s.getSsoSetupSuiteSettings()).isNull();
    TenantSearchRequest r = new TenantSearchRequest(Arrays.asList("id"), null, null, null, null);
    assertThat(r.getIds()).containsExactly("id");
    assertThat(r.getTopLevelOnly()).isNull();
  }
}

