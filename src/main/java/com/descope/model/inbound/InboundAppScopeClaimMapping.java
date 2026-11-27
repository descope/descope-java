package com.descope.model.inbound;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps a scope to the claims it grants. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppScopeClaimMapping {
  private String scope;
  /** Claim name to value template, used only when {@code useProjectMapping} is not true. */
  private Map<String, String> claims;
  private String description;
  /** Reuse the project-wide mapping's entry for this scope. */
  private Boolean useProjectMapping;
  /** The scope is always granted and the user can't deselect it. */
  private Boolean mandatory;
  /** The tokens each claim is written to, keyed by the claim name. */
  private Map<String, InboundAppClaimTokenTargets> claimTargets;
}
