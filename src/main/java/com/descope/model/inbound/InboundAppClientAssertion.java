package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A private_key_jwt trust entry: a JWKS URL or a PEM public key. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppClientAssertion {
  public static final String KEY_SOURCE_JWKS = "jwks";
  public static final String KEY_SOURCE_PUBLIC_KEY = "publicKey";

  /** Required. One of the {@code KEY_SOURCE_*} values. */
  private String keySource;
  private String jwksUri;
  private String publicKey;
  private String signAlgorithm;
}
