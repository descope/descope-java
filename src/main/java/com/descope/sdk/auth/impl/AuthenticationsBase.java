package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.AUTHORIZATION_HEADER_NAME;
import static com.descope.literals.AppConstants.BEARER_AUTHORIZATION_PREFIX;
import static com.descope.literals.AppConstants.COOKIE;
import static com.descope.literals.AppConstants.REFRESH_COOKIE_NAME;
import static com.descope.literals.AppConstants.SESSION_COOKIE_NAME;
import static com.descope.literals.Routes.AuthEndPoints.GET_KEYS_LINK;
import static com.descope.literals.Routes.AuthEndPoints.REFRESH_TOKEN_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ClientFunctionalException;
import com.descope.exception.ServerCommonException;
import com.descope.model.User;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.JWTResponse;
import com.descope.model.jwt.Provider;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.Token;
import com.descope.model.magiclink.Masked;
import com.descope.model.magiclink.MaskedEmailRes;
import com.descope.model.magiclink.MaskedPhoneRes;
import com.descope.model.magiclink.Tokens;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.utils.JwtUtils;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpRequest;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
abstract class AuthenticationsBase implements AuthenticationService {

  protected final Client client;
  private final AuthParams authParams;
  private final Provider provider;

  AuthenticationsBase(Client client, AuthParams authParams) {
    this.client = client;
    this.authParams = authParams;
    this.provider =
        Provider.builder().client(client).authParams(authParams).keyMap(new HashMap<>()).build();
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

  ApiProxy getApiProxy() {
    String projectId = authParams.getProjectId();
    if (StringUtils.isNotBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId);
    }
    return ApiProxyBuilder.buildProxy();
  }

  ApiProxy getApiProxy(String refreshToken) {
    String projectId = authParams.getProjectId();
    if (StringUtils.isBlank(refreshToken) || StringUtils.isNotBlank(projectId)) {
      return getApiProxy();
    }

    String token = String.format("Bearer %s:%s", projectId, refreshToken);
    return ApiProxyBuilder.buildProxy(() -> token);
  }

  @SneakyThrows
  Key requestKeys() {
    if (Objects.nonNull(provider.getProvidedKey())) {
      return provider.getProvidedKey();
    }

    // TODO - Cache keys | 18/04/23 | by keshavram

    String projectId = authParams.getProjectId();
    var apiProxy = getApiProxy();
    var uri = composeGetKeysURI(projectId);
    var signingKeys = apiProxy.get(uri, SigningKey[].class);

    if (ArrayUtils.isEmpty(signingKeys)) {
      // TODO - Throw valid Exception | 18/04/23 | by keshavram
      throw new RuntimeException();
    }

    // TODO - Understand the concept | 18/04/23 | by keshavram
    // Will have rotating keys
    var signingKey = signingKeys[0];
    var publicKey = getPublicKey(signingKey);

    provider.setProvidedKey(publicKey);
    return publicKey;
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

  Token validateAndCreateToken(String jwt) {
    if (StringUtils.isBlank(jwt)) {
      throw ClientFunctionalException.invalidToken();
    }
    return JwtUtils.getToken(jwt, requestKeys());
  }

  String getValidRefreshToken(HttpRequest request) {
    Tokens tokens = provideTokens(request);
    if (isEmpty(tokens.getRefreshToken())) {
      throw ServerCommonException.refreshToken("Unable to find tokens from cookies");
    }
    return tokens.getRefreshToken();
  }

  Tokens provideTokens(HttpRequest request) {
    if (isNull(request)) {
      return Tokens.builder().build();
    }

    Tokens tokens = new Tokens();
    Optional<String> authToken = request.headers().firstValue(AUTHORIZATION_HEADER_NAME);
    if (authToken.isPresent()) {
      try {
        String sessionToken = getSessionTokenFromBearerToken(authToken.get());
        tokens.setSessionToken(sessionToken);
      } catch (ServerCommonException e) {
        log.warn(e.getMessage());
      }
    }

    if (isEmpty(tokens.getSessionToken())) {
      Optional<String> cookies = request.headers().firstValue(COOKIE);
      if (cookies.isPresent()) {
        String[] cookiesList = cookies.get().split(";");
        String sessionCookie =
            Arrays.stream(cookiesList)
                .filter(cookie -> cookie.contains(SESSION_COOKIE_NAME))
                .map(String::trim)
                .findAny()
                .orElse(null);
        if (nonNull(sessionCookie)) {
          tokens.setSessionToken(sessionCookie.split("=")[1]);
        }

        String refreshCookie =
            Arrays.stream(cookiesList)
                .filter(cookie -> cookie.contains(REFRESH_COOKIE_NAME))
                .findAny()
                .orElse(null);
        if (nonNull(refreshCookie)) {
          tokens.setRefreshToken(refreshCookie.split("=")[1]);
        }
      }
    }

    return tokens;
  }

  String getSessionTokenFromBearerToken(String bearerToken) {
    if (StringUtils.isNotBlank(bearerToken)) {
      String[] sessionTokens = bearerToken.split(BEARER_AUTHORIZATION_PREFIX);
      if (sessionTokens.length == 2) {
        try {
          return sessionTokens[1];
        } catch (ArrayIndexOutOfBoundsException e) {
          throw ServerCommonException.invalidArgument("bearerToken");
        }
      }
    }
    throw ServerCommonException.invalidArgument("bearerToken");
  }

  Token validateJWT(String jwt) {
    return JwtUtils.getToken(jwt, requestKeys());
  }

  Token refreshSession(String refreshToken) {
    var token = validateJWT(refreshToken);
    var apiProxy = getApiProxy(refreshToken);
    URI refreshTokenLinkURL = composeRefreshTokenLinkURL();

    var jwtResponse = apiProxy.post(refreshTokenLinkURL, null, JWTResponse.class);
    var authenticationInfo = getAuthenticationInfo(jwtResponse);

    Token sessionToken = authenticationInfo.getToken();
    sessionToken.setExpiration(token.getExpiration());

    return sessionToken;
  }

  AuthenticationInfo getAuthenticationInfo(JWTResponse jwtResponse) {
    Token sessionToken = validateAndCreateToken(jwtResponse.getSessionJwt());
    Token refreshToken = validateAndCreateToken(jwtResponse.getRefreshJwt());

    // TODO - Set Cookies | 18/04/23 | by keshavram

    return new AuthenticationInfo(
        sessionToken, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen());
  }

   List<String> getAuthorizationClaimItems(
      Token token, String tenant, List<String> permissions) {
    if (Objects.isNull(tenant) || MapUtils.isEmpty(token.getClaims())) {
      return Collections.emptyList();
    }

    // TODO - Understand Tenant Roles | 08/05/23 | by keshavram

    return token.getClaims().keySet().stream()
        .filter(permissions::contains)
        .collect(Collectors.toList());
  }

  private URI composeRefreshTokenLinkURL() {
    return getUri(REFRESH_TOKEN_LINK);
  }
}
