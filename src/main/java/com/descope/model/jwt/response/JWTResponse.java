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
  private Boolean ssoAuthenticationOnly;
  private Boolean userCreationDeferred;

  public JWTResponse(String sessionJwt, String refreshJwt, String cookieDomain, String cookiePath,
      Integer cookieMaxAge, Integer cookieExpiration, UserResponse user, Boolean firstSeen, IDPResponse idpResponse) {
    this(sessionJwt, refreshJwt, cookieDomain, cookiePath, cookieMaxAge, cookieExpiration, user,
        firstSeen, idpResponse, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  public JWTResponse(String sessionJwt, String refreshJwt, String cookieDomain, String cookiePath,
      Integer cookieMaxAge, Integer cookieExpiration, UserResponse user, Boolean firstSeen) {
    this(sessionJwt, refreshJwt, cookieDomain, cookiePath, cookieMaxAge, cookieExpiration, user,
        firstSeen, null);
  }
}
