package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.inbound.AgenticIdentityRevokeOptions;
import com.descope.model.inbound.AgenticIdentitySearchOptions;
import com.descope.model.inbound.AgenticIdentitySearchResponse;
import com.descope.model.inbound.InboundApp;
import com.descope.model.inbound.InboundAppConsentDeleteOptions;
import com.descope.model.inbound.InboundAppConsentSearchOptions;
import com.descope.model.inbound.InboundAppConsentSearchResponse;
import com.descope.model.inbound.InboundAppCreateResponse;
import com.descope.model.inbound.InboundAppRequest;
import com.descope.model.inbound.InboundAppTenantConsentDeleteOptions;

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
   * Search agentic identities (consents granted to agentic apps), filterable by client ID, resource ID,
   * consent ID or user ID.
   *
   * @param options the search filters, all optional
   * @return the matching agentic identities and their total count
   * @throws DescopeException in case of errors
   */
  AgenticIdentitySearchResponse searchAgenticIdentities(AgenticIdentitySearchOptions options)
      throws DescopeException;

  /**
   * Revoke agentic identities and their consents by client ID, consent ID, resource ID and/or user ID.
   *
   * @param options the identities to revoke
   * @return the number of revoked identities
   * @throws DescopeException in case of errors
   */
  int revokeAgenticIdentities(AgenticIdentityRevokeOptions options) throws DescopeException;
}