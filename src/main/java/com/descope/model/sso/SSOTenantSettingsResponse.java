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
}
