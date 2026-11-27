package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttribute;
import com.descope.model.customattributes.CustomAttributeOption;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.model.sso.IssuerSettings;
import com.descope.model.sso.JWTBearerSettings;
import com.descope.model.ssoapp.OIDCApplicationRequest;
import com.descope.model.ssoapp.SAMLApplicationRequest;
import com.descope.model.ssoapp.SAMLIDPAttributeMappingInfo;
import com.descope.model.ssoapp.SSOAppServiceAccountRole;
import com.descope.model.ssoapp.SSOApplication;
import com.descope.model.ssoapp.SSOApplicationAddSecretResponse;
import com.descope.model.ssoapp.SSOApplicationCustomAttributesResponse;
import com.descope.model.ssoapp.SSOApplicationSecretMeta;
import com.descope.model.ssoapp.SSOApplicationSessionSettings;
import com.descope.model.ssoapp.WSFedApplicationRequest;
import com.descope.model.ssoapp.WellKnownCustomization;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.SsoApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class SsoApplicationServiceImplTest {

  private SsoApplicationService ssoApplicationService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    this.ssoApplicationService = ManagementServiceBuilder.buildServices(client).getSsoApplicationService();
  }

  @Test
  void testCreateWSFedApplicationForSuccess() throws Exception {
    WSFedApplicationRequest req = WSFedApplicationRequest.builder()
        .name("app")
        .realm("urn:realm")
        .replyUrl("https://sp.com/reply")
        .replyAllowedCallbacks(Collections.singletonList("https://sp.com/*"))
        .attributeMapping(Collections.singletonList(new SAMLIDPAttributeMappingInfo("email", "string", "email")))
        .customAttributes(Collections.singletonMap("tier", "gold"))
        .darkLogo("https://logo.com/dark.png")
        .assignedRoleIds(Collections.singletonList("r1"))
        .build();
    Sent sent = send("{\"id\":\"app1\"}", () -> assertEquals("app1", service().createWSFedApplication(req)));
    sent.assertPost("/v1/mgmt/sso/idp/app/wsfed/create");
    Map<?, ?> body = sent.body();
    assertEquals("app", body.get("name"));
    assertEquals("urn:realm", body.get("realm"));
    assertEquals("https://sp.com/reply", body.get("replyUrl"));
    assertEquals(Collections.singletonList("https://sp.com/*"), body.get("replyAllowedCallbacks"));
    assertEquals("email", ((Map<?, ?>) ((List<?>) body.get("attributeMapping")).get(0)).get("name"));
    assertEquals(Collections.singletonMap("tier", "gold"), body.get("customAttributes"));
    assertEquals("https://logo.com/dark.png", body.get("darkLogo"));
    assertEquals(Collections.singletonList("r1"), body.get("assignedRoleIds"));
    assertFalse(body.containsKey("id"));
  }

  @Test
  void testCreateWSFedApplicationForInvalidArguments() {
    assertInvalid("appRequest", () -> service().createWSFedApplication(null));
    assertInvalid("appRequest.Name", () -> service().createWSFedApplication(WSFedApplicationRequest.builder().build()));
  }

  @Test
  void testUpdateWSFedApplicationForSuccess() throws Exception {
    Sent sent = send("{}", () -> service().updateWSFedApplication(
        WSFedApplicationRequest.builder().id("app1").name("app").realm("urn:realm").build()));
    sent.assertPost("/v1/mgmt/sso/idp/app/wsfed/update");
    Map<?, ?> body = sent.body();
    assertEquals("app1", body.get("id"));
    assertEquals("app", body.get("name"));
    assertEquals("urn:realm", body.get("realm"));
  }

  @Test
  void testUpdateWSFedApplicationForInvalidArguments() {
    assertInvalid("appRequest", () -> service().updateWSFedApplication(null));
    assertInvalid("appRequest.Id",
        () -> service().updateWSFedApplication(WSFedApplicationRequest.builder().name("app").build()));
    assertInvalid("appRequest.Name",
        () -> service().updateWSFedApplication(WSFedApplicationRequest.builder().id("app1").build()));
  }

  @Test
  void testGetApplicationSecretWithSecretName() throws Exception {
    Sent sent = send("{\"cleartext\":\"secret\"}",
        () -> assertEquals("secret", service().getApplicationSecret("app1", "s1")));
    sent.assertMethod("GET", "/v1/mgmt/sso/idp/app/secret");
    assertTrue(sent.request.getRequestUri().contains("id=app1"));
    assertTrue(sent.request.getRequestUri().contains("secretName=s1"));

    sent = send("{\"cleartext\":\"secret\"}", () -> assertEquals("secret", service().getApplicationSecret("app1")));
    assertFalse(sent.request.getRequestUri().contains("secretName"));
    sent = send("{\"cleartext\":\"secret\"}", () -> service().getApplicationSecret("app1", ""));
    assertFalse(sent.request.getRequestUri().contains("secretName"));
    assertInvalid("id", () -> service().getApplicationSecret("", "s1"));
  }

  @Test
  void testAddApplicationSecretForSuccess() throws Exception {
    String resp = "{\"meta\":{\"id\":\"sid\",\"name\":\"s1\",\"status\":\"active\",\"expireTime\":1700000000,"
        + "\"createdTime\":1600000000},\"cleartext\":\"secret\"}";
    SSOApplicationAddSecretResponse[] res = new SSOApplicationAddSecretResponse[1];
    Sent sent = send(resp, () -> res[0] = service().addApplicationSecret("app1", "s1", 1700000000));
    sent.assertPost("/v1/mgmt/sso/idp/app/secret/add");
    Map<?, ?> body = sent.body();
    assertEquals("app1", body.get("appId"));
    assertEquals("s1", body.get("name"));
    assertEquals(1700000000, body.get("expireTime"));
    assertEquals("secret", res[0].getCleartext());
    SSOApplicationSecretMeta meta = res[0].getMeta();
    assertEquals("sid", meta.getId());
    assertEquals("s1", meta.getName());
    assertEquals("active", meta.getStatus());
    assertEquals(1700000000, meta.getExpireTime());
    assertEquals(1600000000, meta.getCreatedTime());

    sent = send(resp, () -> service().addApplicationSecret("app1", "s1", null));
    assertFalse(sent.body().containsKey("expireTime"));
  }

  @Test
  void testAddApplicationSecretForInvalidArguments() {
    assertInvalid("id", () -> service().addApplicationSecret("", "s1", null));
    assertInvalid("name", () -> service().addApplicationSecret("app1", "", null));
  }

  @Test
  void testRevokeApplicationSecretForSuccess() throws Exception {
    List<?>[] res = new List<?>[1];
    Sent sent = send("{\"secrets\":[{\"id\":\"sid\",\"name\":\"s2\",\"status\":\"active\"}]}",
        () -> res[0] = service().revokeApplicationSecret("app1", "s1"));
    sent.assertPost("/v1/mgmt/sso/idp/app/secret/revoke");
    Map<?, ?> body = sent.body();
    assertEquals("app1", body.get("appId"));
    assertEquals("s1", body.get("name"));
    assertEquals(1, res[0].size());
    assertEquals("s2", ((SSOApplicationSecretMeta) res[0].get(0)).getName());
  }

  @Test
  void testRevokeApplicationSecretForInvalidArguments() {
    assertInvalid("id", () -> service().revokeApplicationSecret("", "s1"));
    assertInvalid("name", () -> service().revokeApplicationSecret("app1", ""));
  }

  @Test
  void testGetCustomAttributesForSuccess() throws Exception {
    SSOApplicationCustomAttributesResponse[] res = new SSOApplicationCustomAttributesResponse[1];
    Sent sent = send(CUSTOM_ATTRIBUTES_RESPONSE, () -> res[0] = service().getCustomAttributes());
    sent.assertMethod("GET", "/v1/mgmt/sso/idp/app/customattributes");
    assertSSOApplicationCustomAttributesResponse(res[0]);
  }

  @Test
  void testCreateCustomAttributesForSuccess() throws Exception {
    CustomAttribute attr = CustomAttribute.builder()
        .name("tier")
        .type(4)
        .displayName("Tier")
        .options(Collections.singletonList(new CustomAttributeOption("gold", "Gold")))
        .defaultValue("gold")
        .viewPermissions(Collections.singletonList("view"))
        .editPermissions(Collections.singletonList("edit"))
        .build();
    SSOApplicationCustomAttributesResponse[] res = new SSOApplicationCustomAttributesResponse[1];
    Sent sent = send(CUSTOM_ATTRIBUTES_RESPONSE, () -> res[0] = service().createCustomAttributes(
        new CreateCustomAttributesRequest(Collections.singletonList(attr))));
    sent.assertPost("/v1/mgmt/sso/idp/app/customattribute/create");
    Map<?, ?> sentAttr = (Map<?, ?>) ((List<?>) sent.body().get("attributes")).get(0);
    assertEquals("tier", sentAttr.get("name"));
    assertEquals(4, sentAttr.get("type"));
    assertEquals("Tier", sentAttr.get("displayName"));
    assertEquals("gold", ((Map<?, ?>) ((List<?>) sentAttr.get("options")).get(0)).get("value"));
    assertEquals("gold", sentAttr.get("defaultValue"));
    assertEquals(Collections.singletonList("view"), sentAttr.get("viewPermissions"));
    assertEquals(Collections.singletonList("edit"), sentAttr.get("editPermissions"));
    assertFalse(sentAttr.containsKey("editable"));
    assertSSOApplicationCustomAttributesResponse(res[0]);
  }

  @Test
  void testCreateCustomAttributesForInvalidArguments() {
    assertInvalid("Request", () -> service().createCustomAttributes(null));
    assertInvalid("attributes", () -> service().createCustomAttributes(new CreateCustomAttributesRequest()));
    assertInvalid("attribute name", () -> service().createCustomAttributes(
        new CreateCustomAttributesRequest(Collections.singletonList(CustomAttribute.builder().type(1).build()))));
  }

  @Test
  void testDeleteCustomAttributesForSuccess() throws Exception {
    SSOApplicationCustomAttributesResponse[] res = new SSOApplicationCustomAttributesResponse[1];
    Sent sent = send(CUSTOM_ATTRIBUTES_RESPONSE, () -> res[0] = service().deleteCustomAttributes(
        new DeleteCustomAttributesRequest(Collections.singletonList("tier"))));
    sent.assertPost("/v1/mgmt/sso/idp/app/customattribute/delete");
    assertEquals(Collections.singletonList("tier"), sent.body().get("names"));
    assertSSOApplicationCustomAttributesResponse(res[0]);
  }

  @Test
  void testDeleteCustomAttributesForInvalidArguments() {
    assertInvalid("Request", () -> service().deleteCustomAttributes(null));
    assertInvalid("names", () -> service().deleteCustomAttributes(new DeleteCustomAttributesRequest()));
  }

  @Test
  void testLoadNewFields() throws Exception {
    String resp = "{\"id\":\"app1\",\"name\":\"app\",\"appType\":\"wsfed\",\"templateId\":\"t1\","
        + "\"customSettings\":{\"loginPageUrl\":\"https://login.com\",\"sessionSettings\":{\"enabled\":true,"
        + "\"refreshTokenExpiration\":4,\"refreshTokenExpirationUnit\":\"weeks\"}},"
        + "\"wsfedSettings\":{\"realm\":\"urn:realm\",\"replyUrl\":\"https://sp.com/reply\","
        + "\"idpSsoUrl\":\"https://idp.com/sso\",\"replyAllowedCallbacks\":[\"https://sp.com/*\"]},"
        + "\"trustedAppIds\":[\"app2\"],\"customAttributes\":{\"tier\":\"gold\"},"
        + "\"serviceAccountRoles\":[{\"targetAppId\":\"app2\",\"roleIds\":[\"r1\"]}],"
        + "\"createdTime\":1600000000,\"darkLogo\":\"https://logo.com/dark.png\",\"assignedRoleIds\":[\"r2\"],"
        + "\"unknownField\":1}";
    SSOApplication[] res = new SSOApplication[1];
    Sent sent = send(resp, () -> res[0] = service().load("app1"));
    sent.assertMethod("GET", "/v1/mgmt/sso/idp/app/load");
    SSOApplication app = res[0];
    assertEquals("t1", app.getTemplateId());
    assertEquals("https://login.com", app.getCustomSettings().getLoginPageUrl());
    assertEquals(4, app.getCustomSettings().getSessionSettings().getRefreshTokenExpiration());
    assertEquals(SSOApplicationSessionSettings.UNIT_WEEKS,
        app.getCustomSettings().getSessionSettings().getRefreshTokenExpirationUnit());
    assertEquals("urn:realm", app.getWsfedSettings().getRealm());
    assertEquals("https://sp.com/reply", app.getWsfedSettings().getReplyUrl());
    assertEquals("https://idp.com/sso", app.getWsfedSettings().getIdpSsoUrl());
    assertEquals(Collections.singletonList("https://sp.com/*"), app.getWsfedSettings().getReplyAllowedCallbacks());
    assertEquals(Collections.singletonList("app2"), app.getTrustedAppIds());
    assertEquals(Collections.singletonMap("tier", "gold"), app.getCustomAttributes());
    assertEquals("app2", app.getServiceAccountRoles().get(0).getTargetAppId());
    assertEquals(Collections.singletonList("r1"), app.getServiceAccountRoles().get(0).getRoleIds());
    assertEquals(1600000000, app.getCreatedTime());
    assertEquals("https://logo.com/dark.png", app.getDarkLogo());
    assertEquals(Collections.singletonList("r2"), app.getAssignedRoleIds());

    send("{\"id\":\"app1\",\"customSettings\":null,\"wsfedSettings\":null}", () -> res[0] = service().load("app1"));
    assertNull(res[0].getCustomSettings());
    assertNull(res[0].getWsfedSettings());
    assertNull(res[0].getTrustedAppIds());
  }

  @Test
  void testCreateAndUpdateOIDCApplicationNewFields() throws Exception {
    OIDCApplicationRequest req = OIDCApplicationRequest.builder()
        .name("app")
        .claims(Collections.singletonList("email"))
        .backChannelLogoutUrl("https://sp.com/bcl")
        .jwtBearerSettings(JWTBearerSettings.builder()
            .issuers(Collections.singletonMap("https://issuer.com", IssuerSettings.builder()
                .jwksUri("https://issuer.com/jwks").signAlgorithm(IssuerSettings.SIGN_ALGORITHM_RS256).build()))
            .build())
        .sessionSettings(SSOApplicationSessionSettings.builder().enabled(true)
            .refreshTokenExpirationUnit(SSOApplicationSessionSettings.UNIT_DAYS).build())
        .clientType(OIDCApplicationRequest.CLIENT_TYPE_CONFIDENTIAL)
        .approvedRedirectUrls(Collections.singletonList("https://sp.com/cb"))
        .forcePkce(true)
        .clientId("cid")
        .clientSecret("csecret")
        .defaultAudience(OIDCApplicationRequest.AUDIENCE_CLIENT_ID)
        .trustedAppIds(Collections.singletonList("app2"))
        .customAttributes(Collections.singletonMap("tier", "gold"))
        .serviceAccountRoles(Collections.singletonList(new SSOAppServiceAccountRole("app2", null)))
        .wellKnownCustomization(WellKnownCustomization.builder().version(5L).enabled(true).build())
        .logoutPropagation(OIDCApplicationRequest.LOGOUT_PROPAGATION_ALL_APPS)
        .notifyOnForceLogout(false)
        .noScopeBehavior(OIDCApplicationRequest.NO_SCOPE_BEHAVIOR_GRANT_DEFAULT_SCOPES)
        .defaultScopes(Arrays.asList("profile", "email"))
        .build();
    Sent sent = send("{\"id\":\"app1\"}", () -> assertEquals("app1", service().createOIDCApplication(req)));
    sent.assertPost("/v1/mgmt/sso/idp/app/oidc/create");
    Map<?, ?> body = sent.body();
    assertEquals(Collections.singletonList("email"), body.get("claims"));
    assertEquals("https://sp.com/bcl", body.get("backChannelLogoutUrl"));
    Map<?, ?> issuer = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) body.get("jwtBearerSettings")).get("issuers"))
        .get("https://issuer.com");
    assertEquals("RS256", issuer.get("signAlgorithm"));
    assertEquals("days", ((Map<?, ?>) body.get("sessionSettings")).get("refreshTokenExpirationUnit"));
    assertEquals("confidential", body.get("clientType"));
    assertEquals(Boolean.TRUE, body.get("forcePkce"));
    assertEquals("cid", body.get("clientId"));
    assertEquals("csecret", body.get("clientSecret"));
    assertEquals("clientId", body.get("defaultAudience"));
    assertEquals(Collections.singletonMap("tier", "gold"), body.get("customAttributes"));
    assertEquals("app2", ((Map<?, ?>) ((List<?>) body.get("serviceAccountRoles")).get(0)).get("targetAppId"));
    assertEquals("5", ((Map<?, ?>) body.get("wellKnownCustomization")).get("version"));
    assertEquals("allApps", body.get("logoutPropagation"));
    assertEquals(Boolean.FALSE, body.get("notifyOnForceLogout"));
    assertEquals("grantDefaultScopes", body.get("noScopeBehavior"));
    assertEquals(Arrays.asList("profile", "email"), body.get("defaultScopes"));
    assertFalse(body.containsKey("darkLogo"));

    req.setId("app1");
    sent = send("{}", () -> service().updateOIDCApplication(req));
    sent.assertPost("/v1/mgmt/sso/idp/app/oidc/update");
    assertEquals("allApps", sent.body().get("logoutPropagation"));

    // the constructor without the new fields sends none of them
    sent = send("{\"id\":\"app1\"}", () -> service().createOIDCApplication(
        new OIDCApplicationRequest(null, "app", null, true, null, "https://login.com", null)));
    assertEquals(new HashSet<>(Arrays.asList("name", "enabled", "loginPageUrl")), sent.body().keySet());
  }

  @Test
  void testCreateAndUpdateSAMLApplicationNewFields() throws Exception {
    SAMLApplicationRequest req = SAMLApplicationRequest.builder()
        .name("app")
        .errorRedirectUrl("https://sp.com/error")
        .defaultSignatureAlgorithm(SAMLApplicationRequest.SIGNATURE_ALGORITHM_SHA256)
        .customAttributes(Collections.singletonMap("tier", "gold"))
        .darkLogo("https://logo.com/dark.png")
        .assignedRoleIds(Collections.singletonList("r1"))
        .singleLogoutEnabled(true)
        .spSingleLogoutUrl("https://sp.com/slo")
        .spSingleLogoutBinding(SAMLApplicationRequest.SLO_BINDING_SOAP)
        .singleLogoutPropagation(SAMLApplicationRequest.SLO_PROPAGATION_ALL_APPS)
        .singleLogoutScope(SAMLApplicationRequest.SLO_SCOPE_ALL_SESSIONS)
        .notifyOnForceLogout(true)
        .build();
    Sent sent = send("{\"id\":\"app1\"}", () -> assertEquals("app1", service().createSAMLApplication(req)));
    sent.assertPost("/v1/mgmt/sso/idp/app/saml/create");
    Map<?, ?> body = sent.body();
    assertEquals("https://sp.com/error", body.get("errorRedirectUrl"));
    assertEquals("sha256", body.get("defaultSignatureAlgorithm"));
    assertEquals(Collections.singletonMap("tier", "gold"), body.get("customAttributes"));
    assertEquals("https://logo.com/dark.png", body.get("darkLogo"));
    assertEquals(Collections.singletonList("r1"), body.get("assignedRoleIds"));
    assertEquals(Boolean.TRUE, body.get("singleLogoutEnabled"));
    assertEquals("https://sp.com/slo", body.get("spSingleLogoutUrl"));
    assertEquals("soap", body.get("spSingleLogoutBinding"));
    assertEquals("allApps", body.get("singleLogoutPropagation"));
    assertEquals("allSessions", body.get("singleLogoutScope"));
    assertEquals(Boolean.TRUE, body.get("notifyOnForceLogout"));

    req.setId("app1");
    sent = send("{}", () -> service().updateSAMLApplication(req));
    sent.assertPost("/v1/mgmt/sso/idp/app/saml/update");
    assertEquals("soap", sent.body().get("spSingleLogoutBinding"));

    // the constructor without the new fields sends none of them
    sent = send("{\"id\":\"app1\"}", () -> service().createSAMLApplication(new SAMLApplicationRequest(null, "app",
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
    assertEquals(Collections.singleton("name"), sent.body().keySet());
  }

  private static final String CUSTOM_ATTRIBUTES_RESPONSE = "{\"data\":[{\"name\":\"tier\",\"type\":4,"
      + "\"displayName\":\"Tier\",\"options\":[{\"value\":\"gold\",\"label\":\"Gold\"}],"
      + "\"defaultValue\":\"gold\",\"viewPermissions\":[\"view\"],\"editPermissions\":[\"edit\"],"
      + "\"editable\":true}],\"total\":\"1\"}";

  private static void assertSSOApplicationCustomAttributesResponse(SSOApplicationCustomAttributesResponse res) {
    assertEquals(1L, res.getTotal());
    CustomAttribute attr = res.getData().get(0);
    assertEquals("tier", attr.getName());
    assertEquals(4, attr.getType());
    assertEquals("Tier", attr.getDisplayName());
    assertEquals("Gold", attr.getOptions().get(0).getLabel());
    assertEquals("gold", attr.getDefaultValue());
    assertEquals(Collections.singletonList("view"), attr.getViewPermissions());
    assertEquals(Collections.singletonList("edit"), attr.getEditPermissions());
    assertTrue(attr.getEditable());
  }

  private static void assertInvalid(String argument, Executable call) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, call);
    assertEquals("The " + argument + " argument is invalid", thrown.getMessage());
  }

  private static SsoApplicationService service() {
    Client client = TestUtils.getClient();
    client.setManagementKey("test-key");
    return new SsoApplicationServiceImpl(client);
  }

  private static class Sent {
    private ClassicHttpRequest request;

    void assertMethod(String method, String path) {
      assertEquals(method, request.getMethod());
      assertTrue(request.getRequestUri().startsWith(path), request.getRequestUri());
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":test-key", request.getFirstHeader("Authorization").getValue());
    }

    void assertPost(String path) {
      assertMethod("POST", path);
      assertEquals(path, request.getRequestUri());
    }

    Map<?, ?> body() throws IOException {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      request.getEntity().writeTo(bs);
      return new ObjectMapper().readValue(bs.toByteArray(), Map.class);
    }
  }

  // Runs call against a mocked HTTP client answering responseBody, and returns the request it sent.
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Sent send(String responseBody, Runnable call) throws IOException {
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    doAnswer(inv -> {
      ClassicHttpResponse response = mock(ClassicHttpResponse.class);
      when(response.getCode()).thenReturn(200);
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
      when(response.getEntity()).thenReturn(entity);
      return ((HttpClientResponseHandler) inv.getArgument(1)).handleResponse(response);
    }).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
    try (MockedStatic<HttpClients> mockedHttpClients = mockStatic(HttpClients.class)) {
      mockedHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      call.run();
    }
    Sent sent = new Sent();
    sent.request = captor.getValue();
    return sent;
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
