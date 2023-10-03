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
}
