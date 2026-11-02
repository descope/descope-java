package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestTarget {
  // The name of the target, as listed in the project's resources or connections
  private String name;
  // "resource" or "connection"
  private String kind;
  // Read-only, set by the server
  private String targetId;
  private List<String> scopes;
  private Boolean allScopes;
}
