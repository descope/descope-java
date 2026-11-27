package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.authaccess.AuthAccessAction;
import com.descope.model.authaccess.AuthAccessRule;
import com.descope.model.authaccess.AuthAccessRules;
import com.descope.model.authaccess.AuthAccessSettings;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.mgmt.AuthAccessService;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class AuthAccessServiceImplTest {

  private static final String PROJECT_ID = "P123456789012345678901234567";
  private static final String MANAGEMENT_KEY = "some-management-key";
  private static final String BASE = "https://api.descope.com";

  private final Map<String, Object> condition = new HashMap<>();
  private AuthAccessRule rule;
  private AuthAccessSettings settings;
  private AuthAccessService service;
  private ApiProxy apiProxy;
  private final AtomicReference<String> bearer = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    condition.put("key", "request.country");
    condition.put("operator", "in");
    condition.put("values", Arrays.asList("US", "IL"));
    rule = AuthAccessRule.builder()
        .id("r1")
        .name("n")
        .description("d")
        .enabled(true)
        .effect(AuthAccessAction.BLOCK)
        .conditions(Collections.singletonList(condition))
        .createdTime(1)
        .modifiedTime(2)
        .evaluationOrder(3)
        .expireTime(4)
        .build();
    settings = AuthAccessSettings.builder()
        .defaultAction(AuthAccessAction.ALLOW)
        .ruleIds(Arrays.asList("r1", "r2"))
        .build();
    Client client = Client.builder()
        .uri(BASE)
        .projectId(PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    service = ManagementServiceBuilder.buildServices(client).getAuthAccessService();
    apiProxy = mock(ApiProxy.class);
  }

  @SuppressWarnings("unchecked")
  private void run(Executable call) throws Throwable {
    try (MockedStatic<ApiProxyBuilder> mocked = mockStatic(ApiProxyBuilder.class)) {
      mocked.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        bearer.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.execute();
    }
    assertEquals("Bearer " + PROJECT_ID + ":" + MANAGEMENT_KEY, bearer.get());
  }

  private URI capturedGetUri(Class<?> clz) {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).get(uri.capture(), eq(clz));
    return uri.getValue();
  }

  private URI capturedPostUri(Object body, Class<?> clz) {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).post(uri.capture(), eq(body), eq(clz));
    return uri.getValue();
  }

  private URI capturedDeleteUri() {
    ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
    verify(apiProxy).delete(uri.capture(), isNull(), eq(Void.class));
    return uri.getValue();
  }

  private void assertRule(AuthAccessRule res) {
    assertEquals("r1", res.getId());
    assertEquals("n", res.getName());
    assertEquals("d", res.getDescription());
    assertTrue(res.getEnabled());
    assertEquals(AuthAccessAction.BLOCK, res.getEffect());
    assertEquals(Collections.singletonList(condition), res.getConditions());
    assertEquals(1, res.getCreatedTime());
    assertEquals(2, res.getModifiedTime());
    assertEquals(3, res.getEvaluationOrder());
    assertEquals(4, res.getExpireTime());
  }

  private void assertSettings(AuthAccessSettings res) {
    assertEquals(AuthAccessAction.ALLOW, res.getDefaultAction());
    assertEquals(Arrays.asList("r1", "r2"), res.getRuleIds());
  }

  @Test
  void testLoadRule() throws Throwable {
    doReturn(rule).when(apiProxy).get(any(), eq(AuthAccessRule.class));
    AtomicReference<AuthAccessRule> res = new AtomicReference<>();
    run(() -> res.set(service.loadRule("r1")));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule?id=r1", capturedGetUri(AuthAccessRule.class).toString());
    assertRule(res.get());
  }

  @Test
  void testLoadTenantRule() throws Throwable {
    doReturn(rule).when(apiProxy).get(any(), eq(AuthAccessRule.class));
    AtomicReference<AuthAccessRule> res = new AtomicReference<>();
    run(() -> res.set(service.loadTenantRule("t 1", "r1")));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/tenant/t%201?id=r1",
        capturedGetUri(AuthAccessRule.class).toString());
    assertRule(res.get());
  }

  @Test
  void testLoadAllRules() throws Throwable {
    doReturn(new AuthAccessRules(Collections.singletonList(rule))).when(apiProxy).get(any(), any());
    AtomicReference<List<AuthAccessRule>> res = new AtomicReference<>();
    run(() -> res.set(service.loadAllRules()));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/all", capturedGetUri(AuthAccessRules.class).toString());
    assertEquals(1, res.get().size());
    assertRule(res.get().get(0));
  }

  @Test
  void testLoadAllRulesEmpty() throws Throwable {
    doReturn(new AuthAccessRules()).when(apiProxy).get(any(), any());
    AtomicReference<List<AuthAccessRule>> res = new AtomicReference<>();
    run(() -> res.set(service.loadAllRules()));
    assertTrue(res.get().isEmpty());
  }

  @Test
  void testLoadAllTenantRules() throws Throwable {
    doReturn(new AuthAccessRules(Collections.singletonList(rule))).when(apiProxy).get(any(), any());
    AtomicReference<List<AuthAccessRule>> res = new AtomicReference<>();
    run(() -> res.set(service.loadAllTenantRules("t1")));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/all/tenant/t1", capturedGetUri(AuthAccessRules.class).toString());
    assertRule(res.get().get(0));
  }

  @Test
  void testCreateRule() throws Throwable {
    AuthAccessRule req = AuthAccessRule.builder().name("n").effect(AuthAccessAction.BLOCK).build();
    doReturn(rule).when(apiProxy).post(any(), any(), any());
    AtomicReference<AuthAccessRule> res = new AtomicReference<>();
    run(() -> res.set(service.createRule(req)));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule", capturedPostUri(req, AuthAccessRule.class).toString());
    assertRule(res.get());
  }

  @Test
  void testCreateTenantRule() throws Throwable {
    AuthAccessRule req = AuthAccessRule.builder().name("n").effect(AuthAccessAction.ALLOW).build();
    doReturn(rule).when(apiProxy).post(any(), any(), any());
    AtomicReference<AuthAccessRule> res = new AtomicReference<>();
    run(() -> res.set(service.createTenantRule("t1", req)));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/tenant/t1",
        capturedPostUri(req, AuthAccessRule.class).toString());
    assertRule(res.get());
  }

  @Test
  void testDeleteRule() throws Throwable {
    run(() -> service.deleteRule("r1"));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule?id=r1", capturedDeleteUri().toString());
  }

  @Test
  void testDeleteTenantRule() throws Throwable {
    run(() -> service.deleteTenantRule("t1", "r1"));
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/tenant/t1?id=r1", capturedDeleteUri().toString());
  }

  @Test
  void testReorderRule() throws Throwable {
    doReturn(new AuthAccessRules(Collections.singletonList(rule))).when(apiProxy).post(any(), any(), any());
    AtomicReference<List<AuthAccessRule>> res = new AtomicReference<>();
    run(() -> res.set(service.reorderRule("r1", 2)));
    Map<String, Object> body = new HashMap<>();
    body.put("id", "r1");
    body.put("position", 2);
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/reorder", capturedPostUri(body, AuthAccessRules.class).toString());
    assertRule(res.get().get(0));
  }

  @Test
  void testReorderTenantRule() throws Throwable {
    doReturn(new AuthAccessRules(Collections.singletonList(rule))).when(apiProxy).post(any(), any(), any());
    AtomicReference<List<AuthAccessRule>> res = new AtomicReference<>();
    run(() -> res.set(service.reorderTenantRule("t1", "r1", 2)));
    Map<String, Object> body = new HashMap<>();
    body.put("tenantId", "t1");
    body.put("id", "r1");
    body.put("position", 2);
    assertEquals(BASE + "/v1/mgmt/authaccess/rule/reorder/tenant/t1",
        capturedPostUri(body, AuthAccessRules.class).toString());
    assertRule(res.get().get(0));
  }

  @Test
  void testGetSettings() throws Throwable {
    doReturn(settings).when(apiProxy).get(any(), any());
    AtomicReference<AuthAccessSettings> res = new AtomicReference<>();
    run(() -> res.set(service.getSettings()));
    assertEquals(BASE + "/v1/mgmt/authaccess/settings", capturedGetUri(AuthAccessSettings.class).toString());
    assertSettings(res.get());
  }

  @Test
  void testGetTenantSettings() throws Throwable {
    doReturn(settings).when(apiProxy).get(any(), any());
    AtomicReference<AuthAccessSettings> res = new AtomicReference<>();
    run(() -> res.set(service.getTenantSettings("t1")));
    assertEquals(BASE + "/v1/mgmt/authaccess/settings/tenant/t1",
        capturedGetUri(AuthAccessSettings.class).toString());
    assertSettings(res.get());
  }

  @Test
  void testConfigureSettings() throws Throwable {
    doReturn(settings).when(apiProxy).post(any(), any(), any());
    AtomicReference<AuthAccessSettings> res = new AtomicReference<>();
    run(() -> res.set(service.configureSettings(settings)));
    assertEquals(BASE + "/v1/mgmt/authaccess/settings",
        capturedPostUri(settings, AuthAccessSettings.class).toString());
    assertSettings(res.get());
  }

  @Test
  void testConfigureTenantSettings() throws Throwable {
    doReturn(settings).when(apiProxy).post(any(), any(), any());
    AtomicReference<AuthAccessSettings> res = new AtomicReference<>();
    run(() -> res.set(service.configureTenantSettings("t1", settings)));
    assertEquals(BASE + "/v1/mgmt/authaccess/settings/tenant/t1",
        capturedPostUri(settings, AuthAccessSettings.class).toString());
    assertSettings(res.get());
  }

  private static void assertInvalid(String arg, Executable call) {
    ServerCommonException thrown = assertThrows(ServerCommonException.class, call);
    assertEquals("The " + arg + " argument is invalid", thrown.getMessage());
  }

  @Test
  void testValidation() {
    AuthAccessRule valid = AuthAccessRule.builder().effect(AuthAccessAction.ALLOW).build();
    assertInvalid("id", () -> service.loadRule(""));
    assertInvalid("tenantId", () -> service.loadTenantRule("", "r1"));
    assertInvalid("id", () -> service.loadTenantRule("t1", null));
    assertInvalid("tenantId", () -> service.loadAllTenantRules(" "));
    assertInvalid("rule", () -> service.createRule(null));
    assertInvalid("rule.effect", () -> service.createRule(AuthAccessRule.builder().name("n").build()));
    assertInvalid("tenantId", () -> service.createTenantRule(null, valid));
    assertInvalid("rule", () -> service.createTenantRule("t1", null));
    assertInvalid("rule.effect", () -> service.createTenantRule("t1", AuthAccessRule.builder().build()));
    assertInvalid("id", () -> service.deleteRule(null));
    assertInvalid("tenantId", () -> service.deleteTenantRule("", "r1"));
    assertInvalid("id", () -> service.deleteTenantRule("t1", ""));
    assertInvalid("id", () -> service.reorderRule("", 1));
    assertInvalid("tenantId", () -> service.reorderTenantRule("", "r1", 1));
    assertInvalid("id", () -> service.reorderTenantRule("t1", "", 1));
    assertInvalid("tenantId", () -> service.getTenantSettings(""));
    assertInvalid("settings", () -> service.configureSettings(null));
    assertInvalid("tenantId", () -> service.configureTenantSettings("", settings));
    assertInvalid("settings", () -> service.configureTenantSettings("t1", null));
    assertNull(bearer.get());
  }
}
