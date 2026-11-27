package com.descope.model.jwt;

import com.descope.model.auth.AssociatedTenant;
import com.descope.model.user.User;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MgmtSignUpUser {
  private User user;
  boolean verifiedEmail;
  boolean verifiedPhone;
  String ssoAppId;
  Map<String, Object> customClaims;
  // Session refresh duration in seconds
  Integer refreshDuration;
  String selectedTenantId;
  // Tenants to associate with the user; each needs a tenantId, tenantName is not sent
  List<AssociatedTenant> userTenants;
  // Keep existing tenant memberships that enforce SSO in the JWT
  Boolean ignoreEnforceSSO;

  public MgmtSignUpUser(User user, boolean verifiedEmail, boolean verifiedPhone, String ssoAppId,
      Map<String, Object> customClaims) {
    this(user, verifiedEmail, verifiedPhone, ssoAppId, customClaims, null, null, null, null);
  }
}
