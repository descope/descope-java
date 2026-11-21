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
  private String cookieDomain;
  private String cookiePath;
  private Integer cookieMaxAge;
  private Integer cookieExpiration;
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

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen,
      IDPResponse idpResponse) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.user = user;
    this.firstSeen = firstSeen;
    this.idpResponse = idpResponse;
  }

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen) {
    this(token, refreshToken, user, firstSeen, null);
  }

  /** Builds the authentication info from validated tokens and the response they came from. */
  public static AuthenticationInfo fromJwtResponse(Token token, Token refreshToken, JWTResponse jwtResponse) {
    AuthenticationInfo authInfo = new AuthenticationInfo(token, refreshToken, jwtResponse.getUser(),
        jwtResponse.getFirstSeen(), jwtResponse.getIdpResponse());
    authInfo.setCookieDomain(jwtResponse.getCookieDomain());
    authInfo.setCookiePath(jwtResponse.getCookiePath());
    authInfo.setCookieMaxAge(jwtResponse.getCookieMaxAge());
    authInfo.setCookieExpiration(jwtResponse.getCookieExpiration());
    authInfo.setSessionExpiration(jwtResponse.getSessionExpiration());
    authInfo.setExternalToken(jwtResponse.getExternalToken());
    authInfo.setClaims(jwtResponse.getClaims());
    authInfo.setTenantSSOID(jwtResponse.getTenantSSOID());
    authInfo.setTrustedDeviceJwt(jwtResponse.getTrustedDeviceJwt());
    authInfo.setNextRefreshSeconds(jwtResponse.getNextRefreshSeconds());
    authInfo.setCookieName(jwtResponse.getCookieName());
    authInfo.setSessionCookieName(jwtResponse.getSessionCookieName());
    authInfo.setSessionCookieDomain(jwtResponse.getSessionCookieDomain());
    authInfo.setUnsavedSSO(jwtResponse.getUnsavedSSO());
    authInfo.setKnownDeviceJwt(jwtResponse.getKnownDeviceJwt());
    authInfo.setSsoAuthenticationOnly(jwtResponse.getSsoAuthenticationOnly());
    authInfo.setUserCreationDeferred(jwtResponse.getUserCreationDeferred());
    return authInfo;
  }
}
