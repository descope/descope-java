package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentity {
  // Null when the identity has no consent
  private AgenticIdentityConsent consent;
  private String agentName;
  private String resourceId;
  private String clientId;
  private String createdBy;
}
