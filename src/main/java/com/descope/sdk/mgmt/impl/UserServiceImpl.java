package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.COMPOSE_OTP_FOR_TEST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.CREATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_ALL_TEST_USERS_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.DELETE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.ENCHANTED_LINK_FOR_TEST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.LOAD_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MAGIC_LINK_FOR_TEST_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_CUSTOM_ATTRIBUTE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_PICTURE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.UPDATE_USER_NAME_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_ADD_ROLES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_ADD_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_EXPIRE_PASSWORD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_REMOVE_ROLES_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_REMOVE_TENANT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SEARCH_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_SET_PASSWORD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_UPDATE_EMAIL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_UPDATE_PHONE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.USER_UPDATE_STATUS_LINK;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementParams;
import com.descope.model.user.request.EnchantedLinkTestUserRequest;
import com.descope.model.user.request.MagicLinkTestUserRequest;
import com.descope.model.user.request.OTPTestUserRequest;
import com.descope.model.user.request.TestUserRequest;
import com.descope.model.user.request.UserRequest;
import com.descope.model.user.request.UserSearchRequest;
import com.descope.model.user.response.AllUsersResponseDetails;
import com.descope.model.user.response.UserResponseDetails;
import com.descope.sdk.mgmt.UserService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

class UserServiceImpl extends ManagementsBase implements UserService {

  UserServiceImpl(Client client, ManagementParams managementParams) {
    super(client, managementParams);
  }

  @Override
  public UserResponseDetails create(String loginId, UserRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }

    request.setInvite(false);
    request.setTest(false);

    URI createUserUri = composeCreateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails createTestUser(String loginId, UserRequest request)
      throws DescopeException {
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }

    request.setInvite(false);
    request.setTest(true);

    URI createUserUri = composeCreateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails invite(String loginId, UserRequest request) throws DescopeException {
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }

    request.setInvite(true);
    request.setTest(false);

    URI createUserUri = composeCreateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(createUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails update(String loginId, UserRequest request) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (Objects.isNull(request)) {
      request = new UserRequest();
    }
    URI updateUserUri = composeUpdateUserUri();
    var apiProxy = getApiProxy();
    return apiProxy.post(updateUserUri, request, UserResponseDetails.class);
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
    apiProxy.delete(deleteAllTestUsersUri, null, Void.class);
  }

