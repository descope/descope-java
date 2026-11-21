package com.descope.model.notp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NOTPLoginOptions {
  private Boolean stepup;
  private Boolean mfa;
  private Map<String, Object> customClaims;
  private String ssoAppId;
  private Map<String, String> templateOptions;
  private String locale;
  private String pkceChallenge;
  private String templateId;
  private Boolean revokeOtherSessions;
  private List<String> revokeOtherSessionsTypes;
  private String tenantId;

  @JsonIgnore
  public boolean isJWTRequired() {
    return Boolean.TRUE.equals(stepup) || Boolean.TRUE.equals(mfa);
  }
}
