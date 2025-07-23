package com.descope.model.tenant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTenantLinkRequest {
  String tenantId;
  /** Expiration duration in seconds. */
  @JsonProperty("expireTime")
  long expireDuration;
  String ssoId;
  String email;
  String templateId;
}
