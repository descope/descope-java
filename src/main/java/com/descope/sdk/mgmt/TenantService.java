package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.tenant.Tenant;
import com.descope.model.tenant.TenantSettings;
import com.descope.model.tenant.request.CreateTenantRequest;
import com.descope.model.tenant.request.GenerateTenantLinkRequest;
import com.descope.model.tenant.request.PatchTenantRequest;
import com.descope.model.tenant.request.TenantSearchRequest;
import com.descope.model.tenant.request.UpdateTenantRequest;
import com.descope.model.tenant.response.CreateTenantsBatchResponse;
import com.descope.model.tenant.response.DeleteTenantsBatchResponse;
import com.descope.model.tenant.response.UpdateTenantsBatchResponse;
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
   * Create a new tenant with all the settings in the request. The ID is generated when the request has none.
   *
   * @param request - The tenant to create. {@code name} is required and must be unique per project.
   * @return The tenant ID.
   * @throws DescopeException in case of errors
   */
  String create(CreateTenantRequest request) throws DescopeException;

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
   * Create tenants in batch, at most 100 per call by default. A tenant that fails is returned in
   * {@code failedTenants} while the others are created.
   *
   * @param tenants - The tenants to create, each with a {@code name}.
   * @param strict  - When true, setting custom attributes the project does not declare fails that tenant.
   *                Optional, null leaves it unset.
   * @return {@link CreateTenantsBatchResponse} with the created and the failed tenants
   * @throws DescopeException in case of errors
   */
  CreateTenantsBatchResponse createBatch(List<CreateTenantRequest> tenants, Boolean strict) throws DescopeException;

  /**
   * Update an existing tenant's name and domains. IMPORTANT: All parameters are
   * required and will override whatever value is currently set in the existing
   * tenant. Use carefully.
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
   * Update an existing tenant. IMPORTANT: All set parameters will override
   * whatever value is currently set in the existing tenant. Use carefully.
   *
   * @param id                      - Tenant ID (required)
   * @param name                    - The tenant name must be unique per project (required)
   * @param selfProvisioningDomains - Users authenticating from these domains will be associated with this tenant
   * @param customAttributes        - Custom attributes to apply to tenant (needs to be pre-configured)
   * @param authType                - The authentication type for the tenant
   * @param disabled                - Whether the tenant is disabled
   * @param enforceSSO              - Whether SSO is enforced for this tenant
   * @param enforceSSOExclusions    - List of user IDs excluded from SSO enforcement
   * @param federatedAppIds         - List of federated app IDs associated with the tenant
   * @param roleInheritance         - The role inheritance setting for the tenant
   * @throws DescopeException in case of errors
   */
  void update(String id, String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes,
      String authType, Boolean disabled, Boolean enforceSSO, List<String> enforceSSOExclusions,
      List<String> federatedAppIds, String roleInheritance) throws DescopeException;

  /**
   * Update an existing tenant. IMPORTANT: A field left unset is cleared, except {@code approvedDomains}
   * and an empty {@code authType}, which keep their current values. Use carefully.
   *
   * @param request - The tenant settings. {@code id} and {@code name} are required.
   * @throws DescopeException in case of errors
   */
  void update(UpdateTenantRequest request) throws DescopeException;

  /**
   * Update tenants in batch, at most 100 per call by default. Each tenant is replaced as in
   * {@link #update(UpdateTenantRequest)}. A tenant that fails is returned in {@code failedTenants}
   * while the others are updated.
   *
   * @param tenants - The tenants to update, each with an {@code id} and a {@code name}.
   * @param strict  - When true, setting custom attributes the project does not declare fails that tenant.
   *                Optional, null leaves it unset.
   * @return {@link UpdateTenantsBatchResponse} with the updated and the failed tenants
   * @throws DescopeException in case of errors
   */
  UpdateTenantsBatchResponse updateBatch(List<UpdateTenantRequest> tenants, Boolean strict) throws DescopeException;

  /**
   * Patch an existing tenant. Only the fields set in the request are changed.
   *
   * @param id      - Tenant ID
   * @param request - The fields to change
   * @throws DescopeException in case of errors
   */
  void patch(String id, PatchTenantRequest request) throws DescopeException;

  /**
   * Delete an existing tenant. IMPORTANT: This action is irreversible. Use
   * carefully.
   *
   * @param id - Tenant ID
   * @throws DescopeException in case of errors
   */
  void delete(String id) throws DescopeException;

  /**
   * Delete an existing tenant. IMPORTANT: This action is irreversible. Use
   * carefully.
   *
   * @param id      - Tenant ID
   * @param cascade - Whether to cascade the delete. Optional, null leaves it unset.
   * @throws DescopeException in case of errors
   */
  void delete(String id, Boolean cascade) throws DescopeException;

  /**
   * Delete tenants in batch, at most 100 per call by default. IMPORTANT: This action is irreversible.
   * A tenant that fails is returned in {@code failedTenants} while the others are deleted.
   *
   * @param ids     - The IDs of the tenants to delete
   * @param cascade - Whether to cascade the delete. Optional, null leaves it unset.
   * @return {@link DeleteTenantsBatchResponse} with the deleted and the failed tenants
   * @throws DescopeException in case of errors
   */
  DeleteTenantsBatchResponse deleteBatch(List<String> ids, Boolean cascade) throws DescopeException;

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

  /**
   * Update the default roles for the tenant.
   *
   * @param tenantId     Tenant ID
   * @param defaultRoles The list of default roles to set for the tenant
   * @throws DescopeException in case of errors
   */
  void updateDefaultRoles(String tenantId, List<String> defaultRoles) throws DescopeException;
}
