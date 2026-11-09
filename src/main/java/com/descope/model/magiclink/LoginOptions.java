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
  private String[] revokeOtherSessionsTypes;
  private String jwt;
  private String locale;
  private String templateId;
  private String ssoAppId;
  /** Base64-encoded PKCE challenge; used only by enchanted links. */
  private String pkceChallenge;
  private String tenantId;

  public LoginOptions(boolean stepup, boolean mfa, Map<String, Object> customClaims,
                      Map<String, String> templateOptions) {
    this.stepup = stepup;
    this.mfa = mfa;
    this.customClaims = customClaims;
    this.templateOptions = templateOptions;
  }

  public LoginOptions(boolean stepup, boolean mfa, Map<String, Object> customClaims,
                      Map<String, String> templateOptions, boolean revokeOtherSessions,
                      String[] revokeOtherSessionsTypes, String jwt) {
    this.stepup = stepup;
    this.mfa = mfa;
    this.customClaims = customClaims;
    this.templateOptions = templateOptions;
    this.revokeOtherSessions = revokeOtherSessions;
    this.revokeOtherSessionsTypes = revokeOtherSessionsTypes;
    this.jwt = jwt;
  }

  public LoginOptions(boolean stepup, boolean mfa, Map<String, Object> customClaims,
                      Map<String, String> templateOptions, boolean revokeOtherSessions,
                      String[] revokeOtherSessionsTypes, String jwt, String locale, String templateId) {
    this(stepup, mfa, customClaims, templateOptions, revokeOtherSessions, revokeOtherSessionsTypes, jwt, locale,
        templateId, null, null, null);
  }

  public boolean isJWTRequired() {
    return this.isStepup() || this.isMfa();
  }
}
