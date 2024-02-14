package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.sso.AttributeMapping;
import com.descope.model.sso.OIDCAttributeMapping;
import com.descope.model.sso.RoleMapping;
import com.descope.model.sso.SSOOIDCSettings;
import com.descope.model.sso.SSOSAMLSettings;
import com.descope.model.sso.SSOSettingsResponse;
import com.descope.model.sso.SSOTenantSettingsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.SsoService;
import com.descope.sdk.mgmt.TenantService;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@SuppressWarnings("deprecation")
class SsoServiceImplTest {

  private SsoService ssoService;
  private TenantService tenantService;


  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmt = ManagementServiceBuilder.buildServices(client);
    this.ssoService = mgmt.getSsoService();
    this.tenantService = mgmt.getTenantService();
  }

  @Test
  void testGetSettingsForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> ssoService.getSettings(""));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetSettingsForSuccess() {
    SSOSettingsResponse ssoSettingsResponse = mock(SSOSettingsResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(ssoSettingsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SSOSettingsResponse response = ssoService.getSettings("someTenantID");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testDeleteSettingsForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> ssoService.deleteSettings(""));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteSettingsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).delete(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ssoService.deleteSettings("someTenantID");
      verify(apiProxy, times(1)).delete(any(), any(), any());
    }
  }

  @Test
  void testConfigureSettingsForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                ssoService.configureSettings(
                    "", "idpUrl", "idpCert", "entryId", "redirectUrl", Arrays.asList("domain.com")));
    assertNotNull(thrown);
    assertEquals("The TenantID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyIdpURL() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                ssoService.configureSettings(
                    "someTenantID", "", "idpCert", "entryId", "redirectUrl", Arrays.asList("domain.com")));
    assertNotNull(thrown);
    assertEquals("The IdpURL argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyIdpCert() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                ssoService.configureSettings(
                    "someTenantID", "idpUrl", "", "entryId", "redirectUrl", Arrays.asList("domain.com")));
    assertNotNull(thrown);
    assertEquals("The IdpCert argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyEntityID() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                ssoService.configureSettings(
                    "someTenantID", "idpUrl", "idpCert", "", "redirectUrl", Arrays.asList("domain.com")));
    assertNotNull(thrown);
    assertEquals("The EntityID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForEmptyRedirectURL() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () ->
                ssoService.configureSettings(
                    "someTenantID", "idpUrl", "idpCert", "entryId", "", Arrays.asList("domain.com")));
    assertNotNull(thrown);
    assertEquals("The RedirectURL argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureSettingsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ssoService.configureSettings(
          "someTenantID", "idpUrl", "idpCert", "entryId", "redirectUrl", Arrays.asList("domain.com"));
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testConfigureMetadataForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> ssoService.configureMetadata("", "idpMetaDataUrl"));
    assertNotNull(thrown);
    assertEquals("The TenantID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureMetadataForEmptyIdpMetaDataURL() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class, () -> ssoService.configureMetadata("someTenantID", ""));
    assertNotNull(thrown);
    assertEquals("The IdpMetadataURL argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureMetadataForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ssoService.configureMetadata("someTenantID", "idpMetaDataUrl");
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }

  @Test
  void testConfigureMappingForEmptyTenantId() {
    RoleMapping mockRoleMapping = Mockito.mock(RoleMapping.class);
    AttributeMapping attributeMapping = Mockito.mock(AttributeMapping.class);
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> ssoService.configureMapping("", Arrays.asList(mockRoleMapping), attributeMapping));
    assertNotNull(thrown);
    assertEquals("The TenantID argument is invalid", thrown.getMessage());
  }

  @Test
  void testConfigureMappingForSuccess() {
    RoleMapping mockRoleMapping = Mockito.mock(RoleMapping.class);
    AttributeMapping attributeMapping = Mockito.mock(AttributeMapping.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      ssoService.configureMapping("someTenantID", Arrays.asList(mockRoleMapping), attributeMapping);
      verify(apiProxy, times(1)).post(any(), any(), any());
    }
  }
  
  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleOIDC() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, Arrays.asList(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    ssoService.configureOIDCSettings(tenantId, SSOOIDCSettings.builder()
        .jwksUrl("https://" + name + ".com/jwks")
        .authUrl("https://" + name + ".com/auth")
        .callbackDomain(name + ".com")
        .clientId("xxx")
        .clientSecret("yyy")
        .grantType("implicit")
        .issuer("i")
        .name(name)
        .redirectUrl("https://" + name + ".com/r")
        .tokenUrl("https://" + name + ".com/t")
        .userDataUrl("https://" + name + ".com/ud")
        .userAttrMapping(OIDCAttributeMapping.builder()
            .loginId("loginId")
            .email("email")
            .username("username")
            .name("name")
            .build())
        .build(), null);
    SSOTenantSettingsResponse resp = ssoService.loadSettings(tenantId);
    assertEquals(tenantId, resp.getTenant().getId());
    assertThat(Arrays.asList(name + ".com", name + "1.com")).containsExactly(name + ".com", name + "1.com");
    assertEquals(name, resp.getTenant().getName());
    assertEquals("https://" + name + ".com/jwks", resp.getOidc().getJwksUrl());
    assertEquals("https://" + name + ".com/auth", resp.getOidc().getAuthUrl());
    assertEquals(name + ".com", resp.getOidc().getCallbackDomain());
    assertEquals("xxx", resp.getOidc().getClientId());
    assertEquals("implicit", resp.getOidc().getGrantType());
    assertEquals("i", resp.getOidc().getIssuer());
    assertEquals(name, resp.getOidc().getName());
    assertEquals("https://" + name + ".com/r", resp.getOidc().getRedirectUrl());
    assertEquals("https://" + name + ".com/t", resp.getOidc().getTokenUrl());
    assertEquals("https://" + name + ".com/ud", resp.getOidc().getUserDataUrl());
    ssoService.deleteSettings(tenantId);
    tenantService.delete(tenantId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleSAML() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, Arrays.asList(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    ssoService.configureSAMLSettings(tenantId, SSOSAMLSettings.builder()
        .attributeMapping(AttributeMapping.builder()
            .email("email")
            .name("name")
            .build())
        .entityId("entityId")
        .idpCert("idpCert")
        .idpUrl("https://" + name + ".com")
        .build(), "https://" + name + ".com", null);
    SSOTenantSettingsResponse resp = ssoService.loadSettings(tenantId);
    assertEquals(tenantId, resp.getTenant().getId());
    assertThat(Arrays.asList(name + ".com", name + "1.com")).containsExactly(name + ".com", name + "1.com");
    assertEquals(name, resp.getTenant().getName());
    assertEquals("entityId", resp.getSaml().getIdpEntityId());
    assertEquals("idpCert", resp.getSaml().getIdpCertificate());
    assertEquals("https://" + name + ".com", resp.getSaml().getIdpSSOUrl());
    assertEquals("https://" + name + ".com", resp.getSaml().getRedirectUrl());
    ssoService.deleteSettings(tenantId);
    tenantService.delete(tenantId);
  }
}
