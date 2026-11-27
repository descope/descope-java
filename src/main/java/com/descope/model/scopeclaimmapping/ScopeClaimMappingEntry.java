package com.descope.model.scopeclaimmapping;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Binds a single scope to the JWT claims it produces. Each claim value may be a static string or a
 * {{...}} template resolved at token-generation time.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScopeClaimMappingEntry {
  private String scope;
  private Map<String, String> claims;
  /** Free-form note shown in the console, never used at token-generation time. */
  private String description;
  /** Narrows which tokens a claim is written to, keyed by the claim name in {@code claims}. */
  private Map<String, ClaimTokenTargets> claimTargets;
  /** When true, adds this scope to every OIDC SSO and inbound application created from now on. */
  private Boolean addToNewApplications;
}
