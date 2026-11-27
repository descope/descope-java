package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.model.sso.AttributeMapping;
import com.descope.model.sso.GroupsMapping;
import com.descope.model.sso.OIDCAttributeMapping;
import com.descope.model.sso.RoleMapping;
import com.descope.model.sso.SSOAllSettingsResponse;
import com.descope.model.sso.SSOOIDCSettings;
import com.descope.model.sso.SSOSAMLSettings;
import com.descope.model.sso.SSOSAMLSettingsByMetadata;
import com.descope.model.sso.SSOSettingsResponse;
import com.descope.model.sso.SSOTenantSettingsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.RolesService;
import com.descope.sdk.mgmt.SsoService;
import com.descope.sdk.mgmt.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@SuppressWarnings("deprecation")
class SsoServiceImplTest {

  private SsoService ssoService;
  private TenantService tenantService;
  private RolesService rolesService;


  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmt = ManagementServiceBuilder.buildServices(client);
    this.ssoService = mgmt.getSsoService();
    this.tenantService = mgmt.getTenantService();
    this.rolesService = mgmt.getRolesService();
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
  void testLoadSettingsWithSsoIdForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> ssoService.loadSettings("", "someSsoId"));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadSettingsWithSsoIdForSuccess() {
    SSOTenantSettingsResponse ssoTenantSettingsResponse = mock(SSOTenantSettingsResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(ssoTenantSettingsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SSOTenantSettingsResponse response = ssoService.loadSettings("someTenantID", "someSsoId");
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadSettingsWithNullSsoIdForSuccess() {
    SSOTenantSettingsResponse ssoTenantSettingsResponse = mock(SSOTenantSettingsResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(ssoTenantSettingsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SSOTenantSettingsResponse response = ssoService.loadSettings("someTenantID", null);
      Assertions.assertThat(response).isNotNull();
    }
  }

  @Test
  void testLoadAllSettingsForEmptyTenantId() {
    ServerCommonException thrown =
        assertThrows(ServerCommonException.class, () -> ssoService.loadAllSettings(""));
    assertNotNull(thrown);
    assertEquals("The TenantId argument is invalid", thrown.getMessage());
  }

  @Test
  void testLoadAllSettingsForSuccess() {
    SSOAllSettingsResponse ssoAllSettingsResponse = mock(SSOAllSettingsResponse.class);
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(ssoAllSettingsResponse).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      SSOAllSettingsResponse response = ssoService.loadAllSettings("someTenantID");
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

  @Test
  void testConfigureSAMLSettingsAcceptedAudiences() throws Exception {
    // null, also what the constructor without acceptedAudiences gives, keeps the stored values
    Map<?, ?> settings = sentSAMLSettings("/v1/mgmt/sso/saml", () -> ssoService.configureSAMLSettings("t1",
        new SSOSAMLSettings("https://idp.com", "entityId", "idpCert", null, null, null, null, null, null, null),
        null, null));
    assertEquals("https://idp.com", settings.get("idpUrl"));
    assertFalse(settings.containsKey("acceptedAudiences"));
    assertFalse(settings.containsKey("replaceAcceptedAudiences"));

    settings = sentSAMLSettings("/v1/mgmt/sso/saml", () -> ssoService.configureSAMLSettings("t1",
        samlSettings().acceptedAudiences(Arrays.asList("urn:other:audience", "")).build(), null, null));
    assertEquals(Arrays.asList("urn:other:audience", ""), settings.get("acceptedAudiences"));
    assertEquals(Boolean.TRUE, settings.get("replaceAcceptedAudiences"));

    settings = sentSAMLSettings("/v1/mgmt/sso/saml", () -> ssoService.configureSAMLSettings("t1",
        samlSettings().acceptedAudiences(Collections.emptyList()).build(), null, null));
    assertEquals(Collections.emptyList(), settings.get("acceptedAudiences"));
    assertEquals(Boolean.TRUE, settings.get("replaceAcceptedAudiences"));
  }

  @Test
  void testConfigureSAMLSettingsByMetadataAcceptedAudiences() throws Exception {
    // null, also what the constructor without acceptedAudiences gives, keeps the stored values
    Map<?, ?> settings = sentSAMLSettings("/v1/mgmt/sso/saml/metadata",
        () -> ssoService.configureSAMLSettingsByMetadata("t1",
            new SSOSAMLSettingsByMetadata("https://idp.com/md", null, null, null, null, null, null, null), null, null));
    assertEquals("https://idp.com/md", settings.get("idpMetadataUrl"));
    assertFalse(settings.containsKey("acceptedAudiences"));
    assertFalse(settings.containsKey("replaceAcceptedAudiences"));

    settings = sentSAMLSettings("/v1/mgmt/sso/saml/metadata", () -> ssoService.configureSAMLSettingsByMetadata("t1",
        samlSettingsByMetadata().acceptedAudiences(Arrays.asList("urn:other:audience", "")).build(), null, null));
    assertEquals(Arrays.asList("urn:other:audience", ""), settings.get("acceptedAudiences"));
    assertEquals(Boolean.TRUE, settings.get("replaceAcceptedAudiences"));

    settings = sentSAMLSettings("/v1/mgmt/sso/saml/metadata", () -> ssoService.configureSAMLSettingsByMetadata("t1",
        samlSettingsByMetadata().acceptedAudiences(Collections.emptyList()).build(), null, null));
    assertEquals(Collections.emptyList(), settings.get("acceptedAudiences"));
    assertEquals(Boolean.TRUE, settings.get("replaceAcceptedAudiences"));
  }

  @Test
  void testLoadSettingsAcceptedAudiences() throws Exception {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor,
        "{\"tenant\":{\"id\":\"t1\"},\"saml\":{\"acceptedAudiences\":[\"urn:other:audience\",\"\"]}}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      SSOTenantSettingsResponse resp = ssoService.loadSettings("t1");
      assertTrue(captor.getValue().getRequestUri().contains("/v2/mgmt/sso/settings"));
      assertEquals(Arrays.asList("urn:other:audience", ""), resp.getSaml().getAcceptedAudiences());
    }
    httpClient = mockHttpClient(captor, "{\"tenant\":{\"id\":\"t1\"},\"saml\":{}}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      assertNull(ssoService.loadSettings("t1").getSaml().getAcceptedAudiences());
    }
  }

  @Test
  void testLoadSettingsConfigurationFields() throws Exception {
    SsoService service = ManagementServiceBuilder.buildServices(Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey("test-key")
        .build()).getSsoService();
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor,
        "{\"tenant\":{\"id\":\"t1\"},\"saml\":{\"idpEntityId\":\"e1\"},\"oidc\":{\"name\":\"oidc1\"},"
            + "\"ssoId\":\"sso1\",\"name\":\"Okta\",\"authenticationOnly\":true,\"jitDisabled\":true}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      SSOTenantSettingsResponse resp = service.loadSettings("t1", "sso1");
      ClassicHttpRequest req = captor.getValue();
      assertEquals("GET", req.getMethod());
      assertEquals("/v2/mgmt/sso/settings", req.getUri().getPath());
      assertTrue(req.getUri().getQuery().contains("tenantId=t1"));
      assertTrue(req.getUri().getQuery().contains("ssoId=sso1"));
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", req.getFirstHeader("Authorization").getValue());
      assertEquals("t1", resp.getTenant().getId());
      assertEquals("e1", resp.getSaml().getIdpEntityId());
      assertEquals("oidc1", resp.getOidc().getName());
      assertEquals("sso1", resp.getSsoId());
      assertEquals("Okta", resp.getName());
      assertEquals(Boolean.TRUE, resp.getAuthenticationOnly());
      assertEquals(Boolean.TRUE, resp.getJitDisabled());
    }
    httpClient = mockHttpClient(captor, "{\"tenant\":{\"id\":\"t1\"},\"ssoId\":null,\"extra\":1}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      SSOTenantSettingsResponse resp = service.loadSettings("t1");
      assertFalse(captor.getValue().getUri().getQuery().contains("ssoId"));
      assertNull(resp.getSsoId());
      assertNull(resp.getName());
      assertNull(resp.getAuthenticationOnly());
      assertNull(resp.getJitDisabled());
    }
  }

  private static SSOSAMLSettings.SSOSAMLSettingsBuilder samlSettings() {
    return SSOSAMLSettings.builder().idpUrl("https://idp.com").entityId("entityId").idpCert("idpCert");
  }

  private static SSOSAMLSettingsByMetadata.SSOSAMLSettingsByMetadataBuilder samlSettingsByMetadata() {
    return SSOSAMLSettingsByMetadata.builder().idpMetadataUrl("https://idp.com/md");
  }

  // Runs configure against a mocked HTTP client and returns the "settings" object of the JSON body it sent.
  private Map<?, ?> sentSAMLSettings(String path, Runnable configure) throws IOException {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mockHttpClient(captor, "{}");
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      configure.run();
    }
    ClassicHttpRequest request = captor.getValue();
    assertTrue(request.getRequestUri().endsWith(path));
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    request.getEntity().writeTo(bs);
    Map<?, ?> body = new ObjectMapper().readValue(bs.toByteArray(), Map.class);
    assertEquals("t1", body.get("tenantId"));
    return (Map<?, ?>) body.get("settings");
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
    SSOTenantSettingsResponse beforeUpdateResp = ssoService.loadSettings(tenantId);
    assertEquals(tenantId, beforeUpdateResp.getTenant().getId());
    String encryptCert = beforeUpdateResp.getSaml().getSpCertificate();
    assertThat(encryptCert).isNotBlank();
    String signCert = beforeUpdateResp.getSaml().getSpSignCertificate();
    assertThat(signCert).isNotBlank();
    final String unspecifiedFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    ssoService.configureSAMLSettings(tenantId, SSOSAMLSettings.builder()
        .attributeMapping(AttributeMapping.builder()
            .email("email")
            .name("name")
            .build())
        .entityId("entityId")
        .idpCert("idpCert")
        .idpUrl("https://" + name + ".com")
        .spEncryptionKey(TestUtils.MOCK_PRIVATE_KEY_STRING)
        .spSignKey(TestUtils.MOCK_PRIVATE_KEY_STRING)
        .subjectNameIdFormat(unspecifiedFormat)
        .spACSUrl("https://spacsurl.com")
        .spEntityId("spEntityId")
        .build(), "https://" + name + ".com", null);
    SSOTenantSettingsResponse resp = ssoService.loadSettings(tenantId);
    assertEquals(tenantId, resp.getTenant().getId());
    assertThat(Arrays.asList(name + ".com", name + "1.com")).containsExactly(name + ".com", name + "1.com");
    assertEquals(name, resp.getTenant().getName());
    assertEquals("entityId", resp.getSaml().getIdpEntityId());
    assertEquals("idpCert", resp.getSaml().getIdpCertificate());
    assertEquals("https://" + name + ".com", resp.getSaml().getIdpSSOUrl());
    assertEquals("https://" + name + ".com", resp.getSaml().getRedirectUrl());
    String newEncryptCert = resp.getSaml().getSpCertificate();
    assertThat(newEncryptCert).isNotBlank();
    assertThat(newEncryptCert).isNotEqualTo(encryptCert);
    String newSignCert = resp.getSaml().getSpSignCertificate();
    assertThat(signCert).isNotBlank();
    assertThat(newSignCert).isNotEqualTo(signCert);
    assertThat(unspecifiedFormat).isEqualTo(resp.getSaml().getSubjectNameIdFormat());
    assertEquals("https://spacsurl.com", resp.getSaml().getSpACSUrl());
    assertEquals("spEntityId", resp.getSaml().getSpEntityId());
    ssoService.deleteSettings(tenantId);
    tenantService.delete(tenantId);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleSAMLMetadata() {
    String name = TestUtils.getRandomName("t-");
    String tenantId = tenantService.create(name, Arrays.asList(name + ".com", name + "1.com"));
    assertThat(tenantId).isNotBlank();
    SSOTenantSettingsResponse beforeUpdateResp = ssoService.loadSettings(tenantId);
    assertEquals(tenantId, beforeUpdateResp.getTenant().getId());
    String encryptCert = beforeUpdateResp.getSaml().getSpCertificate();
    assertThat(encryptCert).isNotBlank();
    String signCert = beforeUpdateResp.getSaml().getSpSignCertificate();
    assertThat(signCert).isNotBlank();
    String roleName = TestUtils.getRandomName("rt-").substring(0, 20);
    rolesService.create(roleName, tenantId, "ttt", null);
    final String unspecifiedFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    ssoService.configureSAMLSettingsByMetadata(tenantId, SSOSAMLSettingsByMetadata.builder()
        .attributeMapping(AttributeMapping.builder()
            .email("email")
            .name("name")
            .build())
        .idpMetadataUrl("https://" + name + ".com/md")
        .roleMappings(Arrays.asList(RoleMapping.builder().groups(Arrays.asList("a", "b")).roleName(roleName).build()))
        .spEncryptionKey(TestUtils.MOCK_PRIVATE_KEY_STRING)
        .spSignKey(TestUtils.MOCK_PRIVATE_KEY_STRING)
        .subjectNameIdFormat(unspecifiedFormat)
        .build(), "https://" + name + ".com", null);
    SSOTenantSettingsResponse resp = ssoService.loadSettings(tenantId);
    assertEquals(tenantId, resp.getTenant().getId());
    assertThat(Arrays.asList(name + ".com", name + "1.com")).containsExactly(name + ".com", name + "1.com");
    assertEquals(name, resp.getTenant().getName());
    assertEquals("https://" + name + ".com/md", resp.getSaml().getIdpMetadataUrl());
    List<GroupsMapping> groupsMapping = resp.getSaml().getGroupsMapping();
    assertNotNull(groupsMapping);
    assertThat(groupsMapping).hasSize(1);
    assertThat(groupsMapping.get(0).getRole().getId()).isNotBlank();
    String newEncryptCert = resp.getSaml().getSpCertificate();
    assertThat(newEncryptCert).isNotBlank();
    assertThat(newEncryptCert).isNotEqualTo(encryptCert);
    String newSignCert = resp.getSaml().getSpSignCertificate();
    assertThat(signCert).isNotBlank();
    assertThat(newSignCert).isNotEqualTo(signCert);
    assertThat(unspecifiedFormat).isEqualTo(resp.getSaml().getSubjectNameIdFormat());
    ssoService.deleteSettings(tenantId);
    tenantService.delete(tenantId);
  }
}
