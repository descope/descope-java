package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppConsentDeleteOptions {
  String[] consentIds;
  String appId;
  String[] userIds;
  String tenantId;
}
