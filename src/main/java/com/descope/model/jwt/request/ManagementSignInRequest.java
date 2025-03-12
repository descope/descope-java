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
  private Map<String, Object> customClaims;
  private String jwt;
}