package com.descope.model.group;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
  @JsonAlias("loginId")
  private String loginID;
  @JsonAlias("userId")
  private String userID;
  private String display;
}
