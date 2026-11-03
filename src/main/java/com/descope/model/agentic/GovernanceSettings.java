package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Agentic Governance Suite settings. A field left null keeps its stored value on update.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceSettings {
  // Whether the Agentic Governance Suite has been activated for the project
  private Boolean configured;
  // When true, a new access request that the current policies already permit is approved at creation
  private Boolean autoApproval;
  // When true, no new access requests can be created and none are auto-approved
  private Boolean suiteDisabled;
}
