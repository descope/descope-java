package com.descope.model.magiclink;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpOptions {
  private Map<String, Object> customClaims;
  private Map<String, String> templateOptions;
  /** Message locale, at most 254 characters. */
  private String locale;
  private String tenantId;

  public SignUpOptions(Map<String, Object> customClaims, Map<String, String> templateOptions) {
    this(customClaims, templateOptions, null, null);
  }
}
