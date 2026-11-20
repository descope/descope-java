package com.descope.model.magiclink.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest {
  private String token;
  private byte[] pkceVerifier;

  // Keeps the pre-pkceVerifier all-args constructor available to callers that use it positionally.
  public VerifyRequest(String token) {
    this(token, null);
  }
}
