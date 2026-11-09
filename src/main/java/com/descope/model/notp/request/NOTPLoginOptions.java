package com.descope.model.notp.request;

import com.descope.model.magiclink.LoginOptions;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Spec's LoginOptions fields only (the shared model also serializes jwtrequired)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPLoginOptions {
  private boolean stepup;
  private boolean mfa;
  private Map<String, Object> customClaims;
  private Map<String, String> templateOptions;
  private boolean revokeOtherSessions;
  private String[] revokeOtherSessionsTypes;
  private String locale;

  public static NOTPLoginOptions from(LoginOptions loginOptions) {
    if (loginOptions == null) {
      return null;
    }
    return NOTPLoginOptions.builder()
        .stepup(loginOptions.isStepup())
        .mfa(loginOptions.isMfa())
        .customClaims(loginOptions.getCustomClaims())
        .templateOptions(loginOptions.getTemplateOptions())
        .revokeOtherSessions(loginOptions.isRevokeOtherSessions())
        .revokeOtherSessionsTypes(loginOptions.getRevokeOtherSessionsTypes())
        .locale(loginOptions.getLocale())
        .build();
  }
}
