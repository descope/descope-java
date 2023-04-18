package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.GET_KEYS_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ClientFunctionalException;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.client.Client;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.magiclink.Masked;
import com.descope.model.magiclink.MaskedEmailRes;
import com.descope.model.magiclink.MaskedPhoneRes;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import java.math.BigInteger;
import java.net.URI;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

abstract class AuthenticationsBase implements AuthenticationService {

  private static final long SKEW_SECONDS = TimeUnit.SECONDS.toSeconds(5);
  protected final Client client;
  private final AuthParams authParams;
  private final Provider provider;

  AuthenticationsBase(Client client, AuthParams authParams) {
    this.client = client;
    this.authParams = authParams;
    this.provider =
        Provider.builder().client(client).authParams(authParams).keyMap(new HashMap<>()).build();
  }

  ApiProxy getApiProxy() {
    String projectId = authParams.getProjectId();
    if (StringUtils.isNotBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId);
    }
    return ApiProxyBuilder.buildProxy();
  }

  @SneakyThrows
  Key requestKeys() {
    if (Objects.nonNull(provider.getProvidedKey())) {
      return provider.getProvidedKey();
    }

    String projectId = authParams.getProjectId();
    var apiProxy = getApiProxy();
    var uri = composeGetKeysURI(projectId);
    var signingKeys = apiProxy.get(uri, SigningKey[].class);

    if (ArrayUtils.isEmpty(signingKeys)) {
      // TODO - Throw valid Exception | 18/04/23 | by keshavram
      throw new RuntimeException();
    }

    // TODO - Understand the concept | 18/04/23 | by keshavram
    var signingKey = signingKeys[0];
    var publicKey = getPublicKey(signingKey);

    provider.setProvidedKey(publicKey);
    return publicKey;
  }

  @SneakyThrows
  // https://mojoauth.com/blog/jwt-validation-with-jwks-java/
  private static PublicKey getPublicKey(SigningKey signingKey) {
    byte[] exponentB = Base64.getUrlDecoder().decode(signingKey.getE());
    byte[] modulusB = Base64.getUrlDecoder().decode(signingKey.getN());
    BigInteger bigExponent = new BigInteger(1, exponentB);
    BigInteger bigModulus = new BigInteger(1, modulusB);

    return KeyFactory.getInstance(signingKey.getKty())
        .generatePublic(new RSAPublicKeySpec(bigModulus, bigExponent));
  }

  URI composeGetKeysURI(String projectId) {
    return composeURI(GET_KEYS_LINK, projectId);
  }

  void verifyDeliveryMethod(DeliveryMethod deliveryMethod, String loginId, User user) {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    switch (deliveryMethod) {
      case SMS:
      case WHATSAPP:
        {
          String phone = user.getPhone();
          if (StringUtils.isBlank(phone)) {
            phone = loginId;
          }
          if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw ServerCommonException.invalidArgument("user.phone");
          }
          break;
        }
      case EMAIL:
        {
          String email = user.getEmail();
          if (StringUtils.isBlank(email)) {
            email = loginId;
            user.setEmail(email);
          }
          if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw ServerCommonException.invalidArgument("user.email");
          }
          break;
        }
    }
  }

  Class<? extends Masked> getMaskedValue(DeliveryMethod deliveryMethod) {
    switch (deliveryMethod) {
      case SMS:
      case WHATSAPP:
        return MaskedPhoneRes.class;
      case EMAIL:
        return MaskedEmailRes.class;
      default:
        throw new IllegalStateException("Unexpected value: " + deliveryMethod);
    }
  }

  URI composeURI(String base, String path) {
    URI uri = getUri(base);
    return addPath(uri, path);
  }

  URI getUri(String path) {
    return URI.create(client.getUri() + (path.startsWith("/") ? path : "/" + path));
  }

  private URI addPath(URI uri, String path) {
    String newPath;
    if (path.startsWith("/")) newPath = path.replaceAll("//+", "/");
    else if (uri.getPath().endsWith("/")) newPath = uri.getPath() + path.replaceAll("//+", "/");
    else newPath = uri.getPath() + "/" + path.replaceAll("//+", "/");

    return uri.resolve(newPath).normalize();
  }

  Token createToken(String jwt) {
    if (StringUtils.isBlank(jwt)) {
      throw ClientFunctionalException.invalidToken();
    }

    var jwtParser =
        Jwts.parserBuilder()
            .setSigningKey(requestKeys())
            .setAllowedClockSkewSeconds(SKEW_SECONDS)
            .build();
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
}
