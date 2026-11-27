package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.outbound.CreateOutboundAppByTemplateRequest;
import com.descope.model.outbound.CreateOutboundSCIMConfigurationRequest;
import com.descope.model.outbound.DeleteOutboundAppUserTokensRequest;
import com.descope.model.outbound.FetchLatestOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenRequest;
import com.descope.model.outbound.FetchOutboundAppTenantTokenResponse;
import com.descope.model.outbound.FetchOutboundAppUserTokenRequest;
import com.descope.model.outbound.FetchOutboundAppUserTokenResponse;
import com.descope.model.outbound.OutboundApp;
import com.descope.model.outbound.OutboundAppCreateResponse;
import com.descope.model.outbound.OutboundAppRequest;
import com.descope.model.outbound.OutboundAppTenantTokenUploadItem;
import com.descope.model.outbound.OutboundAppTokenUploadFailure;
import com.descope.model.outbound.OutboundAppUserTokenUploadItem;
import com.descope.model.outbound.OutboundSCIMConfiguration;
import com.descope.model.outbound.UpdateOutboundSCIMConfigurationRequest;
import com.descope.model.outbound.UploadOutboundAppTenantAPIKeyRequest;
import com.descope.model.outbound.UploadOutboundAppTenantTokenRequest;
import com.descope.model.outbound.UploadOutboundAppUserAPIKeyRequest;
import com.descope.model.outbound.UploadOutboundAppUserTokenRequest;
import java.util.List;

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

  /**
   * Create an outbound application from an existing template.
   *
   * @param request Required templateId; optional id, clientId, clientSecret, tenantId and overrides
   * @return The created application
   * @throws DescopeException in case of errors
   */
  OutboundApp createApplicationByTemplate(CreateOutboundAppByTemplateRequest request) throws DescopeException;

  /**
   * List the IDs of the outbound applications for which the given user has a valid token.
   *
   * @param userId required user ID
   * @param tenantId optional tenant ID
   * @return The application IDs
   * @throws DescopeException in case of errors
   */
  List<String> listAppsWithUserToken(String userId, String tenantId) throws DescopeException;

  /**
   * Upload a static API key for a user on an apikey-type outbound application.
   *
   * @param request Required appId, userId and apiKey; optional tenantId and externalIdentifier
   * @throws DescopeException in case of errors
   */
  void uploadUserAPIKey(UploadOutboundAppUserAPIKeyRequest request) throws DescopeException;

  /**
   * Upload a static API key for a tenant on an apikey-type outbound application.
   *
   * @param request Required appId, tenantId and apiKey; optional externalIdentifier
   * @throws DescopeException in case of errors
   */
  void uploadTenantAPIKey(UploadOutboundAppTenantAPIKeyRequest request) throws DescopeException;

  /**
   * Upload (migrate) an existing OAuth token for a user on an OAuth-type outbound application.
   *
   * @param request Required appId and userId
   * @throws DescopeException in case of errors
   */
  void uploadUserToken(UploadOutboundAppUserTokenRequest request) throws DescopeException;

  /**
   * Upload (migrate) an existing OAuth token for a tenant on an OAuth-type outbound application.
   *
   * @param request Required appId and tenantId
   * @throws DescopeException in case of errors
   */
  void uploadTenantToken(UploadOutboundAppTenantTokenRequest request) throws DescopeException;

  /**
   * Batch upload (migrate) existing OAuth tokens for users. All-or-nothing: when failures are returned,
   * no token was saved.
   *
   * @param tokens Required, 1 to 500 items, each with appId and userId
   * @return The failed items, empty on success
   * @throws DescopeException in case of errors
   */
  List<OutboundAppTokenUploadFailure> batchUploadUserTokens(List<OutboundAppUserTokenUploadItem> tokens)
      throws DescopeException;

  /**
   * Batch upload (migrate) existing OAuth tokens for tenants. All-or-nothing: when failures are returned,
   * no token was saved.
   *
   * @param tokens Required, 1 to 500 items, each with appId and tenantId
   * @return The failed items, empty on success
   * @throws DescopeException in case of errors
   */
  List<OutboundAppTokenUploadFailure> batchUploadTenantTokens(List<OutboundAppTenantTokenUploadItem> tokens)
      throws DescopeException;

  /**
   * Create an outbound SCIM configuration for an outbound application.
   *
   * @param request Required appId and configuration
   * @return The created configuration
   * @throws DescopeException in case of errors
   */
  OutboundSCIMConfiguration createSCIMConfiguration(CreateOutboundSCIMConfigurationRequest request)
      throws DescopeException;

  /**
   * Update an outbound SCIM configuration.
   *
   * @param request Required appId; optional configuration and version
   * @return The updated configuration
   * @throws DescopeException in case of errors
   */
  OutboundSCIMConfiguration updateSCIMConfiguration(UpdateOutboundSCIMConfigurationRequest request)
      throws DescopeException;

  /**
   * Enable or disable an outbound SCIM configuration.
   *
   * @param appId required application ID
   * @param enabled whether the configuration is enabled
   * @return The updated configuration
   * @throws DescopeException in case of errors
   */
  OutboundSCIMConfiguration setSCIMEnabled(String appId, boolean enabled) throws DescopeException;

  /**
   * Load the outbound SCIM configuration of an outbound application.
   *
   * @param appId required application ID
   * @return The configuration
   * @throws DescopeException in case of errors
   */
  OutboundSCIMConfiguration loadSCIMConfiguration(String appId) throws DescopeException;
}