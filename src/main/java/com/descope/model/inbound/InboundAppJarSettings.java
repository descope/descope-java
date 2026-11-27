package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JAR (JWT-Secured Authorization Request, RFC 9101) settings. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppJarSettings {
  private Boolean enforce;
  private String publicKey;
  private Integer maxLifetimeSeconds;
  private Boolean fapi;
}
