package com.descope.model.tenant;

import com.descope.enums.TenantAuthType;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSettings {
  List<String> domains;
  List<String> selfProvisioningDomains;
  @JsonAlias({"enabled"})
  Boolean sessionSettingsEnabled;
  Integer refreshTokenExpiration;
  String refreshTokenExpirationUnit;
  Integer sessionTokenExpiration;
  String sessionTokenExpirationUnit;
  Boolean enableInactivity;
  Integer inactivityTime;
  String inactivityTimeUnit;
  @JsonAlias({"JITDisabled"})
  Boolean jitDisabled;
  TenantAuthType authType; // authType can be either "oidc" or "saml"
  Integer stepupTokenExpiration;
  String stepupTokenExpirationUnit;
  SSOSetupSuiteSettings ssoSetupSuiteSettings;

  // Keeps the pre-existing all-args constructor binary compatible
  public TenantSettings(List<String> domains, List<String> selfProvisioningDomains, Boolean sessionSettingsEnabled,
      Integer refreshTokenExpiration, String refreshTokenExpirationUnit, Integer sessionTokenExpiration,
      String sessionTokenExpirationUnit, Boolean enableInactivity, Integer inactivityTime, String inactivityTimeUnit,
      Boolean jitDisabled, TenantAuthType authType) {
    this(domains, selfProvisioningDomains, sessionSettingsEnabled, refreshTokenExpiration, refreshTokenExpirationUnit,
        sessionTokenExpiration, sessionTokenExpirationUnit, enableInactivity, inactivityTime, inactivityTimeUnit,
        jitDisabled, authType, null, null, null);
  }
}
