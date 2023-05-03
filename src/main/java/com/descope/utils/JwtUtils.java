package com.descope.utils;

import static java.util.Objects.nonNull;

import com.descope.model.jwt.Token;
import com.descope.model.magiclink.LoginOptions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtUtils {
  private static final long SKEW_SECONDS = TimeUnit.SECONDS.toSeconds(5);

  public static Token getToken(String jwt, Key key) {
    var jwtParser =
        Jwts.parserBuilder().setSigningKey(key).setAllowedClockSkewSeconds(SKEW_SECONDS).build();
    Jws<Claims> claimsJws = jwtParser.parseClaimsJws(jwt);
    JwsHeader<?> header = claimsJws.getHeader();
    var claims = claimsJws.getBody();

    return Token.builder()
        .jwt(jwt)
        .projectId(header.getKeyId())
        .id(claims.getId())
        .expiration(claims.getExpiration().getTime())
        .claims(claims)
        .build();
  }

  public static boolean isJWTRequired(LoginOptions loginOptions) {
    return nonNull(loginOptions) && (loginOptions.isStepUp() || loginOptions.isMfa());
  }
}
