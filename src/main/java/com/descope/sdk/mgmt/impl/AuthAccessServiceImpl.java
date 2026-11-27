package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_RULE_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_RULE_ALL_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_RULE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_RULE_REORDER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_RULE_REORDER_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_RULE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_AUTH_ACCESS_SETTINGS_TENANT_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.authaccess.AuthAccessRule;
import com.descope.model.authaccess.AuthAccessRules;
import com.descope.model.authaccess.AuthAccessSettings;
import com.descope.model.client.Client;
import com.descope.sdk.mgmt.AuthAccessService;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

class AuthAccessServiceImpl extends ManagementsBase implements AuthAccessService {

  AuthAccessServiceImpl(Client client) {
    super(client);
  }

  @Override
  public AuthAccessRule loadRule(String id) throws DescopeException {
    validateRuleId(id);
    return getApiProxy().get(getQueryParamUri(MANAGEMENT_AUTH_ACCESS_RULE_LINK, mapOf("id", id)),
        AuthAccessRule.class);
  }

  @Override
  public AuthAccessRule loadTenantRule(String tenantId, String id) throws DescopeException {
    validateTenantId(tenantId);
    validateRuleId(id);
    return getApiProxy().get(
        getQueryParamUri(tenantPath(MANAGEMENT_AUTH_ACCESS_RULE_TENANT_LINK, tenantId), mapOf("id", id)),
        AuthAccessRule.class);
  }

  @Override
  public List<AuthAccessRule> loadAllRules() throws DescopeException {
    return rules(getApiProxy().get(getUri(MANAGEMENT_AUTH_ACCESS_RULE_ALL_LINK), AuthAccessRules.class));
  }

  @Override
  public List<AuthAccessRule> loadAllTenantRules(String tenantId) throws DescopeException {
    validateTenantId(tenantId);
    return rules(getApiProxy().get(tenantUri(MANAGEMENT_AUTH_ACCESS_RULE_ALL_TENANT_LINK, tenantId),
        AuthAccessRules.class));
  }

  @Override
  public AuthAccessRule createRule(AuthAccessRule rule) throws DescopeException {
    validateNewRule(rule);
    return getApiProxy().post(getUri(MANAGEMENT_AUTH_ACCESS_RULE_LINK), rule, AuthAccessRule.class);
  }

  @Override
  public AuthAccessRule createTenantRule(String tenantId, AuthAccessRule rule) throws DescopeException {
    validateTenantId(tenantId);
    validateNewRule(rule);
    return getApiProxy().post(tenantUri(MANAGEMENT_AUTH_ACCESS_RULE_TENANT_LINK, tenantId), rule,
        AuthAccessRule.class);
  }

  @Override
  public void deleteRule(String id) throws DescopeException {
    validateRuleId(id);
    getApiProxy().delete(getQueryParamUri(MANAGEMENT_AUTH_ACCESS_RULE_LINK, mapOf("id", id)), null, Void.class);
  }

  @Override
  public void deleteTenantRule(String tenantId, String id) throws DescopeException {
    validateTenantId(tenantId);
    validateRuleId(id);
    getApiProxy().delete(
        getQueryParamUri(tenantPath(MANAGEMENT_AUTH_ACCESS_RULE_TENANT_LINK, tenantId), mapOf("id", id)),
        null, Void.class);
  }

  @Override
  public List<AuthAccessRule> reorderRule(String id, int position) throws DescopeException {
    validateRuleId(id);
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    req.put("position", position);
    return rules(getApiProxy().post(getUri(MANAGEMENT_AUTH_ACCESS_RULE_REORDER_LINK), req, AuthAccessRules.class));
  }

  @Override
  public List<AuthAccessRule> reorderTenantRule(String tenantId, String id, int position)
      throws DescopeException {
    validateTenantId(tenantId);
    validateRuleId(id);
    Map<String, Object> req = new HashMap<>();
    req.put("tenantId", tenantId);
    req.put("id", id);
    req.put("position", position);
    return rules(getApiProxy().post(tenantUri(MANAGEMENT_AUTH_ACCESS_RULE_REORDER_TENANT_LINK, tenantId), req,
        AuthAccessRules.class));
  }

  @Override
  public AuthAccessSettings getSettings() throws DescopeException {
    return getApiProxy().get(getUri(MANAGEMENT_AUTH_ACCESS_SETTINGS_LINK), AuthAccessSettings.class);
  }

  @Override
  public AuthAccessSettings getTenantSettings(String tenantId) throws DescopeException {
    validateTenantId(tenantId);
    return getApiProxy().get(tenantUri(MANAGEMENT_AUTH_ACCESS_SETTINGS_TENANT_LINK, tenantId),
        AuthAccessSettings.class);
  }

  @Override
  public AuthAccessSettings configureSettings(AuthAccessSettings settings) throws DescopeException {
    validateSettings(settings);
    return getApiProxy().post(getUri(MANAGEMENT_AUTH_ACCESS_SETTINGS_LINK), settings, AuthAccessSettings.class);
  }

  @Override
  public AuthAccessSettings configureTenantSettings(String tenantId, AuthAccessSettings settings)
      throws DescopeException {
    validateTenantId(tenantId);
    validateSettings(settings);
    return getApiProxy().post(tenantUri(MANAGEMENT_AUTH_ACCESS_SETTINGS_TENANT_LINK, tenantId), settings,
        AuthAccessSettings.class);
  }

  private static void validateTenantId(String tenantId) {
    // Dot segments would resolve to the project-level route
    if (StringUtils.isBlank(tenantId) || ".".equals(tenantId) || "..".equals(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }
  }

  private static void validateRuleId(String id) {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
  }

  private static void validateNewRule(AuthAccessRule rule) {
    if (rule == null) {
      throw ServerCommonException.invalidArgument("rule");
    }
    if (StringUtils.isBlank(rule.getEffect())) {
      throw ServerCommonException.invalidArgument("rule.effect");
    }
  }

  private static void validateSettings(AuthAccessSettings settings) {
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
  }

  private static List<AuthAccessRule> rules(AuthAccessRules res) {
    if (res == null || res.getRules() == null) {
      return Collections.emptyList();
    }
    return res.getRules();
  }

  private URI tenantUri(String base, String tenantId) {
    return getUri(tenantPath(base, tenantId));
  }

  // Tenant IDs are path segments, so they must be encoded
  @SneakyThrows
  private static String tenantPath(String base, String tenantId) {
    return base + "/" + URLEncoder.encode(tenantId, "UTF-8").replace("+", "%20");
  }
}
