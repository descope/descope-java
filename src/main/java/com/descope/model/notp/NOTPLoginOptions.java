package com.descope.model.notp;

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
  private boolean stepup;
  private boolean mfa;
  private Map<String, Object> customClaims;
  private String ssoAppId;
  private Map<String, String> templateOptions;
  private String locale;
  private String templateId;
  private boolean revokeOtherSessions;
  private List<String> revokeOtherSessionsTypes;
  private String tenantId;
}
