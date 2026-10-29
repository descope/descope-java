package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentity {
  private AgenticIdentityConsent consent;
  private String agentName;
  private String resourceId;
  private String clientId;
  private String createdBy;
}
