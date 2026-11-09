package com.descope.model.auth;

import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
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

  public static AuthenticationInfo from(Token token, Token refreshToken, JWTResponse jwtResponse) {
    AuthenticationInfo info = new AuthenticationInfo(
        token, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen(), jwtResponse.getIdpResponse());
    info.setSessionExpiration(jwtResponse.getSessionExpiration());
    info.setExternalToken(jwtResponse.getExternalToken());
    info.setClaims(jwtResponse.getClaims());
    info.setTenantSSOID(jwtResponse.getTenantSSOID());
    info.setTrustedDeviceJwt(jwtResponse.getTrustedDeviceJwt());
    info.setNextRefreshSeconds(jwtResponse.getNextRefreshSeconds());
    info.setCookieName(jwtResponse.getCookieName());
    info.setSessionCookieName(jwtResponse.getSessionCookieName());
    info.setSessionCookieDomain(jwtResponse.getSessionCookieDomain());
    info.setUnsavedSSO(jwtResponse.getUnsavedSSO());
    info.setKnownDeviceJwt(jwtResponse.getKnownDeviceJwt());
    info.setSsoAuthenticationOnly(jwtResponse.getSsoAuthenticationOnly());
    info.setUserCreationDeferred(jwtResponse.getUserCreationDeferred());
    return info;
  }
}
