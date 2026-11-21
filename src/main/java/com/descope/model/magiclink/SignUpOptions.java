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
  // Only enchanted links use it; other methods ignore it
  private byte[] pkceChallenge;
  private String templateId;
  private String tenantId;

  // Keeps the previous all-args constructor for positional callers
  public SignUpOptions(Map<String, Object> customClaims, Map<String, String> templateOptions) {
    this.customClaims = customClaims;
    this.templateOptions = templateOptions;
  }
}
