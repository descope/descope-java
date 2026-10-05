package com.descope.model.user.request;

import com.descope.enums.UserStatus;
import com.descope.utils.InstantToMillisSerializer;
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
}
