package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppLoadAllOptions;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;
import com.descope.model.inbound.InboundAppV2;
import com.descope.model.inbound.InboundAppV2Request;
import com.descope.model.inbound.LoadAllApplicationsV2Response;

// Provides functions for managing inbound applications in a project.
public interface InboundAppsService {
  InboundAppCreateResponse createApplication(InboundAppRequest appRequest) throws DescopeException;

  void updateApplication(InboundAppRequest appRequest) throws DescopeException;

  void patchApplication(InboundAppRequest appRequest) throws DescopeException;

  void deleteApplication(String id) throws DescopeException;

  InboundApp loadApplication(String id) throws DescopeException;

  InboundApp loadApplicationByClientId(String id) throws DescopeException;

  String getApplicationSecret(String id) throws DescopeException;

  String rotateApplicationSecret(String id) throws DescopeException;

  InboundApp[] loadAllApplications() throws DescopeException;

  void deleteConsents(InboundAppConsentDeleteOptions options) throws DescopeException;

  void deleteTenantConsents(InboundAppTenantConsentDeleteOptions options) throws DescopeException;

  InboundAppConsentSearchResponse searchConsents(InboundAppConsentSearchOptions options) throws DescopeException;

  /**
   * Create an inbound application with the v2 API, which uses {@code scopeClaimMapping} instead of attributes
   * scopes.
   *
   * @param appRequest The application; {@code name} is required and {@code id} must not be set.
   * @return The application's ID, client ID and client secret.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  InboundAppCreateResponse createApplicationV2(InboundAppV2Request appRequest) throws DescopeException;

  /**
   * Replace an inbound application with the v2 API. Unset fields are reset, and the legacy attributes scopes are
   * cleared.
   *
   * @param appRequest The application; {@code id}, {@code name} and {@code loginPageUrl} are required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void updateApplicationV2(InboundAppV2Request appRequest) throws DescopeException;

  /**
   * Update only the set fields of an inbound application with the v2 API. The legacy attributes scopes are cleared.
   *
   * @param appRequest The fields to update; {@code id} is required.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void patchApplicationV2(InboundAppV2Request appRequest) throws DescopeException;

  /**
   * Load an inbound application by its ID with the v2 API.
   *
   * @param id The application ID.
   * @return The application.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  InboundAppV2 loadApplicationV2(String id) throws DescopeException;

  /**
   * Load an inbound application by its client ID with the v2 API.
   *
   * @param clientId The application's client ID.
   * @return The application.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  InboundAppV2 loadApplicationByClientIdV2(String clientId) throws DescopeException;

  /**
   * Load inbound applications, a page at a time, with the v2 API.
   *
   * @param options Paging and filters; {@code null} loads the first page of all applications.
   * @return The applications and their total count.
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  LoadAllApplicationsV2Response loadAllApplicationsV2(InboundAppLoadAllOptions options) throws DescopeException;
}