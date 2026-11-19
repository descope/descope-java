package com.descope.model.user.response;

import com.descope.model.auth.AssociatedTenant;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  // Aliases read the spec's casing
  @Setter(onMethod_ = @JsonAlias("TOTP"))
  Boolean TOTP;
  @Setter(onMethod_ = @JsonAlias("SAML"))
  Boolean SAML;
  @Setter(onMethod_ = @JsonAlias("OAuth"))
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
  /** Role IDs; order does not match roleNames. */
  List<String> roleIds;
  Boolean recoveryCodes;
  Boolean dependent;
  List<UserFamily> userFamilies;
  String scimTenantId;
  String samlTenantId;
  String oidcTenantId;
  String consentTenantId;
  /** Previous sign-in time in unix seconds, 0 when none. */
  Long lastLoginTime;

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
}
