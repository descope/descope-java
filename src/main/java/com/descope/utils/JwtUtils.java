package com.descope.utils;

import com.descope.model.jwt.Token;
import com.descope.model.magiclink.LoginOptions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtUtils {
  private static final long SKEW_SECONDS = TimeUnit.SECONDS.toSeconds(5);

  public static Token getToken(String jwt, Key key) {
    Jws<Claims> claimsJws = getClaimsJws(jwt, key);
    JwsHeader<?> header = claimsJws.getHeader();
    var claims = claimsJws.getBody();

    return Token.builder()
        .jwt(jwt)
        .projectId(header.getKeyId())
        .id(claims.getSubject())
        .expiration(claims.getExpiration().getTime())
        .refreshExpiration(claims.get("rexp", Date.class))
        .claims(claims)
        .build();
  }

  public static Jws<Claims> getClaimsJws(String jwt, Key key) {
    var jwtParser =
        Jwts.parserBuilder().setSigningKey(key).setAllowedClockSkewSeconds(SKEW_SECONDS).build();
    Jws<Claims> claimsJws = jwtParser.parseClaimsJws(jwt);
    return claimsJws;
  }

  public static boolean isJWTRequired(LoginOptions loginOptions) {
    return loginOptions != null && (loginOptions.isStepUp() || loginOptions.isMfa());
  }
}
