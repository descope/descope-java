package com.descope.model.jwt.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAssertionRequest {
  String issuer;
  String subject;
  List<String> audience;
  Integer expiresIn;
  Boolean flattenAudience;
  String algorithm;
}
