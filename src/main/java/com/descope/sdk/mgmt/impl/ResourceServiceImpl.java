package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATES_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATE_CREATE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATE_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATE_UPDATE;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.resource.DynamicRegistrationTemplate;
import com.descope.model.resource.DynamicRegistrationTemplateRequest;
import com.descope.model.resource.DynamicRegistrationTemplateResponse;
import com.descope.model.resource.LoadAllDynamicRegistrationTemplatesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ResourceService;
import org.apache.commons.lang3.StringUtils;

public class ResourceServiceImpl extends ManagementsBase implements ResourceService {

  ResourceServiceImpl(Client client) {
    super(client);
  }

  @Override
  public DynamicRegistrationTemplate createDynamicRegistrationTemplate(DynamicRegistrationTemplateRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }
    ApiProxy apiProxy = getApiProxy();
    DynamicRegistrationTemplateResponse res = apiProxy.post(
        getUri(MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATE_CREATE), request, DynamicRegistrationTemplateResponse.class);
    return res.getTemplate();
  }

  @Override
  public DynamicRegistrationTemplate updateDynamicRegistrationTemplate(DynamicRegistrationTemplate template)
      throws DescopeException {
    if (template == null) {
      throw ServerCommonException.invalidArgument("template");
    }
    if (StringUtils.isBlank(template.getId())) {
      throw ServerCommonException.invalidArgument("template.id");
    }
    if (StringUtils.isBlank(template.getName())) {
      throw ServerCommonException.invalidArgument("template.name");
    }
    ApiProxy apiProxy = getApiProxy();
    DynamicRegistrationTemplateResponse res = apiProxy.post(getUri(MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATE_UPDATE),
        mapOf("template", template), DynamicRegistrationTemplateResponse.class);
    return res.getTemplate();
  }

  @Override
  public DynamicRegistrationTemplate loadDynamicRegistrationTemplate(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    DynamicRegistrationTemplateResponse res = apiProxy.get(
        getQueryParamUri(MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATE_LOAD, mapOf("id", id)),
        DynamicRegistrationTemplateResponse.class);
    return res.getTemplate();
  }

  @Override
  public LoadAllDynamicRegistrationTemplatesResponse loadAllDynamicRegistrationTemplates() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(MANAGEMENT_DYNAMIC_REGISTRATION_TEMPLATES_LOAD),
        LoadAllDynamicRegistrationTemplatesResponse.class);
  }
}
