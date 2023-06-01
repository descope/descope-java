package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.SSO_CONFIGURE_MAPPING_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_CONFIGURE_METADATA_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_CONFIGURE_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_DELETE_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_GET_SETTINGS_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.sso.AttributeMapping;
import com.descope.model.sso.RoleMapping;
import com.descope.model.sso.SSOSettingsResponse;
import com.descope.sdk.mgmt.SsoService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class SsoServiceImpl extends ManagementsBase implements SsoService {
  SsoServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public SSOSettingsResponse getSettings(String tenantID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, String> params = Map.of("tenantId", tenantID);
    var apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(SSO_GET_SETTINGS_LINK, params), SSOSettingsResponse.class);
  }

  @Override
  public void deleteSettings(String tenantID) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, String> request = Map.of("tenantId", tenantID);
    var apiProxy = getApiProxy();
    apiProxy.delete(getUri(SSO_DELETE_SETTINGS_LINK), request, Void.class);
  }

  @Override
  public void configureSettings(
      String tenantID,
      String idpURL,
      String idpCert,
      String entityID,
      String redirectURL,
      String domain)
      throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantID");
    }
    if (StringUtils.isBlank(idpURL)) {
      throw ServerCommonException.invalidArgument("IdpURL");
    }
    if (StringUtils.isBlank(idpCert)) {
      throw ServerCommonException.invalidArgument("IdpCert");
    }
    if (StringUtils.isBlank(entityID)) {
      throw ServerCommonException.invalidArgument("EntityID");
    }
    if (StringUtils.isBlank(redirectURL)) {
      throw ServerCommonException.invalidArgument("RedirectURL");
    }
    Map<String, String> request =
        Map.of(
            "tenantId",
            tenantID,
            "idpURL",
            idpURL,
            "idpCert",
            idpCert,
            "entityId",
            entityID,
            "redirectURL",
            redirectURL,
            "domain",
            domain);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_CONFIGURE_SETTINGS_LINK), request, Void.class);
  }

  @Override
  public void configureMetadata(String tenantID, String idpMetadataURL) throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantID");
    }
    if (StringUtils.isBlank(idpMetadataURL)) {
      throw ServerCommonException.invalidArgument("IdpMetadataURL");
    }
    Map<String, String> request = Map.of("tenantId", tenantID, "idpMetadataURL", idpMetadataURL);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_CONFIGURE_METADATA_LINK), request, Void.class);
  }

  @Override
  public void configureMapping(
      String tenantID, List<RoleMapping> roleMapping, AttributeMapping attributeMapping)
      throws DescopeException {
    if (StringUtils.isBlank(tenantID)) {
      throw ServerCommonException.invalidArgument("TenantID");
    }
    Map<String, Object> request =
        Map.of(
            "tenantId",
            tenantID,
            "roleMappings",
            roleMapping,
            "attributeMapping",
            attributeMapping);
    var apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_CONFIGURE_MAPPING_LINK), request, Void.class);
  }
}
