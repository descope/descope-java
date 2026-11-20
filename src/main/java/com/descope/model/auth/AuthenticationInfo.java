package com.descope.model.auth;

import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
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
  private Integer sessionExpiration;
  private String externalToken;
  private String tenantSSOID;
  private String trustedDeviceJwt;
  private Integer nextRefreshSeconds;
  private String cookieName;
  private String sessionCookieName;
  private String sessionCookieDomain;
  private Boolean unsavedSSO;
  private String knownDeviceJwt;
  /** Set only when the login went through an SSO configuration classified as authentication only. */
  private Boolean ssoAuthenticationOnly;
  private Boolean userCreationDeferred;

  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen) {
    this(token, refreshToken, user, firstSeen, null);
  }

  // Keeps the pre-sessionExpiration all-args constructor available to callers that use it positionally.
  public AuthenticationInfo(Token token, Token refreshToken, UserResponse user, Boolean firstSeen,
      IDPResponse idpResponse) {
    this(token, refreshToken, user, firstSeen, idpResponse, null, null, null, null, null, null, null, null, null,
        null, null, null);
  }

  /** Builds the info from a JWT response, copying every response field. */
  public static AuthenticationInfo fromJwtResponse(Token token, Token refreshToken, JWTResponse jwtResponse) {
    AuthenticationInfo info = new AuthenticationInfo(token, refreshToken, jwtResponse.getUser(),
        jwtResponse.getFirstSeen(), jwtResponse.getIdpResponse());
    info.setSessionExpiration(jwtResponse.getSessionExpiration());
    info.setExternalToken(jwtResponse.getExternalToken());
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
