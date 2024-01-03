package com.descope.sdk.auth.impl;

import static com.descope.literals.AppConstants.PERMISSIONS_CLAIM_KEY;
import static com.descope.literals.AppConstants.ROLES_CLAIM_KEY;
import static com.descope.literals.Routes.AuthEndPoints.EXCHANGE_ACCESS_KEY_LINK;
import static com.descope.literals.Routes.AuthEndPoints.LOG_OUT_ALL_LINK;
import static com.descope.literals.Routes.AuthEndPoints.LOG_OUT_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.ExchangeTokenRequest;
import com.descope.model.client.Client;
import com.descope.model.jwt.Token;
import com.descope.model.jwt.response.JWTResponse;
import com.descope.proxy.ApiProxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

class AuthenticationServiceImpl extends AuthenticationsBase {

  AuthenticationServiceImpl(Client client) {
    super(client);
  }

  @Override
  public Token validateSessionWithToken(String sessionToken) throws DescopeException {
    if (StringUtils.isBlank(sessionToken)) {
      throw ServerCommonException.invalidArgument("sessionToken");
    }
    return validateJWT(sessionToken);
  }

  @Override
  public Token refreshSessionWithToken(String refreshToken) throws DescopeException {
    if (Strings.isEmpty(refreshToken)) {
      throw ServerCommonException.missingArguments("Request doesn't contain refresh token");
    }

    return refreshSession(refreshToken);
  }

  @Override
  public Token validateAndRefreshSessionWithTokens(String sessionToken, String refreshToken)
      throws DescopeException {
    if (StringUtils.isAllBlank(sessionToken, refreshToken)) {
      throw ServerCommonException.missingArguments("Both sessionToken and refreshToken are empty");
    } else if (StringUtils.isNotBlank(sessionToken)) {
      try {
        return validateSessionWithToken(sessionToken);
      } catch (Exception e) {
        if (StringUtils.isNotBlank(refreshToken)) {
          return refreshSessionWithToken(refreshToken);
        }
        throw e;
      }
    } else {
      return refreshSessionWithToken(refreshToken);
    }
  }

  @Override
  public Token exchangeAccessKey(String accessKey) throws DescopeException {
    ApiProxy apiProxy = getApiProxy(accessKey);
    URI exchangeAccessKeyLinkURL = composeExchangeAccessKeyLinkURL();

    JWTResponse jwtResponse = apiProxy.post(exchangeAccessKeyLinkURL, null, JWTResponse.class);
    AuthenticationInfo authenticationInfo = getAuthenticationInfo(jwtResponse);
    return authenticationInfo.getToken();
  }

  @Override
  public boolean validatePermissions(Token token, List<String> permissions)
      throws DescopeException {
    return validatePermissions(token, "", permissions);
  }

  @Override
  public boolean validatePermissions(Token token, String tenant, List<String> permissions)
      throws DescopeException {
    if (StringUtils.isNotBlank(tenant) && !isTenantAssociated(token, tenant)) {
      return false;
    }
    List<String> grantedPermissions = getPermissions(token, tenant);
    return CollectionUtils.isSubCollection(permissions, grantedPermissions);
  }

  @Override
  public List<String> getMatchedPermissions(Token token, List<String> permissions) throws DescopeException {
    return getMatchedPermissions(token, "", permissions);
  }

  @Override
  public List<String> getMatchedPermissions(Token token, String tenant, List<String> permissions)
      throws DescopeException {
    if (CollectionUtils.isEmpty(permissions) || StringUtils.isNotBlank(tenant) && !isTenantAssociated(token, tenant)) {
      return Collections.emptyList();
    }
    List<String> grantedPermissions = getPermissions(token, tenant);
    Collection<String> intersection = CollectionUtils.intersection(permissions, grantedPermissions);
    return new ArrayList<>(intersection);
  }

  @Override
  public boolean validateRoles(Token token, List<String> roles) throws DescopeException {
    return validateRoles(token, "", roles);
  }

  @Override
  public boolean validateRoles(Token token, String tenant, List<String> roles)
      throws DescopeException {
    if (StringUtils.isNotBlank(tenant) && !isTenantAssociated(token, tenant)) {
      return false;
    }
    List<String> grantedRoles = getRoles(token, tenant);
    return CollectionUtils.isSubCollection(roles, grantedRoles);
  }

  @Override
  public List<String> getMatchedRoles(Token token, List<String> roles) throws DescopeException {
    return getMatchedRoles(token, "", roles);
  }

  @Override
  public List<String> getMatchedRoles(Token token, String tenant, List<String> roles) throws DescopeException {
    if (CollectionUtils.isEmpty(roles) || StringUtils.isNotBlank(tenant) && !isTenantAssociated(token, tenant)) {
      return Collections.emptyList();
    }
    List<String> grantedRoles = getRoles(token, tenant);
    Collection<String> intersection = CollectionUtils.intersection(roles, grantedRoles);
    return new ArrayList<>(intersection);
  }

  @Override
  public List<String> getRoles(Token token, String tenant) throws DescopeException {
    return getAuthorizationClaimItems(token, tenant, ROLES_CLAIM_KEY);
  }

  @Override
  public List<String> getRoles(Token token) throws DescopeException {
    return getAuthorizationClaimItems(token, "", ROLES_CLAIM_KEY);
  }

  @Override
  public List<String> getPermissions(Token token, String tenant) throws DescopeException {
    return getAuthorizationClaimItems(token, tenant, PERMISSIONS_CLAIM_KEY);
  }

  @Override
  public List<String> getPermissions(Token token) throws DescopeException {
    return getAuthorizationClaimItems(token, "", PERMISSIONS_CLAIM_KEY);
  }

  @Override
  public void logout(String refreshToken) throws DescopeException {
    if (Strings.isEmpty(refreshToken)) {
      throw ServerCommonException.missingArguments("Request doesn't contain refresh token");
    }
    ApiProxy apiProxy = getApiProxy(refreshToken);
    URI logOutURL = composeLogOutLinkURL();
    apiProxy.post(logOutURL, null, JWTResponse.class);
  }

  @Override
  public void logoutAll(String refreshToken) throws DescopeException {
    if (Strings.isEmpty(refreshToken)) {
      throw ServerCommonException.missingArguments("Request doesn't contain refresh token");
    }
    ApiProxy apiProxy = getApiProxy(refreshToken);
    URI logOutAllURL = composeLogOutAllLinkURL();
    apiProxy.post(logOutAllURL, null, JWTResponse.class);
  }

  AuthenticationInfo exchangeToken(String code, URI url) {
    if (StringUtils.isBlank(code)) {
      throw ServerCommonException.invalidArgument("Code");
    }
    ExchangeTokenRequest request = new ExchangeTokenRequest(code);
    ApiProxy apiProxy = getApiProxy();
    JWTResponse jwtResponse = apiProxy.post(url, request, JWTResponse.class);
    return getAuthenticationInfo(jwtResponse);
  }

  private URI composeExchangeAccessKeyLinkURL() {
    return getUri(EXCHANGE_ACCESS_KEY_LINK);
  }

  private URI composeLogOutLinkURL() {
    return getUri(LOG_OUT_LINK);
  }

  private URI composeLogOutAllLinkURL() {
    return getUri(LOG_OUT_ALL_LINK);
  }
}
