package com.descope.model.auth;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessKeyLoginOptions {
  private Map<String, Object> customClaims;
  private String selectedTenant;

  public AccessKeyLoginOptions(Map<String, Object> customClaims) {
    this(customClaims, null);
  }
}
