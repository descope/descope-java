package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;

public interface OutboundAppsByTokenService {
  FetchOutboundAppUserTokenResponse fetchOutboundAppUserTokenByScopes(
      String token, FetchOutboundAppUserTokenRequest request) throws DescopeException;

  FetchOutboundAppUserTokenResponse fetchLatestOutboundAppUserToken(
      String token, FetchLatestOutboundAppUserTokenRequest request) throws DescopeException;

  FetchOutboundAppTenantTokenResponse fetchOutboundAppTenantTokenByScopes(
      String token, FetchOutboundAppTenantTokenRequest request) throws DescopeException;

  FetchOutboundAppTenantTokenResponse fetchLatestOutboundAppTenantToken(
      String token, FetchOutboundAppTenantTokenRequest request) throws DescopeException;
}
