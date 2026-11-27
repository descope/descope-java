package com.descope.model.user.request;

import com.descope.enums.UserStatus;
import com.descope.utils.InstantToMillisSerializer;
import com.descope.utils.InstantToMillisStringSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
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
public class UserSearchRequest {
  List<String> tenantIds;
  List<String> roles; // Roles should not be used unless you have the role IDs
  List<String> roleNames; // Search by role names
  @Builder.Default
  Integer limit = 0;
  @Builder.Default
  Integer page = 0;
  Boolean withTestUser;
  Boolean testUsersOnly;
  Map<String, Object> customAttributes;
  List<UserStatus> statuses;
  List<String> emails;
  List<String> phones;
  List<String> loginIds;
  List<String> userIds;
  List<String> ssoAppIds;
  /** Retrieve only users created after the given time. */
  @JsonSerialize(using = InstantToMillisSerializer.class)
  Instant fromCreatedTime;
  /** Retrieve only users created before the given time. */
  @JsonSerialize(using = InstantToMillisSerializer.class)
  Instant toCreatedTime;
  /** Retrieve only users updated after the given time. */
  @JsonSerialize(using = InstantToMillisSerializer.class)
  Instant fromModifiedTime;
  /** Retrieve only users updated before the given time. */
  @JsonSerialize(using = InstantToMillisSerializer.class)
  Instant toModifiedTime;
  Map<String, RolesList> tenantRoleIds;
  Map<String, RolesList> tenantRoleNames;
  /**
   * Retrieve only users disabled by brute-force protection with one of these lock reasons:
   * password, totp, recovery_codes, security_questions.
   */
  List<String> lockReasons;
  /**
   * Retrieve only users currently temporarily locked (tempLockExpiration greater than now) with one
   * of these lock reasons: password, recovery_codes, security_questions (TOTP has no temporary lock).
   */
  List<String> tempLockReasons;
  /** Free text search on many of the user properties like custom properties, name, email, etc. */
  String text;
  /** Search for a specific login ID. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String identifier;
  /** Retrieve only users that have SSO external IDs. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean ssoOnly;
  /** Sort order of the results. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<SortField> sort;
  /** Retrieve only users that were provisioned or updated by SCIM. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean scimOnly;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean includeSubTenants;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean password;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean totp;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean webauthn;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean scim;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> selectedColumns;
  /** If set, filter users by whether their email is verified. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean verifiedEmail;
  /** If set, filter users by whether their phone is verified. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean verifiedPhone;
  /** Direct row offset (0-based). When positive, it takes precedence over page * limit. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Integer offset;
  /** Retrieve only users with one of these recovery emails (exact match). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> recoveryEmails;
  /** Retrieve only users with one of these recovery phones (exact match). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> recoveryPhones;
  /** If set, filter users by whether their recovery email is verified. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean verifiedRecoveryEmail;
  /** If set, filter users by whether their recovery phone is verified. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean verifiedRecoveryPhone;
  /**
   * Structured field filters, ANDed with the other filters. At most 50 items. Only honored for
   * widget-originated requests.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<SearchField> searchFields;
  /** Retrieve only users that last authenticated at or after the given time. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonSerialize(using = InstantToMillisStringSerializer.class)
  Instant fromLastLoginTime;
  /** Retrieve only users that last authenticated at or before the given time. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonSerialize(using = InstantToMillisStringSerializer.class)
  Instant toLastLoginTime;
  /** Retrieve only users that are members of at least one of these families. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> familyIds;
  /** If set, filter users by whether they are a family dependent. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Boolean dependent;

  // Keeps the previous all-args constructor binary compatible
  public UserSearchRequest(List<String> tenantIds, List<String> roles, List<String> roleNames, Integer limit,
      Integer page, Boolean withTestUser, Boolean testUsersOnly, Map<String, Object> customAttributes,
      List<UserStatus> statuses, List<String> emails, List<String> phones, List<String> loginIds,
      List<String> userIds, List<String> ssoAppIds, Instant fromCreatedTime, Instant toCreatedTime,
      Instant fromModifiedTime, Instant toModifiedTime, Map<String, RolesList> tenantRoleIds,
      Map<String, RolesList> tenantRoleNames, List<String> lockReasons, List<String> tempLockReasons,
      String text) {
    this(tenantIds, roles, roleNames, limit, page, withTestUser, testUsersOnly, customAttributes, statuses, emails,
        phones, loginIds, userIds, ssoAppIds, fromCreatedTime, toCreatedTime, fromModifiedTime, toModifiedTime,
        tenantRoleIds, tenantRoleNames, lockReasons, tempLockReasons, text, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }
}
