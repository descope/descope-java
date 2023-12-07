package com.descope.model.webauthn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnFinishRequest {
  private String transactionId;
  private String response;
}
