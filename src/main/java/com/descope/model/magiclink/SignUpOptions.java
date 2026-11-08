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
  /** Max length 254. */
  private String locale;
  /** Relevant only for enchanted links; other methods ignore it. */
  private String pkceChallenge;
  private String tenantId;

  public SignUpOptions(Map<String, Object> customClaims, Map<String, String> templateOptions) {
    this.customClaims = customClaims;
    this.templateOptions = templateOptions;
  }
}
