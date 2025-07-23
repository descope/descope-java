package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.enums.TenantAuthType;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.response.GenerateTenantLinkResponse;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.TenantService;
import java.util.Arrays;
import java.util.List;
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
      verify(apiProxy, times(1)).post(any(), any(), any());
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
  void testRevokeSSOConfigurationLinkForEmptyParams() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () 
      -> tenantService.revokeSSOConfigurationLink("", ""));
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
}
