package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppTenantConsentDeleteOptions {
  private String []consentIds;
  private String appId;
  private String tenantId;
}
