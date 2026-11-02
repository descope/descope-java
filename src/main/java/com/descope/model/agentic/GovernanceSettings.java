package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceSettings {
  // Fields left null are not changed when updating the settings
  private Boolean configured;
  private Boolean autoApproval;
  private Boolean suiteDisabled;
}
