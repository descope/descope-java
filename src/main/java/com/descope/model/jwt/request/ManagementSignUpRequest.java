package com.descope.model.jwt.request;

import com.descope.model.user.User;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagementSignUpRequest {
  private String loginId;
  private User user;
  private boolean emailVerified;
  private boolean phoneVerified;
  private String ssoAppId;
  private Map<String, Object> customClaims;
  private Integer refreshDuration;
  private String selectedTenantId;
  private List<Map<String, Object>> userTenants;
  private Boolean ignoreEnforceSSO;

  public ManagementSignUpRequest(String loginId, User user, boolean emailVerified, boolean phoneVerified,
      String ssoAppId, Map<String, Object> customClaims) {
    this(loginId, user, emailVerified, phoneVerified, ssoAppId, customClaims, null, null, null, null);
  }
}