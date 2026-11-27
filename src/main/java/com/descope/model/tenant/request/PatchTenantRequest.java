package com.descope.model.tenant.request;

import static com.descope.utils.CollectionUtils.addIfNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchTenantRequest {
  String name;
  List<String> selfProvisioningDomains;
  Map<String, Object> customAttributes;
  // Deprecated: set it with configureSettings
  String authType;
  Boolean disabled;
  Boolean enforceSSO;
  List<String> enforceSSOExclusions;
  List<String> federatedAppIds;
  // A TenantRoleInheritance value
  String roleInheritance;
  List<String> approvedDomains;
  List<String> enforceSSOExclusionPermissions;
  Boolean strict;
  List<String> permittedIps;

  public Map<String, Object> toMap() {
    Map<String, Object> m = new HashMap<>();
    addIfNotNull(m, "name", name);
    addIfNotNull(m, "selfProvisioningDomains", selfProvisioningDomains);
    addIfNotNull(m, "customAttributes", customAttributes);
    addIfNotNull(m, "authType", authType);
    addIfNotNull(m, "disabled", disabled);
    addIfNotNull(m, "enforceSSO", enforceSSO);
    addIfNotNull(m, "enforceSSOExclusions", enforceSSOExclusions);
    addIfNotNull(m, "federatedAppIds", federatedAppIds);
    addIfNotNull(m, "roleInheritance", roleInheritance);
    addIfNotNull(m, "approvedDomains", approvedDomains);
    addIfNotNull(m, "enforceSSOExclusionPermissions", enforceSSOExclusionPermissions);
    addIfNotNull(m, "strict", strict);
    addIfNotNull(m, "permittedIps", permittedIps);
    return m;
  }
}
