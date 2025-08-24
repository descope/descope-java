package com.descope.model.outbound;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchOutboundAppTenantTokenRequest {
  private String appId;
  private String tenantId;
  private List<String> scopes; // Required for by-scopes endpoint; optional for latest endpoint
  private FetchOutboundAppTokenOptions options;
}
