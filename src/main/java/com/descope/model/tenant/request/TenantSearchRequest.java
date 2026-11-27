package com.descope.model.tenant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class TenantSearchRequest {
  @JsonProperty("tenantIds")
  List<String> ids;
  @JsonProperty("tenantNames")
  List<String> names;
  Map<String, Object> customAttributes;
  @JsonProperty("tenantSelfProvisioningDomains")
  List<String> selfProvisioningDomains;
  String authType;
  @JsonProperty("tenantSSODomains")
  List<String> ssoDomains;
  String parentTenantId;
  Boolean topLevelOnly;

  // Keeps the pre-existing all-args constructor binary compatible
  public TenantSearchRequest(List<String> ids, List<String> names, Map<String, Object> customAttributes,
      List<String> selfProvisioningDomains, String authType) {
    this(ids, names, customAttributes, selfProvisioningDomains, authType, null, null, null);
  }
}
