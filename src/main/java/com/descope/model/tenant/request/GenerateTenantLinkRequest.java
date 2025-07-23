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
    @JsonProperty("tenantId")
    String tenantId;
    @JsonProperty("expireTime")
    long expireDuration;
    @JsonProperty("ssoId")
    String ssoId;
    @JsonProperty("email")
    String email;
    @JsonProperty("templateId")
    String templateId;
}
