package com.descope.model.sso;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JWTBearerSettings {
  /** Trusted issuers, keyed by issuer. */
  private Map<String, IssuerSettings> issuers;
  private String jwtBearerGrantTypeAudienceToUse;
  private String jwtBearerGrantTypeScopeToUse;
  private String jwtBearerGrantTypeCustomClaimsToUse;
}
