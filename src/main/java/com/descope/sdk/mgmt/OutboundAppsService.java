package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.model.outbound.OutboundApp;
import com.descope.model.outbound.OutboundAppCreateResponse;
import com.descope.model.outbound.OutboundAppRequest;

// Provides functions for managing outbound applications and tokens in a project.
public interface OutboundAppsService {
  OutboundAppCreateResponse createApplication(OutboundAppRequest request) throws DescopeException;

  void updateApplication(OutboundAppRequest request) throws DescopeException;

  void deleteApplication(String id) throws DescopeException;

  OutboundApp loadApplication(String id) throws DescopeException;

  OutboundApp[] loadAllApplications() throws DescopeException;

  /**
   * Fetch the requested token (if exists) for the given user and outbound application.
   *
   * @param request The token details including the requested scopes
   * @return The requested token
   * @throws DescopeException in case of errors
   */
  FetchOutboundAppUserTokenResponse fetchOutboundAppUserToken(FetchOutboundAppUserTokenRequest request)
      throws DescopeException;

  /**
   * Fetch the latest token for the given user and outbound application.
   *
   * @param request Required appId, userId, optional tenantId and options
   * @return The latest token
   * @throws DescopeException in case of errors
   */
  FetchOutboundAppUserTokenResponse fetchLatestOutboundAppUserToken(
      FetchLatestOutboundAppUserTokenRequest request) throws DescopeException;

  /**
   * Fetch a tenant token by specific scopes for an outbound application.
   *
   * @param request Required appId, tenantId, and scopes
   * @return The requested token
   * @throws DescopeException in case of errors
   */
  FetchOutboundAppTenantTokenResponse fetchOutboundAppTenantTokenByScopes(
      FetchOutboundAppTenantTokenRequest request) throws DescopeException;

  /**
   * Fetch the latest tenant token for an outbound application.
   *
   * @param request Required appId and tenantId; scopes ignored
   * @return The latest token
   * @throws DescopeException in case of errors
   */
  FetchOutboundAppTenantTokenResponse fetchLatestOutboundAppTenantToken(
      FetchOutboundAppTenantTokenRequest request) throws DescopeException;

  /**
   * Delete the outbound application token for the given ID.
   *
   * @param id required token ID
   * @throws DescopeException in case of errors
   */
  void deleteOutboundAppTokenById(String id) throws DescopeException;

  /**
   * Delete all outbound application tokens for the given user.
   *
   * @param request required request containing user ID and app ID
   * @throws DescopeException in case of errors
   */
  void deleteOutboundAppUserTokens(DeleteOutboundAppUserTokensRequest request) throws DescopeException;
}