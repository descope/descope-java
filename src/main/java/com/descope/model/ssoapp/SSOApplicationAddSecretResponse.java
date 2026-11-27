package com.descope.model.ssoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSOApplicationAddSecretResponse {
  private SSOApplicationSecretMeta meta;
  /** The secret in cleartext, returned only on add or reveal. */
  private String cleartext;
}
