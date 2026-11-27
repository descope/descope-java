package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadOutboundAppUserAPIKeyRequest {
  private String appId;
  private String userId;
  private String apiKey;
  private String tenantId;
  private String externalIdentifier;
}
