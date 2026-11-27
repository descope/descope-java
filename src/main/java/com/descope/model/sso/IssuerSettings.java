package com.descope.model.sso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuerSettings {
  public static final String SIGN_ALGORITHM_RS256 = "RS256";
  public static final String SIGN_ALGORITHM_ES256 = "ES256";
  public static final String SIGN_ALGORITHM_ES384 = "ES384";
  public static final String SIGN_ALGORITHM_ES512 = "ES512";

  /** JWKS URI of the issuer. Must not resolve to an internal or private address. */
  private String jwksUri;
  /** One of the {@code SIGN_ALGORITHM_*} values. */
  private String signAlgorithm;
  /** User info URI of the issuer. Must not resolve to an internal or private address. */
  private String userInfoUri;
  private String externalIdFieldName;
  /** Cross-App Access JIT provisioning: when true, only an already-provisioned user signs in. */
  private Boolean jitDisabled;
  /** Maps assertion claims to user fields. */
  private AttributeMapping attributeMapping;
}
