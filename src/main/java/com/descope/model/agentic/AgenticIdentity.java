package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// An agentic identity: a consent granted to an agentic app.
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
