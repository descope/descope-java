package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.policy.PolicyRule;
import com.descope.model.policy.PolicyRuleSearchRequest;
import com.descope.model.policy.PolicyRuleSearchResponse;
import java.util.List;

// Provides functions for managing policy rules in a project.
public interface PolicyRulesService {
  /**
   * Create a policy rule. The id is server-generated.
   *
   * @param rule the rule to create; name is required, at most 255 conditions
   * @return the created rule
   * @throws DescopeException if the request fails
   */
  PolicyRule createPolicyRule(PolicyRule rule) throws DescopeException;

  /**
   * Replace an existing policy rule matched by id.
   *
   * @param rule the rule to update; id and name are required, at most 255 conditions
   * @return the updated rule
   * @throws DescopeException if the request fails
   */
  PolicyRule updatePolicyRule(PolicyRule rule) throws DescopeException;

  /**
   * Delete a policy rule by id. Idempotent.
   *
   * @param id the rule id
   * @throws DescopeException if the request fails
   */
  void deletePolicyRule(String id) throws DescopeException;

  /**
   * Load a policy rule by id.
   *
   * @param id the rule id
   * @return the rule
   * @throws DescopeException if the request fails
   */
  PolicyRule loadPolicyRule(String id) throws DescopeException;

  /**
   * Move a policy rule to a 1-based position in the evaluation order; rules in between shift accordingly. Rules
   * are evaluated first-match in this order.
   *
   * @param ruleId the rule id
   * @param position the new 1-based position
   * @return the rules in their new order
   * @throws DescopeException if the request fails
   */
  List<PolicyRule> reorderPolicyRule(String ruleId, int position) throws DescopeException;

  /**
   * Search policy rules. Paged and sorted.
   *
   * @param request optional filters; null returns all rules
   * @return the matching rules and their total
   * @throws DescopeException if the request fails
   */
  PolicyRuleSearchResponse searchPolicyRules(PolicyRuleSearchRequest request) throws DescopeException;
}
