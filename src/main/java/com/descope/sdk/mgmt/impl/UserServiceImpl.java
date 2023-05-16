package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.CREATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_LINK;

import com.descope.exception.DescopeException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.UserResponse;
import com.descope.sdk.mgmt.UserService;
import java.net.URI;
import java.util.List;
import java.util.Objects;

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
    return null;
  }

  @Override
  public UserResponse invite(String loginId, UserRequest request) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse update(String loginId, UserRequest request) throws DescopeException {
    return null;
  }

  @Override
  public void delete(String loginId) throws DescopeException {

  }

  @Override
  public void deleteAllTestUsers() throws DescopeException {

  }

  @Override
  public UserResponse load(String loginId) throws DescopeException {
    return null;
  }

  @Override
  public UserResponse loadByUserId(String userId) throws DescopeException {
    return null;
  }

  @Override
  public List<UserResponse> searchAll(UserSearchRequest request) throws DescopeException {
    return null;
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

  private URI composeCreateUserUri() {
    return getUri(CREATE_USER_LINK);
  }

  private URI composeUpdateUserUri() {
    return getUri(UPDATE_USER_LINK);
  }

  private URI composeDeleteUserUri() {
    return getUri(DELETE_USER_LINK);
  }
}
