package com.descope.model.user.response;

import com.descope.model.auth.AssociatedTenant;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("checkstyle:MemberName")
public class UserResponse {
  String userId;
  List<String> loginIds;
  String email;
  Boolean verifiedEmail;
  String phone;
  Boolean verifiedPhone;
  String name;
  String givenName;
  String middleName;
  String familyName;
  List<String> roleNames;
  List<AssociatedTenant> userTenants;
  String status;
  String picture;
  Boolean test;
  Long createdTime;
  Map<String, Object> customAttributes;
  Boolean TOTP;
  Boolean SAML;
  Map<String, Boolean> oAuth;
  List<String> ssoAppIds;
  /** Auth method that triggered brute-force protection, empty when none. */
  String lockReason;
  /**
   * When a temporary lock ends, in unix seconds (0 when none). The user is temporarily locked while
   * this is greater than now.
   */
  Long tempLockExpiration;
  List<String> externalIds;
  Boolean webauthn;
  Boolean password;
  Boolean editable;
  Boolean SCIM;
  Boolean push;
  List<String> permissions;
  Boolean OIDC;
  Long consentExpiration;
  String recoveryEmail;
  Boolean verifiedRecoveryEmail;
  String recoveryPhone;
  Boolean verifiedRecoveryPhone;
  Long modifiedTime;
  /** Order is not guaranteed to match roleNames: don't pair them by index. */
  List<String> roleIds;
  /** Whether the user has set up recovery codes. */
  Boolean recoveryCodes;
  Boolean dependent;
  List<UserFamily> userFamilies;
  /** The tenant whose SCIM connection provisioned the user, empty when none or ambiguous. */
  String scimTenantId;
  /** The tenant whose SAML SSO created the user or merged the user's login into it, empty when none or ambiguous. */
  String samlTenantId;
  /** The tenant whose OIDC SSO created the user or merged the user's login into it, empty when none or ambiguous. */
  String oidcTenantId;
  String consentTenantId;
  /**
   * Last time the user authenticated, in unix seconds (0 when never). On a sign-in response, this is the sign-in
   * before the current one.
   */
  Long lastLoginTime;

  // Keeps the constructor existing callers use
  public UserResponse(String userId, List<String> loginIds, String email, Boolean verifiedEmail, String phone,
      Boolean verifiedPhone, String name, String givenName, String middleName, String familyName,
      List<String> roleNames, List<AssociatedTenant> userTenants, String status, String picture, Boolean test,
      Long createdTime, Map<String, Object> customAttributes, Boolean totp, Boolean saml,
      Map<String, Boolean> oauth, List<String> ssoAppIds, String lockReason, Long tempLockExpiration) {
    this.userId = userId;
    this.loginIds = loginIds;
    this.email = email;
    this.verifiedEmail = verifiedEmail;
    this.phone = phone;
    this.verifiedPhone = verifiedPhone;
    this.name = name;
    this.givenName = givenName;
    this.middleName = middleName;
    this.familyName = familyName;
    this.roleNames = roleNames;
    this.userTenants = userTenants;
    this.status = status;
    this.picture = picture;
    this.test = test;
    this.createdTime = createdTime;
    this.customAttributes = customAttributes;
    this.TOTP = totp;
    this.SAML = saml;
    this.oAuth = oauth;
    this.ssoAppIds = ssoAppIds;
    this.lockReason = lockReason;
    this.tempLockExpiration = tempLockExpiration;
  }

  // API sends these keys; Lombok won't copy JsonAlias to setters
  @JsonAlias({"TOTP"})
  public void setTOTP(Boolean totp) {
    this.TOTP = totp;
  }

  @JsonAlias({"SAML"})
  public void setSAML(Boolean saml) {
    this.SAML = saml;
  }

  @JsonAlias({"OAuth"})
  public void setOAuth(Map<String, Boolean> oauth) {
    this.oAuth = oauth;
  }

  @JsonAlias({"SCIM"})
  public void setSCIM(Boolean scim) {
    this.SCIM = scim;
  }

  @JsonAlias({"OIDC"})
  public void setOIDC(Boolean oidc) {
    this.OIDC = oidc;
  }
}
