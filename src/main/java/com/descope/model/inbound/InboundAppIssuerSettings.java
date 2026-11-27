package com.descope.model.inbound;

import com.descope.model.sso.AttributeMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A trusted issuer for the JWT bearer grant. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppIssuerSettings {
  public static final String SIGN_ALGORITHM_RS256 = "RS256";
  public static final String SIGN_ALGORITHM_ES256 = "ES256";
  public static final String SIGN_ALGORITHM_ES384 = "ES384";
  public static final String SIGN_ALGORITHM_ES512 = "ES512";

  /** Must not resolve to an internal or private address. */
  private String jwksUri;
  /** One of the {@code SIGN_ALGORITHM_*} values. */
  private String signAlgorithm;
  /** Must not resolve to an internal or private address. */
  private String userInfoUri;
  private String externalIdFieldName;
  /** Only sign in an already provisioned user, without Cross-App Access JIT provisioning. */
  private Boolean jitDisabled;
  private AttributeMapping attributeMapping;
}
