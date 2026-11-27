package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_LIBRARY_APPLY_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_LIBRARY_LIST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_LIBRARY_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_LIST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_UPDATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_JWT_TEMPLATE_VALIDATE_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.jwttemplate.ApplyJwtTemplateFromLibraryRequest;
import com.descope.model.jwttemplate.JwtTemplate;
import com.descope.model.jwttemplate.JwtTemplateLibraryEntry;
import com.descope.model.jwttemplate.JwtTemplateLibraryEntryResponse;
import com.descope.model.jwttemplate.JwtTemplateLibraryResponse;
import com.descope.model.jwttemplate.JwtTemplateResponse;
import com.descope.model.jwttemplate.JwtTemplateValidationResponse;
import com.descope.model.jwttemplate.JwtTemplatesResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.JwtTemplateService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class JwtTemplateServiceImpl extends ManagementsBase implements JwtTemplateService {

  JwtTemplateServiceImpl(Client client) {
    super(client);
  }

  @Override
  public JwtTemplate create(JwtTemplate template) throws DescopeException {
    validateTemplate(template);
    ApiProxy apiProxy = getApiProxy();
    JwtTemplateResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_CREATE_LINK),
        mapOf("template", template), JwtTemplateResponse.class);
    return resp != null ? resp.getTemplate() : null;
  }

  @Override
  public JwtTemplate update(JwtTemplate template) throws DescopeException {
    validateTemplate(template);
    ApiProxy apiProxy = getApiProxy();
    JwtTemplateResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_UPDATE_LINK),
        mapOf("template", template), JwtTemplateResponse.class);
    return resp != null ? resp.getTemplate() : null;
  }

  @Override
  public JwtTemplate load(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    JwtTemplateResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_LOAD_LINK), mapOf("id", id),
        JwtTemplateResponse.class);
    return resp != null ? resp.getTemplate() : null;
  }

  @Override
  public List<JwtTemplate> list() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    JwtTemplatesResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_LIST_LINK), Collections.emptyMap(),
        JwtTemplatesResponse.class);
    return resp != null && resp.getTemplates() != null ? resp.getTemplates() : Collections.emptyList();
  }

  @Override
  public JwtTemplateValidationResponse validate(String id, JwtTemplate template) throws DescopeException {
    if (StringUtils.isBlank(id) && template == null) {
      throw ServerCommonException.invalidArgument("template");
    }
    Map<String, Object> request = new HashMap<>();
    if (StringUtils.isNotBlank(id)) {
      request.put("id", id);
    }
    if (template != null) {
      request.put("template", template);
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_VALIDATE_LINK), request, JwtTemplateValidationResponse.class);
  }

  @Override
  public List<JwtTemplateLibraryEntry> listLibrary() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    JwtTemplateLibraryResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_LIBRARY_LIST_LINK),
        Collections.emptyMap(), JwtTemplateLibraryResponse.class);
    return resp != null && resp.getEntries() != null ? resp.getEntries() : Collections.emptyList();
  }

  @Override
  public JwtTemplateLibraryEntry loadLibraryEntry(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    JwtTemplateLibraryEntryResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_LIBRARY_LOAD_LINK),
        mapOf("id", id), JwtTemplateLibraryEntryResponse.class);
    return resp != null ? resp.getEntry() : null;
  }

  @Override
  public JwtTemplate applyFromLibrary(ApplyJwtTemplateFromLibraryRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getLibraryEntryId())) {
      throw ServerCommonException.invalidArgument("libraryEntryId");
    }
    ApiProxy apiProxy = getApiProxy();
    JwtTemplateResponse resp = apiProxy.post(getUri(MANAGEMENT_JWT_TEMPLATE_LIBRARY_APPLY_LINK), request,
        JwtTemplateResponse.class);
    return resp != null ? resp.getTemplate() : null;
  }

  private static void validateTemplate(JwtTemplate template) {
    if (template == null) {
      throw ServerCommonException.invalidArgument("template");
    }
    if (StringUtils.isBlank(template.getName())) {
      throw ServerCommonException.invalidArgument("name");
    }
    if (StringUtils.isBlank(template.getType())) {
      throw ServerCommonException.invalidArgument("type");
    }
  }
}
