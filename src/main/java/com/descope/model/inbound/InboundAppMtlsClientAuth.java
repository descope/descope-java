package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The credentials of one mTLS client authentication method. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppMtlsClientAuth {
  private InboundAppMtlsCredential[] credentials;
}
