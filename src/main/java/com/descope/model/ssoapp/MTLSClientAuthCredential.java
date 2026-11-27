package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MTLSClientAuthCredential {
  private String id;
  /** Required. */
  private String name;
  private String subjectDn;
  private String pem;
  private String thumbprint;
  private Integer createdAt;
}
