package com.descope.sdk.impl;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.sso.AttributeMapping;
import com.descope.model.sso.RoleMapping;
import com.descope.model.sso.SSOSettingsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.SsoService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static com.descope.sdk.impl.PasswordServiceImplTest.MOCK_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SsoServiceImplTest {

  private SsoService ssoService;

  @BeforeEach
  void setUp() {
    var authParams = ManagementParams.builder().projectId(MOCK_PROJECT_ID).build();
    var client = Client.builder().uri("https://api.descope.com/v1").build();
    this.ssoService = ManagementServiceBuilder.buildServices(client, authParams).getSsoService();
  }

  @Test
  void testGetSettingsForEmptyTenantId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService.getSettings(""));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetSettingsForSuccess() {
    var ssoSettingsResponse = mock(SSOSettingsResponse.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(ssoSettingsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      var response = ssoService.getSettings("someTenantID");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testDeleteSettingsForEmptyTenantId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService.deleteSettings(""));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteSettingsForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).delete(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      ssoService.deleteSettings("someTenantID");
      verify(apiProxy, times(1)).delete(any(), any(), any());
    }
  }

  @Test
  void testConfigureSettingsForEmptyTenantId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService.configureSettings(""
        , "idpUrl", "idpCert", "entryId", "redirectUrl", "domain"));
    assertNotNull(thrown);
    assertEquals("The TenantID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyIdpURL() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService.configureSettings("someTenantID"
        , "", "idpCert", "entryId", "redirectUrl", "domain"));
    assertNotNull(thrown);
    assertEquals("The IdpURL argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyIdpCert() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService.configureSettings("someTenantID"
        , "idpUrl", "", "entryId", "redirectUrl", "domain"));
    assertNotNull(thrown);
    assertEquals("The IdpCert argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyEntityID() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService.configureSettings("someTenantID"
        , "idpUrl", "idpCert", "", "redirectUrl", "domain"));
    assertNotNull(thrown);
    assertEquals("The EntityID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyRedirectURL() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService
        .configureSettings("someTenantID", "idpUrl", "idpCert", "entryId", "", "domain"));
    assertNotNull(thrown);
    assertEquals("The RedirectURL argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      ssoService.configureSettings("someTenantID", "idpUrl", "idpCert", "entryId"
          , "redirectUrl", "domain");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testConfigureMetadataForEmptyTenantId() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService
        .configureMetadata("", "idpMetaDataUrl"));
    assertNotNull(thrown);
    assertEquals("The TenantID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureMetadataForEmptyIdpMetaDataURL() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService
        .configureMetadata("someTenantID", ""));
    assertNotNull(thrown);
    assertEquals("The IdpMetadataURL argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureMetadataForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      ssoService.configureMetadata("someTenantID", "idpMetaDataUrl");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testConfigureMappingForEmptyTenantId() {
    var mockRoleMapping = Mockito.mock(RoleMapping.class);
    var attribueMapping = Mockito.mock(AttributeMapping.class);
    ServerCommonException thrown = assertThrows(ServerCommonException.class, () -> ssoService
        .configureMapping("", List.of(mockRoleMapping), attribueMapping));
    assertNotNull(thrown);
    assertEquals("The TenantID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureMappingForSuccess() {
    var mockRoleMapping = Mockito.mock(RoleMapping.class);
    var attribueMapping = Mockito.mock(AttributeMapping.class);
    var apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any())).thenReturn(apiProxy);
      ssoService.configureMapping("someTenantID", List.of(mockRoleMapping), attribueMapping);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }
}
