package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_DEPENDENT_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_DEPENDENT_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_IMPERSONATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_SEARCH_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_SETTINGS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_STOP_IMPERSONATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FAMILY_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.addIfNotBlank;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.family.Family;
import com.descope.model.family.FamilyDependent;
import com.descope.model.family.FamilySettings;
import com.descope.model.family.request.FamilyDependentRequest;
import com.descope.model.family.request.FamilySearchRequest;
import com.descope.model.family.response.FamilyDependentResponse;
import com.descope.model.family.response.FamilyResponse;
import com.descope.model.family.response.LoadAllFamiliesResponse;
import com.descope.model.jwt.response.UpdateJwtResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.FamilyService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

class FamilyServiceImpl extends ManagementsBase implements FamilyService {

  FamilyServiceImpl(Client client) {
    super(client);
  }

  @Override
  public Family create(String name, Map<String, Object> customAttributes, String photo, Boolean disabled)
      throws DescopeException {
    return createFamily(null, name, customAttributes, photo, disabled);
  }

  @Override
  public Family createWithId(String id, String name, Map<String, Object> customAttributes, String photo,
      Boolean disabled) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    return createFamily(id, name, customAttributes, photo, disabled);
  }

  private Family createFamily(String id, String name, Map<String, Object> customAttributes, String photo,
      Boolean disabled) {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    Map<String, Object> req = mapOf("name", name);
    addIfNotNull(req, "familyId", id);
    addIfNotNull(req, "customAttributes", customAttributes);
    addIfNotNull(req, "photo", photo);
    addIfNotNull(req, "disabled", disabled);
    ApiProxy apiProxy = getApiProxy();
    FamilyResponse response = apiProxy.post(getUri(MANAGEMENT_FAMILY_CREATE_LINK), req, FamilyResponse.class);
    return response.getFamily();
  }

  @Override
  public Family update(String id, String name, Map<String, Object> customAttributes, String photo,
      Boolean disabled) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, Object> req = mapOf("id", id);
    addIfNotNull(req, "name", name);
    addIfNotNull(req, "customAttributes", customAttributes);
    addIfNotNull(req, "photo", photo);
    addIfNotNull(req, "disabled", disabled);
    ApiProxy apiProxy = getApiProxy();
    FamilyResponse response = apiProxy.post(getUri(MANAGEMENT_FAMILY_UPDATE_LINK), req, FamilyResponse.class);
    return response.getFamily();
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_FAMILY_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public List<Family> searchAll(FamilySearchRequest request) throws DescopeException {
    if (request == null) {
      request = FamilySearchRequest.builder().build();
    }
    ApiProxy apiProxy = getApiProxy();
    LoadAllFamiliesResponse response =
        apiProxy.post(getUri(MANAGEMENT_FAMILY_SEARCH_LINK), request, LoadAllFamiliesResponse.class);
    return response.getFamilies();
  }

  @Override
  public FamilyDependent createDependent(String familyId, FamilyDependentRequest dependent)
      throws DescopeException {
    if (StringUtils.isBlank(familyId)) {
      throw ServerCommonException.invalidArgument("familyId");
    }
    Map<String, Object> req = mapOf("familyId", familyId);
    if (dependent != null) {
      addIfNotBlank(req, "loginId", dependent.getLoginId());
      addIfNotBlank(req, "name", dependent.getName());
      addIfNotBlank(req, "email", dependent.getEmail());
      addIfNotBlank(req, "phone", dependent.getPhone());
      addIfNotBlank(req, "givenName", dependent.getGivenName());
      addIfNotBlank(req, "middleName", dependent.getMiddleName());
      addIfNotBlank(req, "familyName", dependent.getFamilyName());
      addIfNotBlank(req, "picture", dependent.getPicture());
      addIfNotNull(req, "customAttributes", dependent.getCustomAttributes());
      if (dependent.getFamilyScopedAttributes() != null) {
        // API keys family scoped attributes by family ID
        req.put("familyScopedAttributes", mapOf(familyId, dependent.getFamilyScopedAttributes()));
      }
    }
    ApiProxy apiProxy = getApiProxy();
    FamilyDependentResponse response = apiProxy.post(getUri(MANAGEMENT_FAMILY_DEPENDENT_CREATE_LINK), req,
        FamilyDependentResponse.class);
    return response.getUser();
  }

  @Override
  public void deleteDependent(String userId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("userId");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_FAMILY_DEPENDENT_DELETE_LINK), mapOf("userId", userId), Void.class);
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
    UpdateJwtResponse response =
        apiProxy.post(getUri(MANAGEMENT_FAMILY_IMPERSONATE_LINK), req, UpdateJwtResponse.class);
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
        apiProxy.post(getUri(MANAGEMENT_FAMILY_STOP_IMPERSONATION_LINK), req, UpdateJwtResponse.class);
    return response.getJwt();
  }

  @Override
  public FamilySettings getSettings() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getUri(MANAGEMENT_FAMILY_SETTINGS_LINK), FamilySettings.class);
  }

  @Override
  public FamilySettings configureSettings(FamilySettings settings) throws DescopeException {
    if (settings == null) {
      throw ServerCommonException.invalidArgument("settings");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_FAMILY_SETTINGS_LINK), settings, FamilySettings.class);
  }
}
