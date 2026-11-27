package com.descope.model.sso;

import com.descope.model.tenant.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOTenantSettingsResponse {
  private Tenant tenant;
  private SSOSAMLSettingsResponse saml;
  private SSOOIDCSettings oidc;
  private String ssoId;
  /** Display name of the SSO configuration. Empty for the tenant's default SSO configuration. */
  private String name;
  /** Verify identity only: logins through this SSO configuration don't create, update or sign in a user. */
  private Boolean authenticationOnly;
  /** JIT provisioning off: logins through this SSO configuration sign in only users who already exist. */
  private Boolean jitDisabled;

  public SSOTenantSettingsResponse(Tenant tenant, SSOSAMLSettingsResponse saml, SSOOIDCSettings oidc) {
    this(tenant, saml, oidc, null, null, null, null);
  }
}
