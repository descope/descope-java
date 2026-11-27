package com.descope.model.tenant;

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
public class Tenant {
  String id;
  String name;
  List<String> selfProvisioningDomains;
  Map<String, Object> customAttributes;
  String authType;
  List<String> domains;
  Boolean disabled;
  Boolean enforceSSO;
  List<String> enforceSSOExclusions;
  List<String> federatedAppIds;
  String roleInheritance;
  Integer createdTime;
  String parent;
  List<String> successors;
  List<String> defaultRoles;
  List<AdditionalSSOConfiguration> additionalSSOConfigs;
  List<String> approvedDomains;
  List<String> enforceSSOExclusionPermissions;
  List<String> permittedIps;

  // Keeps the pre-existing all-args constructor binary compatible
  public Tenant(String id, String name, List<String> selfProvisioningDomains, Map<String, Object> customAttributes,
      String authType, List<String> domains, Boolean disabled, Boolean enforceSSO, List<String> enforceSSOExclusions,
      List<String> federatedAppIds, String roleInheritance) {
    this(id, name, selfProvisioningDomains, customAttributes, authType, domains, disabled, enforceSSO,
        enforceSSOExclusions, federatedAppIds, roleInheritance, null, null, null, null, null, null, null, null);
  }
}
