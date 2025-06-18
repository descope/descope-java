package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
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
}