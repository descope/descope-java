package com.descope.model.jwt.response;

import com.descope.model.user.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTResponse {
  private String sessionJwt;
  private String refreshJwt;
  private String cookieDomain;
  private String cookiePath;
  private Integer cookieMaxAge;
  private Integer cookieExpiration;
  private UserResponse user;
  private Boolean firstSeen;
}
