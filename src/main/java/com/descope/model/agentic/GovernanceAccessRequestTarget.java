package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// A resource or connection an access request asks the agent client to reach.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAccessRequestTarget {
  // The name of the target, as listed in the project's resources or connections
  private String name;
  // "resource" or "connection"
  private String kind;
  // Read-only: the id of the resource the name matched at creation. Ignored on create.
  private String targetId;
  // Scopes requested on this target. Leave empty when allScopes is set.
  private List<String> scopes;
  // Request any scope the target offers instead of an explicit list
  private boolean allScopes;
}
