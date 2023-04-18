package com.descope.model.auth;

import com.descope.model.jwt.Token;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationInfo {
  private Token token;
  private Token refreshToken;
  private UserResponse user;
  private Boolean firstSeen;
}
