package com.descope.model.notp;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  private Map<String, String> templateOptions;
  private String templateId;
  private Boolean revokeOtherSessions;
  private String[] revokeOtherSessionsTypes;
  private String locale;
  private String tenantId;
  private String ssoAppId;

  @JsonIgnore
  public boolean isJWTRequired() {
    return Boolean.TRUE.equals(stepup) || Boolean.TRUE.equals(mfa);
  }
}
