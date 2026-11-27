package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The mTLS client authentication methods (RFC 8705). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppMtlsClientAuthMethods {
  private InboundAppMtlsClientAuth tlsClientAuth;
  private InboundAppMtlsClientAuth selfSignedTlsClientAuth;
}
