package com.descope.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchLatestOutboundAppUserTokenRequest {
  private String appId;
  private String userId;
  // Optional tenant scope for the token, may be null/empty
  private String tenantId;
  private FetchOutboundAppTokenOptions options;
}
