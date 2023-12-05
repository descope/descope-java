package com.descope.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("checkstyle:MemberName")
public class SigningKey {
  private String alg;
  private String e;
  private String kid;
  private String kty;
  private String n;
  private String use;
}
