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
  private String locale;
  /** Base64 PKCE challenge; only enchanted links use it, other methods ignore it. */
  private String pkceChallenge;
  private String templateId;
  private String tenantId;

  public SignUpOptions(Map<String, Object> customClaims, Map<String, String> templateOptions) {
    this(customClaims, templateOptions, null, null, null, null);
  }
}
