package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.tenant.Tenant;
import java.util.List;

/** Provides functions for managing tenants in a project. */
public interface TenantService {

  /**
   * Create a new tenant with the given name. selfProvisioningDomains is an optional list of domains
   * that are associated with this tenant.
   *
   * @param name - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will be associated
   *     with this tenant.
   * @return The tenant ID generated automatically for the tenant.
   * @throws DescopeException
   */
  String create(String name, List<String> selfProvisioningDomains) throws DescopeException;

  /**
   * Create a new tenant with the given name and ID. selfProvisioningDomains is an optional list of
   * domains that are associated with this tenant.
   *
   * @param id - The tenant ID must be unique per project.
   * @param name - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will be associated
   *     with this tenant.
   * @throws DescopeException
   */
  void createWithId(String id, String name, List<String> selfProvisioningDomains)
      throws DescopeException;

  /**
   * Update an existing tenant's name and domains. IMPORTANT: All parameters are required and will
   * override whatever value is currently set in the existing tenant. Use carefully.
   *
   * @param id - Tenant ID
   * @param name - The tenant name must be unique per project.
   * @param selfProvisioningDomains - Users authenticating from these domains will be associated
   *     with this tenant.
   * @throws DescopeException
   */
  void update(String id, String name, List<String> selfProvisioningDomains) throws DescopeException;

  /**
   * Delete an existing tenant. IMPORTANT: This action is irreversible. Use carefully.
   *
   * @param id - Tenant ID
   * @throws DescopeException
   */
  void delete(String id) throws DescopeException;

  /**
   * Load all project tenants.s
   *
   * @return {@link Tenant Tenant}
   * @throws DescopeException
   */
  List<Tenant> loadAll() throws DescopeException;
}
