package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppConsent;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppScope;
import com.descope.model.inbound.InboundAppSecret;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.LoadAllApplicationsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.InboundAppsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

class InboundAppsServiceImplTest {

  private final InboundApp mockInboundApp = InboundApp.builder().id("someId").build();
  private final InboundAppSecret mockSecret = InboundAppSecret.builder().secret("secret").build();
  private final InboundAppConsentSearchResponse mockSearchConsentResponse = InboundAppConsentSearchResponse
      .builder().total(1).consents(new InboundAppConsent[] { InboundAppConsent.builder().build() })
      .build();
  private InboundAppsService inboundAppsService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.inboundAppsService = ManagementServiceBuilder.buildServices(client).getInboundAppsService();
  }

  @Test
  void testMethodsForMissingRequestParts() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> inboundAppsService.createApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
      () -> inboundAppsService.createApplication(InboundAppRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The request.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.updateApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
      () -> inboundAppsService.updateApplication(InboundAppRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The request.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService
      .updateApplication(InboundAppRequest.builder().id("a").build()));
    assertNotNull(thrown);
    assertEquals("The request.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.patchApplication(null));
    assertNotNull(thrown);
    assertEquals("The request argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
      () -> inboundAppsService.patchApplication(InboundAppRequest.builder().build()));
    assertNotNull(thrown);
    assertEquals("The request.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.deleteApplication(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.loadApplication(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.getApplicationSecret(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
      () -> inboundAppsService.rotateApplicationSecret(""));
    assertNotNull(thrown);
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.deleteConsents(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.deleteTenantConsents(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> inboundAppsService.searchConsents(null));
    assertNotNull(thrown);
    assertEquals("The options argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(InboundAppCreateResponse.builder().id(mockInboundApp.getId()).build()).when(apiProxy)
      .post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundAppCreateResponse app = inboundAppsService
          .createApplication(InboundAppRequest.builder().name("n1").id("i1").build());
      assertNotNull(app);
      assertEquals("someId", app.getId());
    }
  }

  @Test
  void testUpdateApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.updateApplication(InboundAppRequest.builder().id("a").name("n1").build());
    }
  }

  @Test
  void testPatchApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.patchApplication(InboundAppRequest.builder().id("a").build());
    }
  }

  @Test
  void testDeleteApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.deleteApplication("a");
    }
  }

  @Test
  void testLoadApplicationSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockInboundApp).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundApp app = inboundAppsService.loadApplication("a");
      assertNotNull(app);
      assertEquals("someId", app.getId());
    }
  }

  @Test
  void testGetApplicationSecretSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSecret).when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String secret = inboundAppsService.getApplicationSecret("a");
      assertEquals("secret", secret);
    }
  }

  @Test
  void testRotateApplicationSecretSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSecret).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      String secret = inboundAppsService.rotateApplicationSecret("a");
      assertEquals("secret", secret);
    }
  }

  @Test
  void testLoadAllApplicationsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(LoadAllApplicationsResponse.builder().apps(new InboundApp[] { mockInboundApp }).build())
      .when(apiProxy).get(any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundApp[] apps = inboundAppsService.loadAllApplications();
      assertEquals(1, apps.length);
    }
  }

  @Test
  void testDeleteConsentsSuccess() {    
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.deleteConsents(InboundAppConsentDeleteOptions.builder().build());
    }
  }

  @Test
  void testDeleteTenantConsentsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class); 
    doReturn(Void.class).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {  
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      inboundAppsService.deleteTenantConsents(InboundAppTenantConsentDeleteOptions.builder().build());
    }
  }

  @Test
  void testSearchConsentsSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(mockSearchConsentResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      InboundAppConsentSearchResponse res = inboundAppsService
          .searchConsents(InboundAppConsentSearchOptions.builder().build());
      assertEquals(1, res.getTotal());
      assertEquals(1, res.getConsents().length);
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalInboundApps() {
    // create app
    String name = TestUtils.getRandomName("iban-");
    InboundAppScope[] scopes = new InboundAppScope[] {
        InboundAppScope.builder().name("s1").values(new String[] { "v1", "v2" }).build()};
    InboundAppCreateResponse initialApp = inboundAppsService.createApplication(InboundAppRequest.builder()
        .name(name).id(TestUtils.getRandomName("ibaid-")).permissionsScopes(scopes).build());
    try {
      // get secret
      String secret = inboundAppsService.getApplicationSecret(initialApp.getId());
      assertEquals(initialApp.getSecret(), secret);
      // rotate secret
      secret = inboundAppsService.rotateApplicationSecret(initialApp.getId());
      assertNotEquals(initialApp.getSecret(), secret);
      // get secret
      String newSecret = inboundAppsService.getApplicationSecret(initialApp.getId());
      assertEquals(secret, newSecret);
      // load all apps
      InboundApp[] apps = inboundAppsService.loadAllApplications();
      assertEquals(1, apps.length);
      assertEquals(initialApp.getId(), apps[0].getId());
      // update app
      inboundAppsService.updateApplication(InboundAppRequest.builder().id(initialApp.getId())
          .name(name + "2").permissionsScopes(scopes).build());
      // load app validate
      InboundApp lapp = inboundAppsService.loadApplication(initialApp.getId());
      assertEquals(initialApp.getId(), lapp.getId());
      assertEquals(name + "2", lapp.getName());
      // patch app
      inboundAppsService.patchApplication(
          InboundAppRequest.builder().id(initialApp.getId()).name(name + "3").build());
      // load app validate
      lapp = inboundAppsService.loadApplication(initialApp.getId());
      assertEquals(initialApp.getId(), lapp.getId());
      assertEquals(name + "3", lapp.getName());
    } finally {
      // delete app
      inboundAppsService.deleteApplication(initialApp.getId());
      // load all apps
      InboundApp[] apps = inboundAppsService.loadAllApplications();
      assertEquals(0, apps.length);
    }
  }
}
