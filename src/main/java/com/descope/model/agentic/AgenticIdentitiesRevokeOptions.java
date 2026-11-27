package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Selects the agentic identities to revoke. At least one field must be set. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticIdentitiesRevokeOptions {
  private String userId;
  private String clientId;
  private String resourceId;
  private String consentId;
}
