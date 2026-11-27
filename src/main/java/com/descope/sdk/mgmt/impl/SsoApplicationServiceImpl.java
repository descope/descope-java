package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_CREATE_CUSTOM_ATTRIBUTES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_CUSTOM_ATTRIBUTES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_DELETE_CUSTOM_ATTRIBUTES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_OIDC_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_OIDC_UPDATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_ROTATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SAML_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SAML_UPDATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SECRET_ADD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SECRET_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SECRET_REVOKE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_WSFED_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_WSFED_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttribute;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.model.mgmt.IDResponse;
import com.descope.model.ssoapp.OIDCApplicationRequest;
import com.descope.model.ssoapp.SAMLApplicationRequest;
import com.descope.model.ssoapp.SSOApplication;
import com.descope.model.ssoapp.SSOApplicationAddSecretResponse;
import com.descope.model.ssoapp.SSOApplicationCustomAttributesResponse;
import com.descope.model.ssoapp.SSOApplicationSecretMeta;
import com.descope.model.ssoapp.SSOApplicationSecretResponse;
import com.descope.model.ssoapp.SSOApplicationSecretsResponse;
import com.descope.model.ssoapp.SSOApplications;
import com.descope.model.ssoapp.WSFedApplicationRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.SsoApplicationService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class SsoApplicationServiceImpl extends ManagementsBase implements SsoApplicationService {
  SsoApplicationServiceImpl(Client client) {
    super(client);
  }

  @Override
  public String createOIDCApplication(OIDCApplicationRequest appRequest) throws DescopeException {
    if (appRequest == null) {
      throw ServerCommonException.invalidArgument("appRequest");
    }
    if (StringUtils.isBlank(appRequest.getName())) {
      throw ServerCommonException.invalidArgument("appRequest.Name");
    }
    ApiProxy apiProxy = getApiProxy();
    IDResponse id = apiProxy.post(getUri(SSO_APPLICATION_OIDC_CREATE_LINK), appRequest, IDResponse.class);
    return id.getId();
  }

  @Override
  public String createSAMLApplication(SAMLApplicationRequest appRequest) throws DescopeException {
    if (appRequest == null) {
      throw ServerCommonException.invalidArgument("appRequest");
    }
    if (StringUtils.isBlank(appRequest.getName())) {
      throw ServerCommonException.invalidArgument("appRequest.Name");
    }
    ApiProxy apiProxy = getApiProxy();
    IDResponse id = apiProxy.post(getUri(SSO_APPLICATION_SAML_CREATE_LINK), appRequest, IDResponse.class);
    return id.getId();
  }

  @Override
  public String createWSFedApplication(WSFedApplicationRequest appRequest) throws DescopeException {
    if (appRequest == null) {
      throw ServerCommonException.invalidArgument("appRequest");
    }
    if (StringUtils.isBlank(appRequest.getName())) {
      throw ServerCommonException.invalidArgument("appRequest.Name");
    }
    ApiProxy apiProxy = getApiProxy();
    IDResponse id = apiProxy.post(getUri(SSO_APPLICATION_WSFED_CREATE_LINK), appRequest, IDResponse.class);
    return id.getId();
  }

  @Override
  public void updateOIDCApplication(OIDCApplicationRequest appRequest) throws DescopeException {
    if (appRequest == null) {
      throw ServerCommonException.invalidArgument("appRequest");
    }
    if (StringUtils.isBlank(appRequest.getId())) {
      throw ServerCommonException.invalidArgument("appRequest.Id");
    }
    if (StringUtils.isBlank(appRequest.getName())) {
      throw ServerCommonException.invalidArgument("appRequest.Name");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_APPLICATION_OIDC_UPDATE_LINK), appRequest, Void.class);
  }

  @Override
  public void updateSAMLApplication(SAMLApplicationRequest appRequest) throws DescopeException {
    if (appRequest == null) {
      throw ServerCommonException.invalidArgument("appRequest");
    }
    if (StringUtils.isBlank(appRequest.getId())) {
      throw ServerCommonException.invalidArgument("appRequest.Id");
    }
    if (StringUtils.isBlank(appRequest.getName())) {
      throw ServerCommonException.invalidArgument("appRequest.Name");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_APPLICATION_SAML_UPDATE_LINK), appRequest, Void.class);
  }

  @Override
  public void updateWSFedApplication(WSFedApplicationRequest appRequest) throws DescopeException {
    if (appRequest == null) {
      throw ServerCommonException.invalidArgument("appRequest");
    }
    if (StringUtils.isBlank(appRequest.getId())) {
      throw ServerCommonException.invalidArgument("appRequest.Id");
    }
    if (StringUtils.isBlank(appRequest.getName())) {
      throw ServerCommonException.invalidArgument("appRequest.Name");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_APPLICATION_WSFED_UPDATE_LINK), appRequest, Void.class);
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(SSO_APPLICATION_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public SSOApplication load(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(SSO_APPLICATION_LOAD_LINK, mapOf("id", id)), SSOApplication.class);
  }

  @Override
  public List<SSOApplication> loadAll() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    SSOApplications apps = apiProxy.get(getUri(SSO_APPLICATION_LOAD_ALL_LINK), SSOApplications.class);
    return apps.getApps();
  }

  @Override
  public String getApplicationSecret(String id) throws DescopeException {
    return getApplicationSecret(id, null);
  }

  @Override
  public String getApplicationSecret(String id, String secretName) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, String> params = mapOf("id", id);
    if (StringUtils.isNotBlank(secretName)) {
      params.put("secretName", secretName);
    }
    ApiProxy apiProxy = getApiProxy();
    SSOApplicationSecretResponse res = apiProxy.get(getQueryParamUri(SSO_APPLICATION_SECRET_LINK, params),
        SSOApplicationSecretResponse.class);
    return res.getCleartext();
  }

  @Override
  public String rotateApplicationSecret(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    SSOApplicationSecretResponse res = apiProxy.post(getUri(SSO_APPLICATION_ROTATE_LINK), mapOf("id", id),
        SSOApplicationSecretResponse.class);
    return res.getCleartext();
  }

  @Override
  public SSOApplicationAddSecretResponse addApplicationSecret(String id, String name, Integer expireTime)
      throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    Map<String, Object> req = mapOf("appId", id, "name", name);
    if (expireTime != null) {
      req.put("expireTime", expireTime);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(SSO_APPLICATION_SECRET_ADD_LINK), req, SSOApplicationAddSecretResponse.class);
  }

  @Override
  public List<SSOApplicationSecretMeta> revokeApplicationSecret(String id, String name) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    SSOApplicationSecretsResponse res = apiProxy.post(getUri(SSO_APPLICATION_SECRET_REVOKE_LINK),
        mapOf("appId", id, "name", name), SSOApplicationSecretsResponse.class);
    return res.getSecrets();
  }

  @Override
  public SSOApplicationCustomAttributesResponse getCustomAttributes() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(SSO_APPLICATION_CUSTOM_ATTRIBUTES_LINK),
        SSOApplicationCustomAttributesResponse.class);
  }

  @Override
  public SSOApplicationCustomAttributesResponse createCustomAttributes(CreateCustomAttributesRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (request.getAttributes() == null || request.getAttributes().isEmpty()) {
      throw ServerCommonException.invalidArgument("attributes");
    }
    for (CustomAttribute ca : request.getAttributes()) {
      if (ca == null || StringUtils.isBlank(ca.getName())) {
        throw ServerCommonException.invalidArgument("attribute name");
      }
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(SSO_APPLICATION_CREATE_CUSTOM_ATTRIBUTES_LINK), request,
        SSOApplicationCustomAttributesResponse.class);
  }

  @Override
  public SSOApplicationCustomAttributesResponse deleteCustomAttributes(DeleteCustomAttributesRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (request.getNames() == null || request.getNames().isEmpty()) {
      throw ServerCommonException.invalidArgument("names");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(SSO_APPLICATION_DELETE_CUSTOM_ATTRIBUTES_LINK), request,
        SSOApplicationCustomAttributesResponse.class);
  }
}
