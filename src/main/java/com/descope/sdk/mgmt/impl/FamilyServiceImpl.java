package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_CREATE_CUSTOM_ATTRIBUTES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_DELETE_CUSTOM_ATTRIBUTES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_DEPENDENT_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_DEPENDENT_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_GET_CUSTOM_ATTRIBUTES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_IMPERSONATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_SEARCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_STOP_IMPERSONATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.FAMILY_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.addIfNotBlank;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttribute;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;
import com.descope.model.family.Family;
import com.descope.model.family.FamilySettings;
import com.descope.model.family.request.FamilyDependentRequest;
import com.descope.model.family.request.FamilyRequest;
import com.descope.model.family.request.FamilySearchRequest;
import com.descope.model.family.response.FamiliesResponse;
import com.descope.model.family.response.FamilyCustomAttributesResponse;
import com.descope.model.family.response.FamilyResponse;
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.model.user.response.UserResponse;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.FamilyService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class FamilyServiceImpl extends ManagementsBase implements FamilyService {

  FamilyServiceImpl(Client client) {
    super(client);
  }

  @Override
  public Family create(FamilyRequest request) throws DescopeException {
    return createFamily(null, request);
  }

  @Override
  public Family createWithId(String id, FamilyRequest request) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    return createFamily(id, request);
  }

  private Family createFamily(String id, FamilyRequest request) {
    if (request == null || StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("name");
    }
    Map<String, Object> req = familyBody(request);
    addIfNotNull(req, "familyId", id);
    ApiProxy apiProxy = getApiProxy();
    FamilyResponse response = apiProxy.post(getUri(FAMILY_CREATE_LINK), req, FamilyResponse.class);
    return response.getFamily();
  }

  @Override
  public Family update(String id, FamilyRequest request) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, Object> req = familyBody(request != null ? request : new FamilyRequest());
    req.put("id", id);
    ApiProxy apiProxy = getApiProxy();
    FamilyResponse response = apiProxy.post(getUri(FAMILY_UPDATE_LINK), req, FamilyResponse.class);
    return response.getFamily();
  }

  private static Map<String, Object> familyBody(FamilyRequest request) {
    Map<String, Object> req = new HashMap<>();
    addIfNotNull(req, "name", request.getName());
    addIfNotNull(req, "customAttributes", request.getCustomAttributes());
    addIfNotNull(req, "photo", request.getPhoto());
    addIfNotNull(req, "disabled", request.getDisabled());
    return req;
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(FAMILY_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public List<Family> searchAll(FamilySearchRequest request) throws DescopeException {
    if (request == null) {
      request = FamilySearchRequest.builder().build();
    }
    ApiProxy apiProxy = getApiProxy();
    FamiliesResponse response = apiProxy.post(getUri(FAMILY_SEARCH_LINK), request, FamiliesResponse.class);
    return response.getFamilies();
  }

  @Override
  public UserResponse createDependent(String familyId, FamilyDependentRequest dependent) throws DescopeException {
    if (StringUtils.isBlank(familyId)) {
      throw ServerCommonException.invalidArgument("familyId");
    }
    Map<String, Object> req = mapOf("familyId", familyId);
    if (dependent != null) {
      addIfNotNull(req, "loginId", dependent.getLoginId());
      addIfNotNull(req, "name", dependent.getName());
      addIfNotNull(req, "email", dependent.getEmail());
      addIfNotNull(req, "phone", dependent.getPhone());
      addIfNotNull(req, "givenName", dependent.getGivenName());
      addIfNotNull(req, "middleName", dependent.getMiddleName());
      addIfNotNull(req, "familyName", dependent.getFamilyName());
      addIfNotNull(req, "picture", dependent.getPicture());
      addIfNotNull(req, "customAttributes", dependent.getCustomAttributes());
      addIfNotNull(req, "familyScopedAttributes", dependent.getFamilyScopedAttributes());
    }
    ApiProxy apiProxy = getApiProxy();
    UserResponseDetails response =
        apiProxy.post(getUri(FAMILY_DEPENDENT_CREATE_LINK), req, UserResponseDetails.class);
    return response.getUser();
  }

  @Override
  public void deleteDependent(String userId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("userId");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(FAMILY_DEPENDENT_DELETE_LINK), mapOf("userId", userId), Void.class);
  }

  @Override
  public String impersonateDependent(String impersonatorUserIdOrLoginId, String dependentLoginId,
      String selectedFamily) throws DescopeException {
    if (StringUtils.isBlank(impersonatorUserIdOrLoginId)) {
      throw ServerCommonException.invalidArgument("impersonatorUserIdOrLoginId");
    }
    if (StringUtils.isBlank(dependentLoginId)) {
      throw ServerCommonException.invalidArgument("dependentLoginId");
    }
    Map<String, Object> req = mapOf(
        "impersonatorUserIdOrLoginId", impersonatorUserIdOrLoginId,
        "dependentLoginId", dependentLoginId);
    addIfNotBlank(req, "selectedFamily", selectedFamily);
    ApiProxy apiProxy = getApiProxy();
    UpdateJwtResponse response = apiProxy.post(getUri(FAMILY_IMPERSONATE_LINK), req, UpdateJwtResponse.class);
    return response.getJwt();
  }

  @Override
  public String stopImpersonation(String jwt, Map<String, Object> customClaims, Integer refreshDuration)
      throws DescopeException {
    if (StringUtils.isBlank(jwt)) {
      throw ServerCommonException.invalidArgument("jwt");
    }
    Map<String, Object> req = mapOf("jwt", jwt);
    addIfNotNull(req, "customClaims", customClaims);
    addIfNotNull(req, "refreshDuration", refreshDuration);
    ApiProxy apiProxy = getApiProxy();
    UpdateJwtResponse response =
        apiProxy.post(getUri(FAMILY_STOP_IMPERSONATION_LINK), req, UpdateJwtResponse.class);
    return response.getJwt();
  }

  @Override
  public FamilySettings getSettings() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(FAMILY_SETTINGS_LINK), FamilySettings.class);
  }

  @Override
  public FamilySettings configureSettings(FamilySettings settings) throws DescopeException {
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FAMILY_SETTINGS_LINK), settings, FamilySettings.class);
  }

  @Override
  public FamilyCustomAttributesResponse getCustomAttributes() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(FAMILY_GET_CUSTOM_ATTRIBUTES_LINK), FamilyCustomAttributesResponse.class);
  }

  @Override
  public FamilyCustomAttributesResponse createCustomAttributes(CreateCustomAttributesRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (request.getAttributes() != null) {
      for (CustomAttribute ca : request.getAttributes()) {
        if (ca == null || StringUtils.isBlank(ca.getName())) {
          throw ServerCommonException.invalidArgument("attribute name");
        }
      }
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FAMILY_CREATE_CUSTOM_ATTRIBUTES_LINK), request, FamilyCustomAttributesResponse.class);
  }

  @Override
  public FamilyCustomAttributesResponse deleteCustomAttributes(DeleteCustomAttributesRequest request)
      throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("Request");
    }
    if (request.getNames() != null) {
      for (String name : request.getNames()) {
        if (StringUtils.isBlank(name)) {
          throw ServerCommonException.invalidArgument("names");
        }
      }
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(FAMILY_DELETE_CUSTOM_ATTRIBUTES_LINK), request, FamilyCustomAttributesResponse.class);
  }
}
