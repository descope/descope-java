package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_CREATE_USER_CUSTOM_ATTRIBUTES;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_DELETE_USER_CUSTOM_ATTRIBUTES;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttribute;
import com.descope.model.customattributes.CustomAttributesResponse;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.UserCustomAttributesService;

class UserCustomAttributesServiceImpl extends ManagementsBase implements UserCustomAttributesService {

  UserCustomAttributesServiceImpl(Client client) {
    super(client);
  }

  @Override
  public CustomAttributesResponse createCustomAttributes(CreateCustomAttributesRequest request)
          throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (request.getAttributes() == null || request.getAttributes().isEmpty()) {
      throw ServerCommonException.invalidArgument("attributes");
    }
    for (CustomAttribute ca : request.getAttributes()) {
      if (ca.getName().isEmpty()) {
        throw ServerCommonException.invalidArgument("attribute name");
      }
      if (ca.getType() <= 0 || ca.getType() > 6) {
        throw ServerCommonException.invalidArgument("attribute type");
      }
      if (ca.getType() != 4 && ca.getType() != 5 && (ca.getOptions() != null && !ca.getOptions().isEmpty())) {
        throw ServerCommonException.invalidArgument("attribute options");
      }
      if ((ca.getType() == 4 || ca.getType() == 5) && (ca.getOptions() == null || ca.getOptions().isEmpty())) {
        throw ServerCommonException.invalidArgument("attribute options");
      }
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_CREATE_USER_CUSTOM_ATTRIBUTES), request,
            CustomAttributesResponse.class);
  }

  @Override
  public CustomAttributesResponse deleteCustomAttributes(DeleteCustomAttributesRequest request)
          throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (request.getNames() == null || request.getNames().isEmpty()) {
      throw ServerCommonException.invalidArgument("names");
    }

    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_DELETE_USER_CUSTOM_ATTRIBUTES), request,
            CustomAttributesResponse.class);
  }

}
