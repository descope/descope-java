package com.descope.model.inbound;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Settings for the JWT bearer grant. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppJwtBearerSettings {
  /** Trusted issuers, keyed by the issuer. */
  private Map<String, InboundAppIssuerSettings> issuers;
  private String jwtBearerGrantTypeAudienceToUse;
  private String jwtBearerGrantTypeScopeToUse;
  private String jwtBearerGrantTypeCustomClaimsToUse;
}
