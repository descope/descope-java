package com.descope.model.password;

import com.descope.model.magiclink.LoginOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationPasswordSignInRequestBody {
  private String loginId;
  private String password;
  private String ssoAppId;
  // Client-side fields the spec doesn't accept
  @JsonIgnoreProperties({"jwt", "jwtrequired", "templateId"})
  private LoginOptions loginOptions;

  public AuthenticationPasswordSignInRequestBody(String loginId, String password) {
    this(loginId, password, null, null);
  }
}
