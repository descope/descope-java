package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_PASSWORD_SETTINGS;
import static com.descope.utils.CollectionUtils.addIfNotBlank;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.passwordsettings.PasswordSettings;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.PasswordSettingsService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("checkstyle:OverloadMethodsDeclarationOrder")
class PasswordSettingsServiceImpl extends ManagementsBase implements PasswordSettingsService {

  PasswordSettingsServiceImpl(Client client) {
    super(client);
  }

  @Override
  public PasswordSettings getSettings() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(managementPasswordSettingsUri(null), PasswordSettings.class);
  }

  @Override
  public PasswordSettings getSettings(String tenantId) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("tenantId");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(managementPasswordSettingsUri(tenantId), PasswordSettings.class);
  }

  @Override
  public void configureSettings(PasswordSettings settings) throws DescopeException {
    configureSettings(null, settings, true);
  }

  @Override
  public void configureSettings(String id, PasswordSettings settings) throws DescopeException {
    configureSettings(id, settings, false);
  }

  private void configureSettings(String id, PasswordSettings settings, boolean ignoreEmptyId) throws DescopeException {
    if (!ignoreEmptyId && StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
    Map<String, Object> req = new HashMap<>();
    addIfNotBlank(req, "tenantId", id);
    addIfNotNull(req, "enabled", settings.getEnabled());
    addIfNotNull(req, "minLength", settings.getMinLength());
    addIfNotNull(req, "lowercase", settings.getLowercase());
    addIfNotNull(req, "uppercase", settings.getUppercase());
    addIfNotNull(req, "number", settings.getNumber());
    addIfNotNull(req, "nonAlphanumeric", settings.getNonAlphanumeric());
    addIfNotNull(req, "expiration", settings.getExpiration());
    addIfNotNull(req, "expirationWeeks", settings.getExpirationWeeks());
    addIfNotNull(req, "reuse", settings.getReuse());
    addIfNotNull(req, "reuseAmount", settings.getReuseAmount());
    addIfNotNull(req, "lock", settings.getLock());
    addIfNotNull(req, "lockAttempts", settings.getLockAttempts());
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(managementPasswordSettingsUri(null), req, Void.class);
  }

  private URI managementPasswordSettingsUri(String id) {
    if (StringUtils.isBlank(id)) {
      return getUri(MANAGEMENT_PASSWORD_SETTINGS);
    }
    return getQueryParamUri(MANAGEMENT_PASSWORD_SETTINGS, mapOf("tenantId", id));
  }
}
