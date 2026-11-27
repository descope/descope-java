package com.descope.model.agentic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The result of blocking or unblocking XAA agentic identities. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XaaAgenticIdentitiesResponse {
  /** The updated identities with their current blocked state. */
  private List<AgenticIdentity> identities;
  /** Requested consent IDs that were not updated. */
  private List<String> skippedConsentIds;
}
