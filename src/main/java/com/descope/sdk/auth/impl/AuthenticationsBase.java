package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.BEARER_AUTHORIZATION_PREFIX;
import static com.descope.literals.AppConstants.TENANTS_CLAIM_KEY;
import static com.descope.literals.Routes.AuthEndPoints.REFRESH_TOKEN_LINK;
import static com.descope.utils.PatternUtils.EMAIL_PATTERN;
import static com.descope.utils.PatternUtils.PHONE_PATTERN;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

abstract class AuthenticationsBase extends SdkServicesBase implements AuthenticationService {

  AuthenticationsBase(Client client) {
    super(client);
  }

  ApiProxy getApiProxy() {
    String projectId = client.getProjectId();
    if (StringUtils.isNotBlank(projectId)) {
      return ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId, client);
    }
    return ApiProxyBuilder.buildProxy(client.getSdkInfo());
  }

  ApiProxy getApiProxy(String refreshToken) {
    String projectId = client.getProjectId();
    if (StringUtils.isBlank(refreshToken) || StringUtils.isBlank(projectId)) {
      return getApiProxy();
    }

    String token = String.format("Bearer %s:%s", projectId, refreshToken);
    return ApiProxyBuilder.buildProxy(() -> token, client);
  }

  void verifyDeliveryMethod(DeliveryMethod deliveryMethod, String loginId, User user) {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }

    switch (deliveryMethod) {
      case SMS:
      case VOICE:
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
      case VOICE:
      case WHATSAPP:
        return MaskedPhoneRes.class;
      case EMAIL:
        return MaskedEmailRes.class;
      default:
        throw new IllegalStateException("Unexpected value: " + deliveryMethod);
    }
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
    return JwtUtils.getToken(jwt, client);
  }

  AuthenticationInfo refreshSession(String refreshToken) {
    validateJWT(refreshToken);
    ApiProxy apiProxy = getApiProxy(refreshToken);
    URI refreshTokenLinkURL = composeRefreshTokenLinkURL();

    JWTResponse jwtResponse = apiProxy.post(refreshTokenLinkURL, null, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  AuthenticationInfo getAuthenticationInfo(JWTResponse jwtResponse) {
    Token sessionToken = validateAndCreateToken(jwtResponse.getSessionJwt());
    Token refreshToken = null;
    if (StringUtils.isNotBlank(jwtResponse.getRefreshJwt())) {
      refreshToken = validateAndCreateToken(jwtResponse.getRefreshJwt());
    }
    return new AuthenticationInfo(
        sessionToken, refreshToken, jwtResponse.getUser(), jwtResponse.getFirstSeen());
  }

  @SuppressWarnings("unchecked")
  public List<String> getTenantIds(Token token) {
    if (MapUtils.isEmpty(token.getClaims())) {
      return Collections.emptyList();
    }
    Map<String, Object> claims = token.getClaims();
    if (claims.get(TENANTS_CLAIM_KEY) == null) {
      return Collections.emptyList();
    }
    claims = (Map<String, Object>) claims.get(TENANTS_CLAIM_KEY);
    return new ArrayList<>(claims.keySet());
  }

  boolean isTenantAssociated(Token token, String tenant) {
    return getTenantIds(token).contains(tenant);
  }

  @SuppressWarnings("unchecked")
  List<String> getAuthorizationClaimItems(Token token, String tenant, String root) {
    if (MapUtils.isEmpty(token.getClaims())) {
      return Collections.emptyList();
    }
    Map<String, Object> claims = token.getClaims();
    if (StringUtils.isNotBlank(tenant)) {
      if (claims.get(TENANTS_CLAIM_KEY) == null) {
        return Collections.emptyList();
      }
      claims = (Map<String, Object>) claims.get(TENANTS_CLAIM_KEY);
      if (claims.get(tenant) == null) {
        return Collections.emptyList();
      }
      claims = (Map<String, Object>) claims.get(tenant);
    }
    List<String> res = (List<String>) claims.get(root);
    return res == null ? Collections.emptyList() : res;
  }

  private URI composeRefreshTokenLinkURL() {
    return getUri(REFRESH_TOKEN_LINK);
  }
}
