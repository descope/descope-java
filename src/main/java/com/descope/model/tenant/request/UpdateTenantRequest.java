package com.descope.model.tenant.request;

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
public class UpdateTenantRequest {
  String id;
  String name;
  List<String> selfProvisioningDomains;
  Map<String, Object> customAttributes;
  String authType;
  Boolean disabled;
  Boolean enforceSSO;
  List<String> enforceSSOExclusions;
  List<String> federatedAppIds;
  String roleInheritance;
}
