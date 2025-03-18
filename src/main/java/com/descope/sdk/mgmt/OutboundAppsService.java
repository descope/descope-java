package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;

/** Provides functions for managing outbound application tokens for a user in a project. */
public interface OutboundAppsService {

  /**
   * Fetch the requested token (if exists) for the given user and outbound application.
   *
   * @param request The token details including the requested scopes
   * @return The requested token
   * @throws DescopeException in case of errors
   */
  FetchOutboundAppUserTokenResponse fetchOutboundAppUserToken(FetchOutboundAppUserTokenRequest request) throws DescopeException;

  /**
   * Delete the outbound application token for the given ID.
   * @param id required token ID
   * @throws DescopeException in case of errors
   */
  void deleteOutboundAppTokenById(String id) throws DescopeException;

  /**
   * Delete all outbound application tokens for the given user.
   * @param request required request containing user ID and app ID
   * @throws DescopeException in case of errors
   */
  void deleteOutboundAppUserTokens(DeleteOutboundAppUserTokensRequest request) throws DescopeException;
}