package com.descope.model.auth;

import com.descope.model.jwt.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeAccessKeyResponse {
  private Token token; // session token
  private String keyId;
}
