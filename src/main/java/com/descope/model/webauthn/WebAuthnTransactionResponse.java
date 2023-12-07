package com.descope.model.webauthn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnTransactionResponse {
  private String transactionId;
  private String options;
  private boolean create;
}
