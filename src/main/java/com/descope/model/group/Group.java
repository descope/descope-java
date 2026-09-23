package com.descope.model.group;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {
  private String id;
  private String display;
  private List<GroupMember> members;
  /**
   * Origin of the group: "scim" when SCIM owns its definition, or "jit" when it was created from an
   * SSO assertion at login. Members whose own source is empty belong to this source. Read-only.
   */
  private String source;
}
