package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.authaccess.AuthAccessRule;
import com.descope.model.authaccess.AuthAccessSettings;
import java.util.List;

/** Provides functions for managing auth access policy rules and settings for both project and tenants. */
public interface AuthAccessService {
  /**
   * Load a project-level auth access rule.
   *
   * @param id The rule ID
   * @return {@link AuthAccessRule}
   * @throws DescopeException in case of errors
   */
  AuthAccessRule loadRule(String id) throws DescopeException;

  /**
   * Load a tenant-level auth access rule.
   *
   * @param tenantId Tenant ID
   * @param id The rule ID
   * @return {@link AuthAccessRule}
   * @throws DescopeException in case of errors
   */
  AuthAccessRule loadTenantRule(String tenantId, String id) throws DescopeException;

  /**
   * Load all project-level auth access rules in evaluation order.
   *
   * @return The list of {@link AuthAccessRule}
   * @throws DescopeException in case of errors
   */
  List<AuthAccessRule> loadAllRules() throws DescopeException;

  /**
   * Load all tenant-level auth access rules in evaluation order.
   *
   * @param tenantId Tenant ID
   * @return The list of {@link AuthAccessRule}
   * @throws DescopeException in case of errors
   */
  List<AuthAccessRule> loadAllTenantRules(String tenantId) throws DescopeException;

  /**
   * Create a project-level auth access rule, appended at the end of the evaluation order.
   *
   * @param rule The rule to create. Its effect is required and its ID must not be set.
   * @return The created {@link AuthAccessRule}
   * @throws DescopeException in case of errors
   */
  AuthAccessRule createRule(AuthAccessRule rule) throws DescopeException;

  /**
   * Create a tenant-level auth access rule, appended at the end of the tenant evaluation order.
   *
   * @param tenantId Tenant ID
   * @param rule The rule to create. Its effect is required and its ID must not be set.
   * @return The created {@link AuthAccessRule}
   * @throws DescopeException in case of errors
   */
  AuthAccessRule createTenantRule(String tenantId, AuthAccessRule rule) throws DescopeException;

  /**
   * Delete a project-level auth access rule.
   *
   * @param id The rule ID
   * @throws DescopeException in case of errors
   */
  void deleteRule(String id) throws DescopeException;

  /**
   * Delete a tenant-level auth access rule.
   *
   * @param tenantId Tenant ID
   * @param id The rule ID
   * @throws DescopeException in case of errors
   */
  void deleteTenantRule(String tenantId, String id) throws DescopeException;

  /**
   * Move a project-level auth access rule to a position in the evaluation order. Rules in between shift
   * accordingly.
   *
   * @param id The rule ID
   * @param position The 1-based position
   * @return All rules in their new order
   * @throws DescopeException in case of errors
   */
  List<AuthAccessRule> reorderRule(String id, int position) throws DescopeException;

  /**
   * Move a tenant-level auth access rule to a position in the tenant evaluation order. Rules in between
   * shift accordingly.
   *
   * @param tenantId Tenant ID
   * @param id The rule ID
   * @param position The 1-based position
   * @return All rules in their new order
   * @throws DescopeException in case of errors
   */
  List<AuthAccessRule> reorderTenantRule(String tenantId, String id, int position) throws DescopeException;

  /**
   * Get the project-level auth access settings.
   *
   * @return {@link AuthAccessSettings}
   * @throws DescopeException in case of errors
   */
  AuthAccessSettings getSettings() throws DescopeException;

  /**
   * Get the tenant-level auth access settings.
   *
   * @param tenantId Tenant ID
   * @return {@link AuthAccessSettings}
   * @throws DescopeException in case of errors
   */
  AuthAccessSettings getTenantSettings(String tenantId) throws DescopeException;

  /**
   * Configure the project-level auth access settings. Fields left null keep their stored values.
   *
   * @param settings The settings to set
   * @return The updated {@link AuthAccessSettings}
   * @throws DescopeException in case of errors
   */
  AuthAccessSettings configureSettings(AuthAccessSettings settings) throws DescopeException;

  /**
   * Configure the tenant-level auth access settings. Fields left null keep their stored values.
   *
   * @param tenantId Tenant ID
   * @param settings The settings to set
   * @return The updated {@link AuthAccessSettings}
   * @throws DescopeException in case of errors
   */
  AuthAccessSettings configureTenantSettings(String tenantId, AuthAccessSettings settings)
      throws DescopeException;
}
