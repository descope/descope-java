package com.descope.model.auth;

import com.descope.model.jwt.Token;
import com.descope.model.user.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationInfo {
  private Token token; // session token
  private Token refreshToken;
  private UserResponse user;
  private Boolean firstSeen;
  private IDPResponse idpResponse;
  // True when an authentication-only SSO login issued no session, so token is null
  private Boolean ssoAuthenticationOnly;

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen) {
    this(token, refreshToken, user, firstSeen, null);
  }

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen,
      IDPResponse idpResponse) {
    this(token, refreshToken, user, firstSeen, idpResponse, null);
  }
}
