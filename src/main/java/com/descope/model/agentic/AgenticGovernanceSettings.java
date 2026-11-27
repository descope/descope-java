package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Agentic Governance Suite settings. On update, a null field keeps its stored value. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenticGovernanceSettings {
  /** Whether the Agentic Governance Suite has been activated for the project. */
  private Boolean configured;
  /** Approve new access requests at creation when the current policies already permit them. */
  private Boolean autoApproval;
  /** Switches the suite off: no new access requests can be created or auto-approved. */
  private Boolean suiteDisabled;
}
