package com.descope.model.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** An mTLS client certificate. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundAppMtlsCredential {
  private String id;
  /** Required. */
  private String name;
  private String subjectDn;
  private String pem;
  private String thumbprint;
  private Long createdAt;
}
