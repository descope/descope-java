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
  /** ID of the tenant SSO configuration that performed the authentication. */
  private String tenantSSOID;
  private String trustedDeviceJwt;
  private Integer nextRefreshSeconds;
  private String cookieName;
  private String sessionCookieName;
  private String sessionCookieDomain;
  private Boolean unsavedSSO;
  private String knownDeviceJwt;
  /** Set when the login only verified the person: no user was created, updated or signed in. */
  private Boolean ssoAuthenticationOnly;
  private Boolean userCreationDeferred;

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen,
      IDPResponse idpResponse) {
    this(token, refreshToken, user, firstSeen, idpResponse, null, null, null, null, null, null, null, null, null,
        null, null, null, null);
  }

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen) {
    this(token, refreshToken, user, firstSeen, null);
  }

  public static AuthenticationInfo fromJWTResponse(Token token, Token refreshToken, JWTResponse jwtResponse) {
    AuthenticationInfo info = new AuthenticationInfo(token, refreshToken, jwtResponse.getUser(),
        jwtResponse.getFirstSeen(), jwtResponse.getIdpResponse());
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