  @Override
  public UserResponseDetails load(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI loadUserUri = composeLoadUserUri(Map.of("loginId", loginId));
    var apiProxy = getApiProxy();
    return apiProxy.get(loadUserUri, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails loadByUserId(String userId) throws DescopeException {
    if (StringUtils.isBlank(userId)) {
      throw ServerCommonException.invalidArgument("User ID");
    }
    URI loadUserUri = composeLoadUserUri(Map.of("userId", userId));
    var apiProxy = getApiProxy();
    return apiProxy.get(loadUserUri, UserResponseDetails.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public AllUsersResponseDetails searchAll(UserSearchRequest request)
      throws DescopeException {
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
    return apiProxy.post(composeSearchAllUri, request, AllUsersResponseDetails.class);
  }

  @Override
  public UserResponseDetails activate(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI activateUserUri = composeActivateUserUri();
    Map<String, String> request = Map.of("loginId", loginId, "status", "enabled");
    var apiProxy = getApiProxy();
    return apiProxy.post(activateUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails deactivate(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI activateUserUri = composeActivateUserUri();
    Map<String, String> request = Map.of("loginId", loginId, "status", "disabled");
    var apiProxy = getApiProxy();
    return apiProxy.post(activateUserUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateEmail(String loginId, String email, Boolean isVerified)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updateEmailUri = composeUpdateEmailUri();
    Map<String, Object> request =
        Map.of("loginId", loginId, "email", email, "verified", isVerified);
    var apiProxy = getApiProxy();
    return apiProxy.post(updateEmailUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updatePhone(String loginId, String phone, Boolean isVerified)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updatePhoneUri = composeUpdatePhoneUri();
    Map<String, Object> request =
        Map.of("loginId", loginId, "phone", phone, "verified", isVerified);
    var apiProxy = getApiProxy();
    return apiProxy.post(updatePhoneUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateDisplayName(String loginId, String displayName)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updateUserNameUri = composeUpdateUserNameUri();
    Map<String, Object> request = Map.of("loginId", loginId, "displayName", displayName);
    var apiProxy = getApiProxy();
    return apiProxy.post(updateUserNameUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updatePicture(String loginId, String picture) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI updatePictureUri = composeUpdatePictureUri();
    Map<String, Object> request = Map.of("loginId", loginId, "picture", picture);
    var apiProxy = getApiProxy();
    return apiProxy.post(updatePictureUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails updateCustomAttributes(String loginId, String key, Object value)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(key)) {
      throw ServerCommonException.invalidArgument("Key");
    }
    URI updateAttributesUri = composeUpdateAttributesUri();
    Map<String, Object> request =
        Map.of("loginId", loginId, "attributeKey", key, "attributeValue", value);
    var apiProxy = getApiProxy();
    return apiProxy.post(updateAttributesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addRoles(String loginId, List<String> roles) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addRolesUri = composeAddRolesUri();
    Map<String, Object> request = Map.of("loginId", loginId, "tenantId", "", "roleNames", roles);
    var apiProxy = getApiProxy();
    return apiProxy.post(addRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeRoles(String loginId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI removeRolesUri = composeRemoveRolesUri();
    Map<String, Object> request = Map.of("loginId", loginId, "tenantId", "", "roleNames", roles);
    var apiProxy = getApiProxy();
    return apiProxy.post(removeRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addTenant(String loginId, String tenantId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addTenantUri = composeAddTenantUri();
    Map<String, Object> request = Map.of("loginId", loginId, "tenantId", tenantId);
    var apiProxy = getApiProxy();
    return apiProxy.post(addTenantUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeTenant(String loginId, String tenantId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI removeTenantUri = composeRemoveTenantUri();
    Map<String, Object> request = Map.of("loginId", loginId, "tenantId", tenantId);
    var apiProxy = getApiProxy();
    return apiProxy.post(removeTenantUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails addTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI addTenantRolesUri = composeAddTenantRolesUri();
    Map<String, Object> request = Map.of("loginId", loginId, "tenantId", "", "roleNames", roles);
    var apiProxy = getApiProxy();
    return apiProxy.post(addTenantRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public UserResponseDetails removeTenantRoles(String loginId, String tenantId, List<String> roles)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI removeTenantRolesUri = composeRemoveTenantRolesUri();
    Map<String, Object> request = Map.of("loginId", loginId, "tenantId", "", "roleNames", roles);
    var apiProxy = getApiProxy();
    return apiProxy.post(removeTenantRolesUri, request, UserResponseDetails.class);
  }

  @Override
  public void setPassword(String loginId, String password) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    if (StringUtils.isBlank(password)) {
      throw ServerCommonException.invalidArgument("Password");
    }
    URI setPasswordUri = composeSetPasswordUri();
    Map<String, Object> request = Map.of("loginId", loginId, "password", password);
    var apiProxy = getApiProxy();
    apiProxy.post(setPasswordUri, request, Void.class);
  }

  @Override
  public void expirePassword(String loginId) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI expirePasswordUri = composeExpirePasswordUri();
    Map<String, Object> request = Map.of("loginId", loginId);
    var apiProxy = getApiProxy();
    apiProxy.post(expirePasswordUri, request, Void.class);
  }

  @Override
  public String generateOtpForTestUser(String loginId, DeliveryMethod deliveryMethod)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI otpForTestUSerUri = composeOTPForTestUSerUri();
    TestUserRequest testUserRequest = new TestUserRequest(loginId);
    OTPTestUserRequest request = new OTPTestUserRequest(testUserRequest, deliveryMethod);
    var apiProxy = getApiProxy();
    return apiProxy.post(otpForTestUSerUri, request, String.class);
  }

  @Override
  public String generateMagicLinkForTestUser(String loginId, URI uri, DeliveryMethod deliveryMethod)
      throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI maginLinkForTestUSerUri = composeMaginLinkForTestUSerUri();
    TestUserRequest testUserRequest = new TestUserRequest(loginId);
    MagicLinkTestUserRequest request =
        new MagicLinkTestUserRequest(testUserRequest, deliveryMethod, uri);
    var apiProxy = getApiProxy();
    return apiProxy.post(maginLinkForTestUSerUri, request, String.class);
  }

  @Override
  public String generateEnchantedLinkForTestUser(String loginId, URI uri) throws DescopeException {
    if (StringUtils.isBlank(loginId)) {
      throw ServerCommonException.invalidArgument("Login ID");
    }
    URI enchantedLinkForTestUSerUri = composeEnchantedLinkForTestUSerUri();
    TestUserRequest testUserRequest = new TestUserRequest(loginId);
    EnchantedLinkTestUserRequest request = new EnchantedLinkTestUserRequest(testUserRequest, uri);
    var apiProxy = getApiProxy();
    return apiProxy.post(enchantedLinkForTestUSerUri, request, String.class);
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
    return getUri(USER_SEARCH_ALL_LINK);
  }

  private URI composeActivateUserUri() {
    return getUri(USER_UPDATE_STATUS_LINK);
  }

  private URI composeUpdateEmailUri() {
    return getUri(USER_UPDATE_EMAIL_LINK);
  }

  private URI composeUpdatePhoneUri() {
    return getUri(USER_UPDATE_PHONE_LINK);
  }

  private URI composeUpdateUserNameUri() {
    return getUri(UPDATE_USER_NAME_LINK);
  }

  private URI composeUpdateAttributesUri() {
    return getUri(UPDATE_CUSTOM_ATTRIBUTE_LINK);
  }

  private URI composeUpdatePictureUri() {
    return getUri(UPDATE_PICTURE_LINK);
  }

  private URI composeAddRolesUri() {
    return getUri(USER_ADD_ROLES_LINK);
  }

  private URI composeRemoveRolesUri() {
    return getUri(USER_REMOVE_ROLES_LINK);
  }

  private URI composeAddTenantUri() {
    return getUri(USER_ADD_TENANT_LINK);
  }

  private URI composeRemoveTenantUri() {
    return getUri(USER_REMOVE_TENANT_LINK);
  }

  private URI composeAddTenantRolesUri() {
    return getUri(USER_ADD_ROLES_LINK);
  }

  private URI composeRemoveTenantRolesUri() {
    return getUri(USER_REMOVE_TENANT_LINK);
  }

  private URI composeOTPForTestUSerUri() {
    return getUri(COMPOSE_OTP_FOR_TEST_LINK);
  }

  private URI composeMaginLinkForTestUSerUri() {
    return getUri(MAGIC_LINK_FOR_TEST_LINK);
  }

  private URI composeEnchantedLinkForTestUSerUri() {
    return getUri(ENCHANTED_LINK_FOR_TEST_LINK);
  }

  private URI composeSetPasswordUri() {
    return getUri(USER_SET_PASSWORD_LINK);
  }

  private URI composeExpirePasswordUri() {
    return getUri(USER_EXPIRE_PASSWORD_LINK);
  }
}
