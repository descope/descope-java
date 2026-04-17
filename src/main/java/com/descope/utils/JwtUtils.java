package com.descope.utils;

import com.descope.exception.ClientFunctionalException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.VerifyOptions;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.magiclink.LoginOptions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtUtils {
  private static final long SKEW_SECONDS = TimeUnit.SECONDS.toSeconds(5);

  /**
   * Parse and validate the given JWT against the Descope project's signing keys.
   * This is the legacy entry point that does not perform audience validation.
   *
   * @param jwt    the raw JWT string
   * @param client the configured Descope client (for signing key lookup)
   * @return a {@link Token} representing the validated claims
   */
  public static Token getToken(String jwt, Client client) {
    return getToken(jwt, client, null);
  }

  /**
   * Parse and validate the given JWT against the Descope project's signing keys, optionally
   * verifying additional claims (e.g., {@code aud}) via {@link VerifyOptions}.
   *
   * <p>This overload mirrors the Node SDK's
   * {@code validateJwt(jwt, { audience })} behavior: if the {@link VerifyOptions} audiences list
   * is non-empty, the token's {@code aud} claim must contain at least one of the expected values.
   *
   * @param jwt     the raw JWT string
   * @param client  the configured Descope client (for signing key lookup)
   * @param options optional verification options; may be {@code null} for default behavior
   * @return a {@link Token} representing the validated claims
   */
  public static Token getToken(String jwt, Client client, VerifyOptions options) {
    Jws<Claims> claimsJws = getClaimsJws(jwt, client);
    Claims claims = claimsJws.getPayload();

    verifyAudience(claims, options);

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
    JwtParserBuilder jwtParserBuilder = Jwts.parser()
        .keyLocator(new Locator<Key>() {
          @Override
          public Key locate(Header header) {
            if (header instanceof JwsHeader) {
              String keyId = ((JwsHeader) header).getKeyId();
              Key k = client.getKey(keyId);
              if (k == null) {
                throw ServerCommonException.invalidSigningKey(String.format("Signing key id %s not found", keyId));
              }
              return k;
            }
            throw ServerCommonException.invalidSigningKey("Header is not a JwsHeader");
          }
        })
        .clockSkewSeconds(SKEW_SECONDS);

    try {
      Jws<Claims> claimsJws = jwtParserBuilder.build().parseSignedClaims(jwt);
      return claimsJws;
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(e);
    }
  }

  /**
   * Verify that the token's {@code aud} claim contains at least one of the expected audiences from
   * {@link VerifyOptions}. If {@code options} is {@code null} or contains no audiences, validation
   * is skipped.
   *
   * <p>Package-private so it can be exercised directly by unit tests without needing a live
   * signing key.
   *
   * @param claims  the parsed JWT claims
   * @param options verification options; may be {@code null}
   * @throws com.descope.exception.DescopeException if the token has no matching audience
   */
  static void verifyAudience(Claims claims, VerifyOptions options) {
    if (options == null) {
      return;
    }
    List<String> expected = options.getAudiencesOrEmpty();
    if (expected == null || expected.isEmpty()) {
      return;
    }
    Set<String> tokenAudiences = claims == null ? Collections.emptySet() : claims.getAudience();
    if (tokenAudiences == null || tokenAudiences.isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("token is missing required 'aud' claim"));
    }
    for (String want : expected) {
      if (want != null && tokenAudiences.contains(want)) {
        return;
      }
    }
    throw ClientFunctionalException.invalidToken(
        new IllegalArgumentException(
            "token 'aud' claim " + tokenAudiences + " does not match any expected audience "
                + expected));
  }

  public static boolean isJWTRequired(LoginOptions loginOptions) {
    return loginOptions != null && (loginOptions.isStepup() || loginOptions.isMfa());
  }
}
