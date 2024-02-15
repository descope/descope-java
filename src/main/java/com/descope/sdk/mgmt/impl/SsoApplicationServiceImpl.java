package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_OIDC_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_OIDC_UPDATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SAML_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.SSO_APPLICATION_SAML_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.IDResponse;
import com.descope.model.ssoapp.OIDCApplicationRequest;
import com.descope.model.ssoapp.SAMLApplicationRequest;
import com.descope.model.ssoapp.SSOApplication;
import com.descope.model.ssoapp.SSOApplications;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.SsoApplicationService;
import java.util.List;
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
}
