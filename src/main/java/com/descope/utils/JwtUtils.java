package com.descope.utils;

import com.descope.exception.ClientFunctionalException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.magiclink.LoginOptions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtUtils {
  private static final long SKEW_SECONDS = TimeUnit.SECONDS.toSeconds(5);

  public static Token getToken(String jwt, Client client) {
    Jws<Claims> claimsJws = getClaimsJws(jwt, client);
    Claims claims = claimsJws.getBody();

    return Token.builder()
        .jwt(jwt)
        .projectId(client.getProjectId())
        .id(claims.getSubject())
        .expiration(claims.getExpiration().getTime())
        .refreshExpiration(claims.get("rexp", Date.class))
        .claims(claims)
        .build();
  }

  public static Jws<Claims> getClaimsJws(String jwt, Client client) {
    JwtParser jwtParser =
        Jwts.parserBuilder().setSigningKeyResolver(new SigningKeyResolverAdapter() {
          @Override
          @SuppressWarnings("rawtypes")
          public Key resolveSigningKey(JwsHeader header, Claims claims) {
            String keyId = header.getKeyId();
            Key k = client.getKey(keyId);
            if (k == null) {
              throw ServerCommonException.invalidSigningKey(String.format("Signing key id %s not found", keyId));
            }
            return k;
          }      
        }).setAllowedClockSkewSeconds(SKEW_SECONDS).build();
    try {
      Jws<Claims> claimsJws = jwtParser.parseClaimsJws(jwt);
      return claimsJws;
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(e);
    }
  }

  public static boolean isJWTRequired(LoginOptions loginOptions) {
    return loginOptions != null && (loginOptions.isStepup() || loginOptions.isMfa());
  }
}
