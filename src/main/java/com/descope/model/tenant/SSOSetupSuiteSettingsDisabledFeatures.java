package com.descope.model.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOSetupSuiteSettingsDisabledFeatures {
  Boolean saml;
  Boolean oidc;
  Boolean scim;
  Boolean ssoDomains;
  Boolean groupMapping;
  Boolean roleMapping;
  Boolean fgaMapping;
  Boolean sso;
  Boolean audit;
}
