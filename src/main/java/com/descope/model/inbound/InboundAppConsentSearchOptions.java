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
  private Integer limit;

  public InboundAppConsentSearchOptions(String appId, String userId, String consentId, String page,
      String tenantId) {
    this.appId = appId;
    this.userId = userId;
    this.consentId = consentId;
    this.page = page;
    this.tenantId = tenantId;
  }
}
