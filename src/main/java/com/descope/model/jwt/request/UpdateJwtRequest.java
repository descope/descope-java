package com.descope.model.jwt.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJwtRequest {
  String jwt;
  Map<String, Object> customClaims;
  Integer refreshDuration;

  public UpdateJwtRequest(String jwt, Map<String, Object> customClaims) {
    this(jwt, customClaims, null);
  }
}
