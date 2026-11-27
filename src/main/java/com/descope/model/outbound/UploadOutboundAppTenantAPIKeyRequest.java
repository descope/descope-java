package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadOutboundAppTenantAPIKeyRequest {
  private String appId;
  private String tenantId;
  private String apiKey;
  private String externalIdentifier;
}
