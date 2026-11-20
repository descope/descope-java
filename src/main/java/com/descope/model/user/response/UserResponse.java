package com.descope.model.user.response;

import com.descope.model.auth.AssociatedTenant;
import com.fasterxml.jackson.annotation.JsonAlias;
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
  /** IDs of the user's roles, in no particular order relative to roleNames. */
  List<String> roleIds;
  /** Whether the user has set up recovery codes. */
  Boolean recoveryCodes;
  Boolean dependent;
  List<UserFamily> userFamilies;
  /** Tenant whose SCIM connection provisioned the user, empty when none or ambiguous. */
  String scimTenantId;
  /** Tenant whose SAML SSO created the user or merged its login into it, empty when none or ambiguous. */
  String samlTenantId;
  /** Tenant whose OIDC SSO created the user or merged its login into it, empty when none or ambiguous. */
  String oidcTenantId;
  String consentTenantId;
  /** Last time the user authenticated, in unix seconds (0 when never). */
  Long lastLoginTime;

  // Keeps the pre-externalIds all-args constructor available to callers that use it positionally.
  public UserResponse(String userId, List<String> loginIds, String email, Boolean verifiedEmail, String phone,
      Boolean verifiedPhone, String name, String givenName, String middleName, String familyName,
      List<String> roleNames, List<AssociatedTenant> userTenants, String status, String picture, Boolean test,
      Long createdTime, Map<String, Object> customAttributes, Boolean totp, Boolean saml,
      Map<String, Boolean> oauth, List<String> ssoAppIds, String lockReason, Long tempLockExpiration) {
    this(userId, loginIds, email, verifiedEmail, phone, verifiedPhone, name, givenName, middleName, familyName,
        roleNames, userTenants, status, picture, test, createdTime, customAttributes, totp, saml, oauth, ssoAppIds,
        lockReason, tempLockExpiration, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);
  }

  // The server sends "OAuth"; "oauth" is still accepted.
  @JsonAlias("OAuth")
  public void setOAuth(Map<String, Boolean> oauth) {
    this.oAuth = oauth;
  }
}
