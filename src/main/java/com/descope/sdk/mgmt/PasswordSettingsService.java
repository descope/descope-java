package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.passwordsettings.PasswordSettings;

/** Provides functions for managing password policies for both project and tenants. */
public interface PasswordSettingsService {
  /**
   * Get the project password settings.
   *
   * @return {@link PasswordSettings}
   * @throws DescopeException in case of errors
   */
  PasswordSettings getSettings() throws DescopeException;

  /**
   * Get the tenant password settings.
   *
   * @param tenantId Tenant ID
   * @return {@link PasswordSettings}
   * @throws DescopeException in case of errors
   */
  PasswordSettings getSettings(String tenantId) throws DescopeException;

  /**
   * Configure the project settings.
   *
   * @param settings The settings to set for the tenant
   * @throws DescopeException in case of errors
   */
  void configureSettings(PasswordSettings settings) throws DescopeException;

  /**
   * Configure the tenant settings.
   *
   * @param tenantId Tenant ID
   * @param settings The settings to set for the tenant
   * @throws DescopeException in case of errors
   */
  void configureSettings(String tenantId, PasswordSettings settings) throws DescopeException;
}
