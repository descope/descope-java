package com.descope.model.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
  private String loginID;
  private String userID;
  private String display;
  /**
   * Which side added this member to the group: "scim" (pushed by SCIM) or "jit" (added from an SSO
   * assertion at login). Empty for memberships recorded before provenance was tracked, which belong
   * to the group's own source. A SCIM group can hold JIT members, because SSO assertions keep their
   * own membership so the user's idpGroups claim survives a token refresh. Read-only.
   */
  private String source;
}
