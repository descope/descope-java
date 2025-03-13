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
public class LoginOptions {
  private boolean stepup;
  private boolean mfa;
  private Map<String, Object> customClaims;
  private Map<String, String> templateOptions;
  private boolean revokeOtherSessions;
  private String jwt;

  public LoginOptions(boolean stepup, boolean mfa, Map<String, Object> customClaims,
                      Map<String, String> templateOptions) {
    this.stepup = stepup;
    this.mfa = mfa;
    this.customClaims = customClaims;
    this.templateOptions = templateOptions;
  }

  public boolean isJWTRequired() {
    return this.isStepup() || this.isMfa();
  }
}
