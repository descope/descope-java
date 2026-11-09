package com.descope.model.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationPasswordReplaceRequestBody {
  private String loginId;
  private String oldPassword;
  private String newPassword;
  private Boolean revokeOtherSessions;
  private String[] revokeOtherSessionsTypes;

  public AuthenticationPasswordReplaceRequestBody(String loginId, String oldPassword, String newPassword) {
    this(loginId, oldPassword, newPassword, null, null);
  }
}
