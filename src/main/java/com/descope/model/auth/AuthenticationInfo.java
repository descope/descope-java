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
  /** Cookie expiration in unix seconds. */
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
  /** Set when an authentication-only SSO verified the person without creating or signing in a user. */
  private Boolean ssoAuthenticationOnly;
  private Boolean userCreationDeferred;

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen) {
    this(token, refreshToken, user, firstSeen, null);
  }

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen,
      IDPResponse idpResponse) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.user = user;
    this.firstSeen = firstSeen;
    this.idpResponse = idpResponse;
  }

  /** Builds the authentication info from a JWT response and its already validated tokens. */
  public static AuthenticationInfo from(JWTResponse jwtResponse, Token token, Token refreshToken) {
    AuthenticationInfo authenticationInfo = new AuthenticationInfo(
        token, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen(), jwtResponse.getIdpResponse());
    authenticationInfo.setCookieDomain(jwtResponse.getCookieDomain());
    authenticationInfo.setCookiePath(jwtResponse.getCookiePath());
    authenticationInfo.setCookieMaxAge(jwtResponse.getCookieMaxAge());
    authenticationInfo.setCookieExpiration(jwtResponse.getCookieExpiration());
    authenticationInfo.setSessionExpiration(jwtResponse.getSessionExpiration());
    authenticationInfo.setExternalToken(jwtResponse.getExternalToken());
    authenticationInfo.setClaims(jwtResponse.getClaims());
    authenticationInfo.setTenantSSOID(jwtResponse.getTenantSSOID());
    authenticationInfo.setTrustedDeviceJwt(jwtResponse.getTrustedDeviceJwt());
    authenticationInfo.setNextRefreshSeconds(jwtResponse.getNextRefreshSeconds());
    authenticationInfo.setCookieName(jwtResponse.getCookieName());
    authenticationInfo.setSessionCookieName(jwtResponse.getSessionCookieName());
    authenticationInfo.setSessionCookieDomain(jwtResponse.getSessionCookieDomain());
    authenticationInfo.setUnsavedSSO(jwtResponse.getUnsavedSSO());
    authenticationInfo.setKnownDeviceJwt(jwtResponse.getKnownDeviceJwt());
    authenticationInfo.setSsoAuthenticationOnly(jwtResponse.getSsoAuthenticationOnly());
    authenticationInfo.setUserCreationDeferred(jwtResponse.getUserCreationDeferred());
    return authenticationInfo;
  }
}
