package com.descope.model.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
  // The server sends loginId / userId; the mapper is case sensitive, so the accessor names here only
  // bind with an explicit property name.
  @JsonProperty("loginId")
  private String loginID;

  @JsonProperty("userId")
  private String userID;

  private String display;

  /**
   * Which side added this member to the group: "scim" when SCIM pushed it, or "jit" when an SSO
   * assertion added it at login. A SCIM-owned group can hold JIT members, because an SSO assertion
   * keeps its own membership so the user's idpGroups claim survives a token refresh. Read-only.
   */
  private String source;

  /** Kept so callers written against the three-field type still compile. */
  public GroupMember(String loginID, String userID, String display) {
    this(loginID, userID, display, null);
  }
}
