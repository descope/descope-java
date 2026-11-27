package com.descope.model.password;

import com.descope.model.magiclink.SignUpOptions;
import com.descope.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationPasswordSignUpRequestBody {
  private String loginId;
  private String password;
  private User user;
  private SignUpOptions loginOptions;

  public AuthenticationPasswordSignUpRequestBody(String loginId, String password, User user) {
    this(loginId, password, user, null);
  }
}
