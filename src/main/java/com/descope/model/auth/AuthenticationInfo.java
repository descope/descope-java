package com.descope.model.auth;

import com.descope.model.jwt.Token;
import com.descope.model.user.response.UserResponse;
import java.util.Map;
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
  // Fields below are set by the management JwtService sign-in and sign-up methods
  private Integer sessionExpiration;
  private String externalToken;
  private Map<String, Object> claims;
  private String tenantSSOID;
  private String trustedDeviceJwt;
  private Integer nextRefreshSeconds;
  private String cookieName;
  private String sessionCookieName;
  private String sessionCookieDomain;
  private Boolean unsavedSSO;
  private String knownDeviceJwt;
  private Boolean ssoAuthenticationOnly;
  private Boolean userCreationDeferred;

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen) {
    this(token, refreshToken, user, firstSeen, null);
  }

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen,
      IDPResponse idpResponse) {
    this(token, refreshToken, user, firstSeen, idpResponse, null, null, null, null, null, null, null, null, null,
        null, null, null, null);
  }
}
