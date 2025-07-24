package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import java.util.List;
import java.util.Map;

/** Provides functions for managing tenants in a project. */
public interface TenantService {

  /**
   * Create a new tenant with the given name. selfProvisioningDomains is an
   * optional list of domains
   * that are associated with this tenant.
   *
   * @param name                    - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will
   *                                be associated
   *                                with this tenant.
   * @return The tenant ID generated automatically for the tenant.
   * @throws DescopeException in case of errors
   */
  String create(String name, List<String> selfProvisioningDomains) throws DescopeException;

  /**
   * Create a new tenant with the given name. selfProvisioningDomains is an
   * optional list of domains that are associated with this tenant.
   * 
   *
   * @param name                    - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will
   *                                be associated with this tenant.
   * @param customAttributes        - Custom attributes to apply to tenant (needs
   *                                to be pre-configured)
   * @return The tenant ID generated automatically for the tenant.
   * @throws DescopeException in case of errors
   */
  String create(String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes)
      throws DescopeException;

  /**
   * Create a new tenant with the given name and ID. selfProvisioningDomains is an
   * optional list of
   * domains that are associated with this tenant.
   *
   * @param id                      - The tenant ID must be unique per project.
   * @param name                    - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will
   *                                be associated
   *                                with this tenant.
   * @throws DescopeException in case of errors
   */
  void createWithId(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException;

  /**
   * Create a new tenant with the given name and ID. selfProvisioningDomains is an
   * optional list of
   * domains that are associated with this tenant.
   *
   * @param id                      - The tenant ID must be unique per project.
   * @param name                    - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will
   *                                be associated with this tenant.
   * @param customAttributes        - Custom attributes to apply to tenant (needs
   *                                to be pre-configured)
   * @throws DescopeException in case of errors
   */
  void createWithId(String id, String name, List<String> selfProvisioningDomains,
      Map<String, Object> customAttributes)
      throws DescopeException;

  /**
   * Update an existing tenant's name and domains. IMPORTANT: All parameters are
   * required and will
   * override whatever value is currently set in the existing tenant. Use
   * carefully.
   *
   * @param id                      - Tenant ID
   * @param name                    - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will
   *                                be associated with this tenant.
   * @param customAttributes        - Custom attributes to apply to tenant (needs
   *                                to be pre-configured)
   * @throws DescopeException in case of errors
   */
  void update(String id, String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes)
      throws DescopeException;

  /**
   * Delete an existing tenant. IMPORTANT: This action is irreversible. Use
   * carefully.
   *
   * @param id - Tenant ID
   * @throws DescopeException in case of errors
   */
  void delete(String id) throws DescopeException;

  /**
   * Load the tenant with the given ID.
   *
   * @param id - Tenant ID
   * @return {@link Tenant}
   * @throws DescopeException in case of errors
   */
  Tenant load(String id) throws DescopeException;

  /**
   * Load all project tenants.s
   *
   * @return {{@link List} of {@link Tenant}
   * @throws DescopeException in case of errors
   */
  List<Tenant> loadAll() throws DescopeException;

  /**
   * Search all tenants according to given filters.
   *
   * @param request The options optional parameter allows to fine-tune the search
   *                filters and
   *                results. Using nil will result in a filter-less query with a
   *                set amount of results.
   * @return {{@link List} of {@link Tenant}
   * @throws DescopeException If there occurs any exception, a subtype of this
   *                          exception will be
   *                          thrown.
   */
  List<Tenant> searchAll(TenantSearchRequest request) throws DescopeException;

  /**
   * Get the tenant settings.
   *
   * @param id Tenant ID
   * @return {@link TenantSettings}
   * @throws DescopeException in case of errors
   */
  TenantSettings getSettings(String id) throws DescopeException;

  /**
   * Configure the tenant settings.
   *
   * @param id Tenant ID
   * @param settings The settings to set for the tenant
   * @throws DescopeException in case of errors
   */
  void configureSettings(String id, TenantSettings settings) throws DescopeException;

  /**
   * Generate a link that can be used to configure SSO for the tenant.
   *
   * @param request The request object containing all necessary parameters
   * @return A link that can be used to configure SSO for the tenant
   * @throws DescopeException in case of errors
   */
  String generateSSOConfigurationLink(GenerateTenantLinkRequest request) throws DescopeException;

  /**
   * Revoke an existing SSO configuration link for the tenant.
   *
   * @param tenantId Tenant ID
   * @param ssoID The SSO ID for which the link should be revoked
   * @throws DescopeException in case of errors
   */
  void revokeSSOConfigurationLink(String tenantId, String ssoID) throws DescopeException;
}
