package com.descope.model.agentic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestResolveRequest {
  private String id; // required
  // "approve" or "deny"
  private String action; // required
  // Optional note recorded with the resolution
  private String resolutionNote;
}
