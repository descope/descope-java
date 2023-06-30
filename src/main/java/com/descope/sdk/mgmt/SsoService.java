package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.sso.AttributeMapping;
import com.descope.model.sso.RoleMapping;
import com.descope.model.sso.SSOSettingsResponse;
import java.util.List;

public interface SsoService {
  SSOSettingsResponse getSettings(String tenantID) throws DescopeException;

  void deleteSettings(String tenantID) throws DescopeException;

  void configureSettings(String tenantID, String idpURL, String idpCert, String entityID,
      String redirectURL, String domain) throws DescopeException;

  void configureMetadata(String tenantID, String idpMetadataURL) throws DescopeException;

  void configureMapping(String tenantID, List<RoleMapping> roleMapping,
      AttributeMapping attributeMapping) throws DescopeException;
}