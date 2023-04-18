package com.descope.model.jwt;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SigningKey {
  private String alg;
  private String e;
  private String kid;
  private String kty;
  private String n;
  private String use;
}
