package com.descope.model.auth;

import com.descope.model.jwt.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeAccessKeyResponse {
  private String keyId;
  private Token sessionToken;
}
