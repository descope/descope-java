package com.descope.model.jwt.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagementSignInRequest {
  private String loginId;
  private boolean stepup;
  private boolean mfa;
  private boolean revokeOtherSessions;
  private String[] revokeOtherSessionsTypes;
  private Map<String, Object> customClaims;
  private String jwt;
  private String ssoAppId;
  private Integer refreshDuration;
  private String selectedTenantId;
  private Boolean ignoreEnforceSSO;

  public ManagementSignInRequest(String loginId, boolean stepup, boolean mfa, boolean revokeOtherSessions,
      String[] revokeOtherSessionsTypes, Map<String, Object> customClaims, String jwt) {
    this(loginId, stepup, mfa, revokeOtherSessions, revokeOtherSessionsTypes, customClaims, jwt, null, null, null,
        null);
  }
}