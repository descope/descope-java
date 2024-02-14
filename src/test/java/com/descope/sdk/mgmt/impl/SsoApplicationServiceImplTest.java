package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.descope.exception.RateLimitExceededException;
import com.descope.model.client.Client;
import com.descope.model.ssoapp.OIDCApplicationRequest;
import com.descope.model.ssoapp.SAMLApplicationRequest;
import com.descope.model.ssoapp.SSOApplication;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.SsoApplicationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junitpioneer.jupiter.RetryingTest;

public class SsoApplicationServiceImplTest {

  private SsoApplicationService ssoApplicationService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.ssoApplicationService = ManagementServiceBuilder.buildServices(client).getSsoApplicationService();
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleOIDC() {
    String name = TestUtils.getRandomName("a-");
    String id = ssoApplicationService.createOIDCApplication(OIDCApplicationRequest.builder()
        .name(name)
        .description("test")
        .enabled(true)
        .build());
    assertThat(id).isNotBlank();
    SSOApplication app = ssoApplicationService.load(id);
    assertEquals(name, app.getName());
    assertEquals("test", app.getDescription());
    assertEquals("oidc", app.getAppType());
    ssoApplicationService.updateOIDCApplication(OIDCApplicationRequest.builder()
        .name(name + "1")
        .description("test1")
        .enabled(false)
        .id(id)
        .build());
    List<SSOApplication> apps = ssoApplicationService.loadAll();
    boolean found = false;
    for (SSOApplication a : apps) {
      if (a.getId().equals(id)) {
        found = true;
        assertEquals("test1", a.getDescription());
        assertEquals(name + "1", a.getName());
        assertFalse(a.getEnabled());
      }
    }
    assertTrue(found);
    ssoApplicationService.delete(id);
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycleSAML() {
    String name = TestUtils.getRandomName("a-");
    String id = ssoApplicationService.createSAMLApplication(SAMLApplicationRequest.builder()
        .name(name)
        .description("test")
        .enabled(true)
        .build());
    assertThat(id).isNotBlank();
    SSOApplication app = ssoApplicationService.load(id);
    assertEquals(name, app.getName());
    assertEquals("test", app.getDescription());
    assertEquals("saml", app.getAppType());
    ssoApplicationService.updateSAMLApplication(SAMLApplicationRequest.builder()
        .name(name + "1")
        .description("test1")
        .enabled(false)
        .id(id)
        .build());
    List<SSOApplication> apps = ssoApplicationService.loadAll();
    boolean found = false;
    for (SSOApplication a : apps) {
      if (a.getId().equals(id)) {
        found = true;
        assertEquals("test1", a.getDescription());
        assertEquals(name + "1", a.getName());
        assertFalse(a.getEnabled());
      }
    }
    assertTrue(found);
    ssoApplicationService.delete(id);
  }
}
