package com.descope.sdk.mgmt.impl;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.UserResponse;
import com.descope.sdk.mgmt.UserService;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_ALL_TEST_USERS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SEARCH_ALL;

class UserServiceImpl extends ManagementsBase implements UserService {

  UserServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public UserResponse create(String loginId, UserRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }

    request.setInvite(false);
    request.setTest(false);

    URI createUserUri = composeCreateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, request, UserResponse.class);
  }

  @Override
  public UserResponse createTestUser(String loginId, UserRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }

    request.setInvite(false);
    request.setTest(true);

    URI createUserUri = composeCreateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, request, UserResponse.class);
  }

  @Override
  public UserResponse invite(String loginId, UserRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }

    request.setInvite(true);
    request.setTest(false);

    URI createUserUri = composeCreateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, request, UserResponse.class);
  }

  @Override
  public UserResponse update(String loginId, UserRequest request) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }
    URI updateUserUri = composeUpdateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(updateUserUri, request, UserResponse.class);
  }

  @Override
  public void delete(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI deleteUserUri = composeDeleteUserUri();
    var apiProxy = getApiProxy();
    apiProxy.post(deleteUserUri, UserRequest.builder().loginId(loginId).build(), Void.class);
  }

  @Override
  public void deleteAllTestUsers() throws DescopeException {
    URI deleteAllTestUsersUri = composeDeleteAllTestUsersUri();
    var apiProxy = getApiProxy();
    apiProxy.post(deleteAllTestUsersUri, null, Void.class);
  }

  @Override
  public UserResponse load(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI loadUserUri = composeLoadUserUri(Map.of("loginId", loginId));
    var apiProxy = getApiProxy();
    return apiProxy.get(loadUserUri, UserResponse.class);
  }

  @Override
  public UserResponse loadByUserId(String userId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("User ID");
    }
    URI loadUserUri = composeLoadUserUri(Map.of("userId", userId));
    var apiProxy = getApiProxy();
    return apiProxy.get(loadUserUri, UserResponse.class);
  }

  @Override
  public List<UserResponse> searchAll(UserSearchRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = UserSearchRequest.builder().limit(0).page(0).build();
    }
    if (Objects.isNull(request.getLimit()) || request.getLimit() < 0) {
      throw ServerCommonException.invalidArgument("limit");
    }
    if (Objects.isNull(request.getPage()) || request.getPage() < 0) {
      throw ServerCommonException.invalidArgument("page");
    }

    URI composeSearchAllUri = composeSearchAllUri();
    var apiProxy = getApiProxy();
    return (List<UserResponse>) apiProxy.post(composeSearchAllUri, request, List.class);
  }


  @Override
  public UserResponse activate(String loginId) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse deactivate(String loginId) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse updateEmail(String loginId, String email, Boolean isVerified)
      throws DescopeException {
    return null;
  }

  @Override
  public UserResponse updatePhone(String loginId, String phone, Boolean isVerified)
      throws DescopeException {
    return null;
  }

  @Override
  public UserResponse updateDisplayName(String loginId, String displayName)
      throws DescopeException {
    return null;
  }

  @Override
  public UserResponse updatePicture(String loginId, String picture) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse updateCustomAttributes(String loginId, String key, String value)
      throws DescopeException {
    return null;
  }

  @Override
  public UserResponse addRoles(String loginId, List<String> roles) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse removeRoles(String loginId, List<String> roles) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse addTenant(String loginId, String tenantId) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse removeTenant(String loginId, String tenantId) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse addTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    return null;
  }

  @Override
  public UserResponse removeTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    return null;
  }

  @Override
  public void setPassword(String loginId, String password) throws DescopeException {

  }

  @Override
  public void expirePassword(String loginId) throws DescopeException {

  }

  @Override
  public String generateOtpForTestUser(String loginId, DeliveryMethod deliveryMethod) throws DescopeException {
    return null;
  }

  @Override
  public String generateMagicLinkForTestUser(String loginId, URI uri, DeliveryMethod deliveryMethod) throws DescopeException {
    return null;
  }

  @Override
  public String generateEnchantedLinkForTestUser(String loginId, URI uri) throws DescopeException {
    return null;
  }

  private URI composeCreateUserUri() {
    return getUri(CREATE_USER_LINK);
  }

  private URI composeUpdateUserUri() {
    return getUri(UPDATE_USER_LINK);
  }

  private URI composeDeleteUserUri() {
    return getUri(DELETE_USER_LINK);
  }

  private URI composeDeleteAllTestUsersUri() {
    return getUri(DELETE_ALL_TEST_USERS_LINK);
  }

  private URI composeLoadUserUri(Map<String, String> params) {
    return getQueryParamUri(LOAD_USER_LINK, params);
  }

  private URI composeSearchAllUri() {
    return getUri(USER_SEARCH_ALL);
  }

}
