package com.descope.model.jwt.response;

import com.descope.model.auth.IDPResponse;
import com.descope.model.user.response.UserResponse;
import java.util.Map;
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
  /** Set only when the login went through an SSO configuration classified authentication only. */
  private Boolean ssoAuthenticationOnly;
  private Boolean userCreationDeferred;

  public JWTResponse(String sessionJwt, String refreshJwt, String cookieDomain, String cookiePath,
      Integer cookieMaxAge, Integer cookieExpiration, UserResponse user, Boolean firstSeen, IDPResponse idpResponse) {
    this(sessionJwt, refreshJwt, cookieDomain, cookiePath, cookieMaxAge, cookieExpiration, user, firstSeen);
    this.idpResponse = idpResponse;
  }

  public JWTResponse(String sessionJwt, String refreshJwt, String cookieDomain, String cookiePath,
      Integer cookieMaxAge, Integer cookieExpiration, UserResponse user, Boolean firstSeen) {
    this.sessionJwt = sessionJwt;
    this.refreshJwt = refreshJwt;
    this.cookieDomain = cookieDomain;
    this.cookiePath = cookiePath;
    this.cookieMaxAge = cookieMaxAge;
    this.cookieExpiration = cookieExpiration;
    this.user = user;
    this.firstSeen = firstSeen;
  }
}
