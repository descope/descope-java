package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppConsentSearchOptions {
  private String appId;
  private String userId;
  private String consentId;
  private String page;
  private String tenantId;
}
