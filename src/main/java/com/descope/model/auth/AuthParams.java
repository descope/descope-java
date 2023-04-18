package com.descope.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthParams {
  private String projectId;
  private String publicKey;
  private Boolean sessionJwtViaCookie;
  private String cookieDomain;
}
