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

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.response.GetAllTenantsResponse;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.TenantService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class TenantServiceImplTest {

  private final List<String> selfProvisioningDomains = List.of("domain1", "domain2");
  Tenant mockTenant = Tenant.builder()
      .id("id")
      .name("name")
      .selfProvisioningDomains(selfProvisioningDomains)
      .build();

  private TenantService tenantService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getManagementParams();
    var client = TestUtils.getClient();
    this.tenantService = ManagementServiceBuilder.buildServices(client, authParams).getTenantService();
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = tenantService.create("someName", selfProvisioningDomains);
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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockTenant).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      tenantService.delete("someId");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testLoadAllForSuccess() {
    var mockTenantsResponse = GetAllTenantsResponse.builder().tenants(List.of(mockTenant)).build();
    var apiProxy = mock(ApiProxy.class);
    doReturn(mockTenantsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
          () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var response = tenantService.loadAll();
      assertThat(response.size()).isEqualTo(1);
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, List.of(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    var tenants = tenantService.loadAll();
    assertThat(tenants).isNotEmpty();
    boolean found = false;
    for (var t : tenants) {
      if (t.getId().equals(tenantId)) {
        found = true;
        assertEquals(name, t.getName());
        assertThat(t.getSelfProvisioningDomains()).containsOnly(name + ".com", name + "1.com");
      }
    }
    assertTrue(found);
    tenantService.update(tenantId, name + "1", List.of(name + ".com"), null);
    tenants = tenantService.loadAll();
    assertThat(tenants).isNotEmpty();
    found = false;
    for (var t : tenants) {
      if (t.getId().equals(tenantId)) {
        found = true;
        assertEquals(name + "1", t.getName());
        assertThat(t.getSelfProvisioningDomains()).containsOnly(name + ".com");
      }
    }

    var tenantSearchRequest = TenantSearchRequest.builder().names(List.of(name + "1")).build();
    tenants = tenantService.searchAll(tenantSearchRequest);
    assertThat(tenants).isNotEmpty();
    found = false;
    for (var t : tenants) {
      if (t.getId().equals(tenantId)) {
        found = true;
        assertEquals(name + "1", t.getName());
      }
    }

    tenantSearchRequest = TenantSearchRequest.builder().names(List.of("doesnotexists")).build();
    tenants = tenantService.searchAll(tenantSearchRequest);
    assertThat(tenants).isEmpty();
    tenantService.delete(tenantId);
  }
}
