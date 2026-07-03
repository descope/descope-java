package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.sso.AttributeMapping;
import com.descope.model.sso.RoleMapping;
import com.descope.model.sso.SSOAllSettingsResponse;
import com.descope.model.sso.SSOOIDCSettings;
import com.descope.model.sso.SSOSAMLSettings;
import com.descope.model.sso.SSOSAMLSettingsByMetadata;
import com.descope.model.sso.SSOSettingsResponse;
import com.descope.model.sso.SSOTenantSettingsResponse;
import java.util.List;

public interface SsoService {
  /**
   * Load SSO settings for the given tenant. When {@code ssoId} is blank the default SSO
   * configuration is returned (same behavior as {@link #loadSettings(String)}).
   *
   * @param tenantId the tenant ID to load settings for
   * @param ssoId optional SSO configuration ID; pass {@code null} or empty to get the default
   * @return {@link SSOTenantSettingsResponse} the matching SSO settings
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOTenantSettingsResponse loadSettings(String tenantId, String ssoId) throws DescopeException;

  /**
   * Load SSO settings for the given tenant (returns the default SSO configuration).
   *
   * @param tenantId the tenant ID we are loading settings for
   * @return {@link SSOTenantSettingsResponse} all SSO settings for the tenant
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOTenantSettingsResponse loadSettings(String tenantId) throws DescopeException;

  /**
   * Load all SSO configurations for the given tenant.
   *
   * @param tenantId the tenant ID to load all SSO configurations for
   * @return {@link SSOAllSettingsResponse} all SSO configurations for the tenant
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  SSOAllSettingsResponse loadAllSettings(String tenantId) throws DescopeException;

  /**
   * Configure SSO SAML settings for a tenant manually.
   *
   * @param tenantId required tenant ID
   * @param settings required settings with all fields set
   * @param redirectUrl optional. If absent, must be specified when starting an SSO authentication via the request
   * @param domains optional and is used to map users to this tenant when authenticating via SSO.
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void configureSAMLSettings(String tenantId, SSOSAMLSettings settings, String redirectUrl, List<String> domains)
      throws DescopeException;

  /**
   * Configure SSO SAML settings for a tenant by fetching them from an IDP metadata URL.
   *
   * @param tenantId required tenant ID
   * @param settings required settings with all fields set
   * @param redirectUrl optional. If absent, must be specified when starting an SSO authentication via the request
   * @param domains optional and is used to map users to this tenant when authenticating via SSO.
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void configureSAMLSettingsByMetadata(String tenantId, SSOSAMLSettingsByMetadata settings, String redirectUrl,
      List<String> domains) throws DescopeException;

  /**
   * Configure SSO OIDC settings for a tenant manually.
   *
   * @param tenantId required tenant ID
   * @param settings required settings
   * @param domains optional and is used to map users to this tenant when authenticating via SSO.
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void configureOIDCSettings(String tenantId, SSOOIDCSettings settings, List<String> domains) throws DescopeException;

  /**
   * Delete the SSO settings for the given tenant.
   *
   * @param tenantId required tenant ID
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void deleteSettings(String tenantId) throws DescopeException;

  /**
   * Configure the URLs to redirect to after a successful SSO authentication for the given tenant.
   *
   * @param tenantId required tenant ID
   * @param samlRedirectUrl optional URL to redirect to after a SAML SSO authentication
   * @param oauthRedirectUrl optional URL to redirect to after an OIDC/OAuth SSO authentication
   * @param ssoId optional SSO configuration ID
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void configureSSORedirectURL(String tenantId, String samlRedirectUrl, String oauthRedirectUrl, String ssoId)
      throws DescopeException;

  /**
   * Recalculate the SSO mappings (role and attribute mappings) for the given tenant.
   *
   * @param tenantId required tenant ID
   * @param ssoId optional SSO configuration ID
   * @throws DescopeException If error, a subtype of this exception will be thrown
   */
  void recalculateSSOMappings(String tenantId, String ssoId) throws DescopeException;

  @Deprecated
  SSOSettingsResponse getSettings(String tenantID) throws DescopeException;


  @Deprecated
  void configureSettings(String tenantID, String idpURL, String idpCert, String entityID,
      String redirectURL, List<String> domains) throws DescopeException;

  @Deprecated
  void configureMetadata(String tenantID, String idpMetadataURL) throws DescopeException;

  @Deprecated
  void configureMapping(String tenantID, List<RoleMapping> roleMapping,
      AttributeMapping attributeMapping) throws DescopeException;
}
