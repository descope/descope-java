package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_POLICY_RULE_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_POLICY_RULE_DELETE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_POLICY_RULE_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_POLICY_RULE_REORDER;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_POLICY_RULE_SEARCH;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_POLICY_RULE_UPDATE;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.policy.PolicyRule;
import com.descope.model.policy.PolicyRuleRequest;
import com.descope.model.policy.PolicyRuleResponse;
import com.descope.model.policy.PolicyRuleSearchRequest;
import com.descope.model.policy.PolicyRuleSearchResponse;
import com.descope.model.policy.PolicyRulesResponse;
import com.descope.model.policy.ReorderPolicyRuleRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.PolicyRulesService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

class PolicyRulesServiceImpl extends ManagementsBase implements PolicyRulesService {

  PolicyRulesServiceImpl(Client client) {
    super(client);
  }

  @Override
  public PolicyRule createPolicyRule(PolicyRule rule) throws DescopeException {
    if (rule == null) {
      throw ServerCommonException.invalidArgument("rule");
    }
    if (StringUtils.isBlank(rule.getName())) {
      throw ServerCommonException.invalidArgument("rule.name");
    }

    ApiProxy apiProxy = getApiProxy();
    PolicyRuleResponse res = apiProxy.post(getUri(MANAGEMENT_POLICY_RULE_CREATE), new PolicyRuleRequest(rule),
        PolicyRuleResponse.class);
    return res.getPolicyRule();
  }

  @Override
  public PolicyRule updatePolicyRule(PolicyRule rule) throws DescopeException {
    if (rule == null) {
      throw ServerCommonException.invalidArgument("rule");
    }
    if (StringUtils.isBlank(rule.getId())) {
      throw ServerCommonException.invalidArgument("rule.id");
    }
    if (StringUtils.isBlank(rule.getName())) {
      throw ServerCommonException.invalidArgument("rule.name");
    }

    ApiProxy apiProxy = getApiProxy();
    PolicyRuleResponse res = apiProxy.post(getUri(MANAGEMENT_POLICY_RULE_UPDATE), new PolicyRuleRequest(rule),
        PolicyRuleResponse.class);
    return res.getPolicyRule();
  }

  @Override
  public void deletePolicyRule(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_POLICY_RULE_DELETE), mapOf("id", id), Void.class);
  }

  @Override
  public PolicyRule loadPolicyRule(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }

    ApiProxy apiProxy = getApiProxy();
    PolicyRuleResponse res = apiProxy.post(getUri(MANAGEMENT_POLICY_RULE_LOAD), mapOf("id", id),
        PolicyRuleResponse.class);
    return res.getPolicyRule();
  }

  @Override
  public List<PolicyRule> reorderPolicyRule(String ruleId, int position) throws DescopeException {
    if (StringUtils.isBlank(ruleId)) {
      throw ServerCommonException.invalidArgument("ruleId");
    }

    ApiProxy apiProxy = getApiProxy();
    PolicyRulesResponse res = apiProxy.post(getUri(MANAGEMENT_POLICY_RULE_REORDER),
        new ReorderPolicyRuleRequest(ruleId, position), PolicyRulesResponse.class);
    return res.getPolicyRules();
  }

  @Override
  public PolicyRuleSearchResponse searchPolicyRules(PolicyRuleSearchRequest request) throws DescopeException {
    if (request == null) {
      request = PolicyRuleSearchRequest.builder().build();
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_POLICY_RULE_SEARCH), request, PolicyRuleSearchResponse.class);
  }
}
