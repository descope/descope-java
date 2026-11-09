package com.descope.model.user.response;

import com.descope.model.auth.AssociatedTenant;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("SCIM")
  Boolean scim;
  Boolean push;
  List<String> permissions;
  @JsonProperty("OIDC")
  Boolean oidc;
  Long consentExpiration;
  String recoveryEmail;
  Boolean verifiedRecoveryEmail;
  String recoveryPhone;
  Boolean verifiedRecoveryPhone;
  Long modifiedTime;
  /** Order doesn't match roleNames: don't pair them by index. */
  List<String> roleIds;
  /** Whether the user has set up recovery codes. */
  Boolean recoveryCodes;
  Boolean dependent;
  List<UserFamily> userFamilies;
  /** Tenant whose SCIM connection provisioned the user, empty when none or ambiguous. */
  String scimTenantId;
  /** Tenant whose SAML SSO created the user or merged its login, empty when none or ambiguous. */
  String samlTenantId;
  /** Tenant whose OIDC SSO created the user or merged its login, empty when none or ambiguous. */
  String oidcTenantId;
  String consentTenantId;
  /**
   * Last sign-in in unix seconds, 0 when never. On a sign-in response it's the sign-in before the current one.
   */
  Long lastLoginTime;

  public UserResponse(String userId, List<String> loginIds, String email, Boolean verifiedEmail, String phone,
      Boolean verifiedPhone, String name, String givenName, String middleName, String familyName,
      List<String> roleNames, List<AssociatedTenant> userTenants, String status, String picture, Boolean test,
      Long createdTime, Map<String, Object> customAttributes, Boolean totp, Boolean saml, Map<String, Boolean> oauth,
      List<String> ssoAppIds, String lockReason, Long tempLockExpiration) {
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
}
