package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.AUTHORIZATION_HEADER_NAME;
import static com.descope.literals.AppConstants.BEARER_AUTHORIZATION_PREFIX;
import static com.descope.literals.AppConstants.COOKIE;
import static com.descope.literals.AppConstants.REFRESH_COOKIE_NAME;
import static com.descope.literals.AppConstants.SESSION_COOKIE_NAME;
import static com.descope.literals.Routes.AuthEndPoints.REFRESH_TOKEN_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthParams;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.model.magiclink.Tokens;
import com.descope.model.magiclink.response.Masked;
import com.descope.model.magiclink.response.MaskedEmailRes;
import com.descope.model.magiclink.response.MaskedPhoneRes;
import com.descope.model.user.User;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.SdkServicesBase;
import com.descope.sdk.auth.AuthenticationService;
import com.descope.utils.JwtUtils;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
abstract class AuthenticationsBase extends SdkServicesBase implements AuthenticationService {
  private final AuthParams authParams;

  AuthenticationsBase(Client client, AuthParams authParams) {
    super(client, authParams.getProjectId());
    this.authParams = authParams;
  }

  ApiProxy getApiProxy() {
    String projectId = authParams.getProjectId();
    if (StringUtils.isNotBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId, client.getSdkInfo());
    }
    return ApiProxyBuilder.buildProxy(client.getSdkInfo());
  }

  ApiProxy getApiProxy(String refreshToken) {
    String projectId = authParams.getProjectId();
    if (StringUtils.isBlank(refreshToken) || StringUtils.isBlank(projectId)) {
      return getApiProxy();
    }

    String token = String.format("Bearer %s:%s", projectId, refreshToken);
    return ApiProxyBuilder.buildProxy(() -> token, client.getSdkInfo());
  }

  void verifyDeliveryMethod(DeliveryMethod deliveryMethod, String loginId, User user) {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    switch (deliveryMethod) {
      case SMS:
      case WHATSAPP:
        String phone = user.getPhone();
        if (StringUtils.isBlank(phone)) {
          phone = loginId;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
          throw ServerCommonException.invalidArgument("user.phone");
        }
        break;
      case EMAIL:
        String email = user.getEmail();
        if (StringUtils.isBlank(email)) {
          email = loginId;
          user.setEmail(email);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
          throw ServerCommonException.invalidArgument("user.email");
        }
        break;
      default:
        throw ServerCommonException.invalidArgument("DeliveryMethod");
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

  String getValidRefreshToken(HttpRequest request) {
    Tokens tokens = provideTokens(request);
    if (isEmpty(tokens.getRefreshToken())) {
      throw ServerCommonException.refreshToken("Unable to find tokens from cookies");
    }
    return tokens.getRefreshToken();
  }

  Tokens provideTokens(HttpRequest request) {
    if (request == null) {
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
        if (sessionCookie != null) {
          tokens.setSessionToken(sessionCookie.split("=")[1]);
        }

        String refreshCookie =
            Arrays.stream(cookiesList)
                .filter(cookie -> cookie.contains(REFRESH_COOKIE_NAME))
                .findAny()
                .orElse(null);
        if (refreshCookie != null) {
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
    validateJWT(refreshToken);
    var apiProxy = getApiProxy(refreshToken);
    URI refreshTokenLinkURL = composeRefreshTokenLinkURL();

    var jwtResponse = apiProxy.post(refreshTokenLinkURL, null, JWTResponse.class);
    var authenticationInfo = getAuthenticationInfo(jwtResponse);

    return authenticationInfo.getToken();
  }

  AuthenticationInfo getAuthenticationInfo(JWTResponse jwtResponse) {
    Token sessionToken = validateAndCreateToken(jwtResponse.getSessionJwt());
    Token refreshToken = validateAndCreateToken(jwtResponse.getRefreshJwt());
    return new AuthenticationInfo(
        sessionToken, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen());
  }

  List<String> getAuthorizationClaimItems(Token token, String tenant, List<String> permissions) {
    if (tenant == null || MapUtils.isEmpty(token.getClaims())) {
      return Collections.emptyList();
    }

    return token.getClaims().keySet().stream()
        .filter(permissions::contains)
        .collect(Collectors.toList());
  }

  private URI composeRefreshTokenLinkURL() {
    return getUri(REFRESH_TOKEN_LINK);
  }
}
