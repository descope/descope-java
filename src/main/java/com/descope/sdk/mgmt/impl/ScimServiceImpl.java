package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCIM_KEY_ACTIVATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCIM_KEY_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCIM_KEY_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCIM_KEY_REVOKE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SCIM_KEY_ROTATE_LINK;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.scim.ActivateScimAccessKeysRequest;
import com.descope.model.scim.ActivateScimAccessKeysResponse;
import com.descope.model.scim.CreateScimAccessKeyRequest;
import com.descope.model.scim.LoadScimAccessKeysResponse;
import com.descope.model.scim.RevokeScimAccessKeysRequest;
import com.descope.model.scim.RevokeScimAccessKeysResponse;
import com.descope.model.scim.RotateScimAccessKeyRequest;
import com.descope.model.scim.ScimAccessKeyResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ScimService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class ScimServiceImpl extends ManagementsBase implements ScimService {

  ScimServiceImpl(Client client) {
    super(client);
  }

  @Override
  public LoadScimAccessKeysResponse loadKeys(String tenantId, String ssoId) throws DescopeException {
    if (StringUtils.isBlank(tenantId)) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    Map<String, String> params = new HashMap<>();
    params.put("tenantId", tenantId);
    if (ssoId != null) {
      params.put("ssoId", ssoId);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_SCIM_KEY_LOAD_LINK, params), LoadScimAccessKeysResponse.class);
  }

  @Override
  public ScimAccessKeyResponse createKey(CreateScimAccessKeyRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_SCIM_KEY_CREATE_LINK), request, ScimAccessKeyResponse.class);
  }

  @Override
  public ScimAccessKeyResponse rotateKey(RotateScimAccessKeyRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_SCIM_KEY_ROTATE_LINK), request, ScimAccessKeyResponse.class);
  }

  @Override
  public RevokeScimAccessKeysResponse revokeKeys(RevokeScimAccessKeysRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_SCIM_KEY_REVOKE_LINK), request, RevokeScimAccessKeysResponse.class);
  }

  @Override
  public ActivateScimAccessKeysResponse activateKeys(ActivateScimAccessKeysRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (StringUtils.isBlank(request.getTenantId())) {
      throw ServerCommonException.invalidArgument("TenantId");
    }
    if (request.getKeyIds() == null || request.getKeyIds().isEmpty()) {
      throw ServerCommonException.invalidArgument("KeyIds");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_SCIM_KEY_ACTIVATE_LINK), request, ActivateScimAccessKeysResponse.class);
  }
}
