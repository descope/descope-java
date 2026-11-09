package com.descope.model.jwt.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccessKeyJWTResponse extends JWTResponse {
  private String keyId;
}
