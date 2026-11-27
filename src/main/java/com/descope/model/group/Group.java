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
  // The SSO configuration the group came from; "default_ssoid" for the tenant's default
  private String ssoId;

  public Group(String id, String display, List<GroupMember> members) {
    this(id, display, members, null);
  }
}
