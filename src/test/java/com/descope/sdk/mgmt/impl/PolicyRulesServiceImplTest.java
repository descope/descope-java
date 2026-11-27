package com.descope.sdk.mgmt.impl;

import static com.descope.utils.CollectionUtils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.client.SdkInfo;
import com.descope.model.policy.PolicyRule;
import com.descope.model.policy.PolicyRuleCondition;
import com.descope.model.policy.PolicyRuleGrant;
import com.descope.model.policy.PolicyRuleRequest;
import com.descope.model.policy.PolicyRuleResourceTarget;
import com.descope.model.policy.PolicyRuleResponse;
import com.descope.model.policy.PolicyRuleSearchField;
import com.descope.model.policy.PolicyRuleSearchRequest;
import com.descope.model.policy.PolicyRuleSearchResponse;
import com.descope.model.policy.PolicyRuleSortField;
import com.descope.model.policy.PolicyRulesResponse;
import com.descope.model.policy.ReorderPolicyRuleRequest;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.PolicyRulesService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class PolicyRulesServiceImplTest {

  private static final String MANAGEMENT_KEY = "test-key";

  private final PolicyRule mockRule = PolicyRule.builder().id("rule-id").name("rule-name").build();
  private PolicyRulesService policyRulesService;

  @BeforeEach
  void setUp() {
    Client client = Client.builder()
        .uri("https://api.descope.com")
        .projectId(TestUtils.PROJECT_ID)
        .managementKey(MANAGEMENT_KEY)
        .sdkInfo(SdkInfo.builder().name("java").build())
        .build();
    this.policyRulesService = ManagementServiceBuilder.buildServices(client).getPolicyRulesService();
  }

  @Test
  void testMethodsForMissingRequestParts() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> policyRulesService.createPolicyRule(null));
    assertEquals("The rule argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> policyRulesService.createPolicyRule(PolicyRule.builder().build()));
    assertEquals("The rule.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> policyRulesService.updatePolicyRule(null));
    assertEquals("The rule argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> policyRulesService.updatePolicyRule(PolicyRule.builder().name("a").build()));
    assertEquals("The rule.id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> policyRulesService.updatePolicyRule(PolicyRule.builder().id("a").build()));
    assertEquals("The rule.name argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> policyRulesService.deletePolicyRule(""));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> policyRulesService.loadPolicyRule(""));
    assertEquals("The id argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class, () -> policyRulesService.reorderPolicyRule("", 1));
    assertEquals("The ruleId argument is invalid", thrown.getMessage());
  }

  @Test
  void testMissingRequestPartsSendNothing() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      assertThrows(ServerCommonException.class, () -> policyRulesService.createPolicyRule(null));
      assertThrows(ServerCommonException.class, () -> policyRulesService.updatePolicyRule(null));
      assertThrows(ServerCommonException.class, () -> policyRulesService.deletePolicyRule(null));
      assertThrows(ServerCommonException.class, () -> policyRulesService.loadPolicyRule(null));
      assertThrows(ServerCommonException.class, () -> policyRulesService.reorderPolicyRule(null, 1));
    }
    verify(apiProxy, never()).post(any(), any(), any());
  }

  @Test
  void testCreatePolicyRuleSuccess() {
    PolicyRule rule = PolicyRule.builder().name("rule-name").build();
    Call call = run(PolicyRuleResponse.builder().policyRule(mockRule).build(),
        () -> assertSame(mockRule, policyRulesService.createPolicyRule(rule)));
    call.assertSent("/v1/mgmt/policies/rule/create", new PolicyRuleRequest(rule));
  }

  @Test
  void testUpdatePolicyRuleSuccess() {
    Call call = run(PolicyRuleResponse.builder().policyRule(mockRule).build(),
        () -> assertSame(mockRule, policyRulesService.updatePolicyRule(mockRule)));
    call.assertSent("/v1/mgmt/policies/rule/update", new PolicyRuleRequest(mockRule));
  }

  @Test
  void testDeletePolicyRuleSuccess() {
    Call call = run(null, () -> policyRulesService.deletePolicyRule("rule-id"));
    call.assertSent("/v1/mgmt/policies/rule/delete", mapOf("id", "rule-id"));
  }

  @Test
  void testLoadPolicyRuleSuccess() {
    Call call = run(PolicyRuleResponse.builder().policyRule(mockRule).build(),
        () -> assertSame(mockRule, policyRulesService.loadPolicyRule("rule-id")));
    call.assertSent("/v1/mgmt/policies/rule/load", mapOf("id", "rule-id"));
  }

  @Test
  void testReorderPolicyRuleSuccess() {
    List<PolicyRule> rules = Arrays.asList(mockRule);
    Call call = run(PolicyRulesResponse.builder().policyRules(rules).build(),
        () -> assertSame(rules, policyRulesService.reorderPolicyRule("rule-id", 2)));
    call.assertSent("/v1/mgmt/policies/rule/reorder", new ReorderPolicyRuleRequest("rule-id", 2));
  }

  @Test
  void testSearchPolicyRulesSuccess() {
    PolicyRuleSearchRequest request = PolicyRuleSearchRequest.builder().text("abc").enabled(true).build();
    PolicyRuleSearchResponse response = PolicyRuleSearchResponse.builder()
        .policyRules(Arrays.asList(mockRule)).total(1).build();
    Call call = run(response, () -> assertSame(response, policyRulesService.searchPolicyRules(request)));
    call.assertSent("/v1/mgmt/policies/rule/search", request);
  }

  @Test
  void testSearchPolicyRulesNullRequestSendsEmptyFilters() {
    Call call = run(PolicyRuleSearchResponse.builder().build(), () -> policyRulesService.searchPolicyRules(null));
    call.assertSent("/v1/mgmt/policies/rule/search", PolicyRuleSearchRequest.builder().build());
  }

  @Test
  void testPolicyRuleRequestSerialization() throws Exception {
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    PolicyRule rule = PolicyRule.builder()
        .id("rule-id")
        .version(12345678901L)
        .name("rule-name")
        .enabled(false)
        .conditions(Arrays.asList(PolicyRuleCondition.builder()
            .key("k").operator(PolicyRuleCondition.OPERATOR_IN).value(Arrays.asList("a", "b")).build()))
        .build();
    JsonNode json = mapper.readTree(mapper.writeValueAsString(new PolicyRuleRequest(rule)));
    JsonNode sent = json.get("policyRule");
    assertTrue(sent.get("version").isTextual());
    assertEquals("12345678901", sent.get("version").asText());
    assertEquals("rule-name", sent.get("name").asText());
    assertFalse(sent.get("enabled").asBoolean());
    assertEquals("in", sent.get("conditions").get(0).get("operator").asText());
    assertEquals("b", sent.get("conditions").get(0).get("value").get(1).asText());
    assertFalse(sent.has("description"));
    assertFalse(sent.has("createdTime"));

    JsonNode reorder = mapper.readTree(mapper.writeValueAsString(new ReorderPolicyRuleRequest("rule-id", 3)));
    assertEquals("rule-id", reorder.get("ruleId").asText());
    assertEquals(3, reorder.get("position").asInt());

    PolicyRuleSearchRequest search = PolicyRuleSearchRequest.builder()
        .searchFields(Arrays.asList(PolicyRuleSearchField.builder().field("f").valStr("v").build()))
        .sort(Arrays.asList(PolicyRuleSortField.builder().field("name").desc(true).build()))
        .batchSize(10)
        .actionKind(Arrays.asList("ak"))
        .build();
    JsonNode searchJson = mapper.readTree(mapper.writeValueAsString(search));
    assertEquals("f", searchJson.get("searchFields").get(0).get("field").asText());
    assertTrue(searchJson.get("sort").get(0).get("desc").asBoolean());
    assertEquals(10, searchJson.get("batchSize").asInt());
    assertEquals("ak", searchJson.get("actionKind").get(0).asText());
    assertFalse(searchJson.has("page"));
    assertFalse(searchJson.has("enabled"));
  }

  @Test
  void testPolicyRuleResponseDeserialization() throws Exception {
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String json = "{\"policyRule\":{\"id\":\"rule-id\",\"version\":\"12345678901\",\"name\":\"n\","
        + "\"description\":\"d\",\"enabled\":true,\"ruleFamily\":\"rf\",\"actionKinds\":[\"ak\"],"
        + "\"effect\":\"allow\",\"principalType\":\"pt\",\"principalSelector\":[\"ps\"],"
        + "\"resourceTargets\":[{\"type\":\"t\",\"allOfType\":true,\"ids\":[\"i\"]}],"
        + "\"grants\":[{\"scopes\":[\"s\"],\"allowedAudiences\":[\"aud\"],\"allScopes\":true}],"
        + "\"conditions\":[{\"key\":\"k\",\"operator\":\"equal\",\"value\":\"v\"}],"
        + "\"createdTime\":1,\"modifiedTime\":2,\"evaluationOrder\":3,\"expireTime\":4,\"unknown\":5}}";
    PolicyRule rule = mapper.readValue(json, PolicyRuleResponse.class).getPolicyRule();
    assertEquals("rule-id", rule.getId());
    assertEquals(12345678901L, rule.getVersion());
    assertEquals("n", rule.getName());
    assertEquals("d", rule.getDescription());
    assertTrue(rule.getEnabled());
    assertEquals("rf", rule.getRuleFamily());
    assertEquals(Arrays.asList("ak"), rule.getActionKinds());
    assertEquals("allow", rule.getEffect());
    assertEquals("pt", rule.getPrincipalType());
    assertEquals(Arrays.asList("ps"), rule.getPrincipalSelector());
    PolicyRuleResourceTarget target = rule.getResourceTargets().get(0);
    assertEquals("t", target.getType());
    assertTrue(target.getAllOfType());
    assertEquals(Arrays.asList("i"), target.getIds());
    PolicyRuleGrant grant = rule.getGrants().get(0);
    assertEquals(Arrays.asList("s"), grant.getScopes());
    assertEquals(Arrays.asList("aud"), grant.getAllowedAudiences());
    assertTrue(grant.getAllScopes());
    PolicyRuleCondition condition = rule.getConditions().get(0);
    assertEquals("k", condition.getKey());
    assertEquals(PolicyRuleCondition.OPERATOR_EQUAL, condition.getOperator());
    assertEquals("v", condition.getValue());
    assertEquals(1, rule.getCreatedTime());
    assertEquals(2, rule.getModifiedTime());
    assertEquals(3, rule.getEvaluationOrder());
    assertEquals(4, rule.getExpireTime());

    assertEquals(7L, mapper.readValue("{\"version\":7}", PolicyRule.class).getVersion());
    assertNull(mapper.readValue("{\"policyRule\":null}", PolicyRuleResponse.class).getPolicyRule());

    PolicyRuleSearchResponse search = mapper.readValue(
        "{\"policyRules\":[{\"id\":\"a\"},{\"id\":\"b\"}],\"total\":2}", PolicyRuleSearchResponse.class);
    assertEquals(2, search.getPolicyRules().size());
    assertEquals(2, search.getTotal());
    List<PolicyRule> reordered = mapper.readValue("{\"policyRules\":[{\"id\":\"a\",\"evaluationOrder\":1}]}",
        PolicyRulesResponse.class).getPolicyRules();
    assertEquals(1, reordered.get(0).getEvaluationOrder());
  }

  private static final class Call {
    private final ApiProxy apiProxy;
    private final String authorization;

    private Call(ApiProxy apiProxy, String authorization) {
      this.apiProxy = apiProxy;
      this.authorization = authorization;
    }

    // Every call is a POST to the given path, sent with the management key.
    void assertSent(String path, Object body) {
      ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
      verify(apiProxy).post(uri.capture(), sent.capture(), any());
      assertEquals(path, uri.getValue().getPath());
      assertEquals(body, sent.getValue());
      assertEquals("Bearer " + TestUtils.PROJECT_ID + ":" + MANAGEMENT_KEY, authorization);
    }
  }

  @SuppressWarnings("unchecked")
  private static Call run(Object response, Runnable call) {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authorization = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authorization.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.run();
    }
    return new Call(apiProxy, authorization.get());
  }
}
