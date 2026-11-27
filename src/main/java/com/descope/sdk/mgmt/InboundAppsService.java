package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppAddSecretResponse;
import com.descope.model.inbound.InboundAppConsent;
import com.descope.model.inbound.InboundAppConsentCreateRequest;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppLoadAllOptions;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppSecretMeta;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.LoadAllApplicationsResponse;
import java.util.List;

// Provides functions for managing inbound applications in a project.
public interface InboundAppsService {
  InboundAppCreateResponse createApplication(InboundAppRequest appRequest) throws DescopeException;

  void updateApplication(InboundAppRequest appRequest) throws DescopeException;

  void patchApplication(InboundAppRequest appRequest) throws DescopeException;

  void deleteApplication(String id) throws DescopeException;

  /**
   * Delete inbound applications.
   *
   * @param ids The IDs of the applications to delete.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void deleteApplications(List<String> ids) throws DescopeException;

  InboundApp loadApplication(String id) throws DescopeException;

  InboundApp loadApplicationByClientId(String id) throws DescopeException;

  String getApplicationSecret(String id) throws DescopeException;

  /**
   * Reveal a named secret of an inbound application.
   *
   * @param id The application ID.
   * @param secretName The secret's name, or empty for the application's secret.
   * @return The secret's cleartext value.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  String getApplicationSecret(String id, String secretName) throws DescopeException;

  /**
   * Add a named secret to an inbound application.
   *
   * @param id The application ID.
   * @param name The secret's name.
   * @param expireTime Expiration in epoch seconds; {@code null} or 0 means the secret never expires.
   * @return The secret's metadata and its cleartext value, which is returned only here and when revealed.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  InboundAppAddSecretResponse addApplicationSecret(String id, String name, Integer expireTime)
      throws DescopeException;

  /**
   * Revoke a named secret of an inbound application.
   *
   * @param id The application ID.
   * @param name The name of the secret to revoke.
   * @return The application's remaining secrets.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  InboundAppSecretMeta[] revokeApplicationSecret(String id, String name) throws DescopeException;

  String rotateApplicationSecret(String id) throws DescopeException;

  InboundApp[] loadAllApplications() throws DescopeException;

  /**
   * Load inbound applications, a page at a time.
   *
   * @param options Paging and filters; {@code null} loads the first page of all applications.
   * @return The applications and their total count.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  LoadAllApplicationsResponse loadAllApplications(InboundAppLoadAllOptions options) throws DescopeException;

  /**
   * Create a consent for a user to an inbound application.
   *
   * @param request The consent; {@code appId} and {@code userId} are required. Up to 1000 scopes.
   * @return The created consent.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  InboundAppConsent createConsent(InboundAppConsentCreateRequest request) throws DescopeException;

  void deleteConsents(InboundAppConsentDeleteOptions options) throws DescopeException;

  void deleteTenantConsents(InboundAppTenantConsentDeleteOptions options) throws DescopeException;

  InboundAppConsentSearchResponse searchConsents(InboundAppConsentSearchOptions options) throws DescopeException;
}