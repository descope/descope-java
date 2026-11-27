package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A workload identity trust entry: a SPIFFE trust domain or an OIDC issuer. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppWorkloadClientAssertion {
  public static final String FEDERATION_TYPE_SPIFFE = "spiffe";
  public static final String FEDERATION_TYPE_OIDC = "oidc";

  /** Required. One of the {@code FEDERATION_TYPE_*} values. */
  private String federationType;
  private String trustDomain;
  private String issuer;
  private String jwksUri;
  /** Required. */
  private String[] allowedSubjects;
  private String signAlgorithm;
}
